package com.velocity.client.util;

import com.velocity.client.VelocityClient;
import com.velocity.client.gui.HudEditorScreen;
import com.velocity.client.gui.Notifications;
import com.velocity.client.gui.VelocityMenuScreen;
import com.velocity.client.module.Module;
import com.velocity.client.module.ModuleManager;
import com.velocity.client.module.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Automated check used by CI (-Dvelocity.smoketest=true). It opens every Velocity screen, creates a world,
 * exercises the in-game mods and themed vanilla screens, takes screenshots, then quits.
 * Any exception or a step that never becomes ready fails the CI job.
 */
public final class SmokeTest {
    private record Step(String name, Predicate<MinecraftClient> ready, Consumer<MinecraftClient> action, int waitAfter) {}

    private static final int STEP_TIMEOUT = 20 * 240; // ticks
    private static final List<Step> STEPS = new ArrayList<>();
    private static int index = 0;
    private static int wait = 0;
    private static int waited = 0;
    private static boolean started;

    private SmokeTest() {}

    public static boolean enabled() {
        return Boolean.getBoolean("velocity.smoketest");
    }

    public static void tick(MinecraftClient client) {
        if (!started) {
            if (client.currentScreen == null || client.getOverlay() != null) return; // still loading
            started = true;
            build();
            client.options.pauseOnLostFocus = false; // the virtual display may not have focus
            Module dynamicFps = ModuleManager.get(DynamicFpsModule.class);
            if (dynamicFps != null) dynamicFps.setEnabledSilently(false);
        }
        if (index >= STEPS.size()) return;
        if (wait > 0) {
            wait--;
            return;
        }
        Step step = STEPS.get(index);
        if (!step.ready.test(client)) {
            if (++waited > STEP_TIMEOUT) throw new IllegalStateException("[smoketest] step timed out: " + step.name);
            return;
        }
        VelocityClient.LOGGER.info("[smoketest] step: {}", step.name);
        step.action.accept(client);
        waited = 0;
        wait = step.waitAfter;
        index++;
    }

    private static void step(String name, Consumer<MinecraftClient> action) {
        STEPS.add(new Step(name, c -> true, action, 30));
    }

    private static void when(String name, Predicate<MinecraftClient> ready, Consumer<MinecraftClient> action, int waitAfter) {
        STEPS.add(new Step(name, ready, action, waitAfter));
    }

    private static void shot(MinecraftClient client, String name) {
        ScreenshotRecorder.saveScreenshot(client.runDirectory, "velocity-" + name + ".png", client.getFramebuffer(),
                text -> VelocityClient.LOGGER.info("[smoketest] {}", text.getString()));
    }

    private static void enable(Class<? extends Module> type) {
        Module m = ModuleManager.get(type);
        if (m != null) m.setEnabled(true);
    }

    private static void build() {
        // Load every mixin target now so a broken injection fails here instead of on a player's PC.
        step("audit mixins", c -> MixinEnvironment.getCurrentEnvironment().audit());

        // Skip the first-launch accessibility screen; the mixin turns this into the Velocity main menu.
        step("title", c -> c.setScreen(new TitleScreen()));
        step("shot title", c -> shot(c, "01-title"));
        step("menu", c -> c.setScreen(new VelocityMenuScreen(c.currentScreen)));
        step("shot mods", c -> shot(c, "02-mods"));
        step("performance", c -> ((VelocityMenuScreen) c.currentScreen).showPage("PERFORMANCE"));
        step("shot performance", c -> shot(c, "03-performance"));
        step("settings", c -> ((VelocityMenuScreen) c.currentScreen).showPage("SETTINGS"));
        step("shot settings", c -> shot(c, "04-settings"));
        step("crosshair options", c -> c.setScreen(new VelocityMenuScreen(new TitleScreen(), ModuleManager.get(CrosshairModule.class))));
        step("shot options", c -> shot(c, "05-options"));
        step("vanilla options", c -> c.setScreen(new OptionsScreen(new TitleScreen(), c.options)));
        step("shot vanilla options", c -> shot(c, "06-vanilla-options"));

        // Create a world so in-game features are exercised for real.
        step("create world screen", c -> CreateWorldScreen.create(c, new TitleScreen()));
        when("press create", c -> c.currentScreen instanceof CreateWorldScreen && findCreateButton(c) != null,
                c -> findCreateButton(c).onPress(), 20);
        when("world loaded", c -> c.world != null && c.player != null && c.currentScreen == null, c -> {
            enable(KeystrokesModule.class);
            enable(CoordinatesModule.class);
            enable(CrosshairModule.class);
            enable(BiomeModule.class);
            enable(ReachDisplayModule.class);
            enable(ComboModule.class);
            Notifications.push("Velocity smoke test", 0xFF2BD47D);
        }, 100);
        step("shot hud", c -> shot(c, "07-ingame-hud"));
        step("zoom on", c -> VelocityClient.ZOOM_KEY.setPressed(true));
        step("shot zoom", c -> shot(c, "08-zoom"));
        step("zoom off, freelook on", c -> {
            VelocityClient.ZOOM_KEY.setPressed(false);
            VelocityClient.FREELOOK_KEY.setPressed(true);
        });
        step("turn camera", c -> c.player.changeLookDirection(600, 0));
        step("shot freelook", c -> shot(c, "09-freelook"));
        step("freelook off", c -> VelocityClient.FREELOOK_KEY.setPressed(false));
        step("inventory", c -> c.setScreen(new InventoryScreen(c.player)));
        step("shot inventory", c -> shot(c, "10-inventory"));
        step("advancements", c -> c.setScreen(new AdvancementsScreen(c.player.networkHandler.getAdvancementHandler())));
        step("shot advancements", c -> shot(c, "11-advancements"));
        step("pause menu", c -> c.setScreen(new GameMenuScreen(true)));
        step("shot pause", c -> shot(c, "12-pause"));
        step("hud editor in game", c -> c.setScreen(new HudEditorScreen(null)));
        step("shot hud editor", c -> shot(c, "13-hud-editor"));
        step("finish", c -> {
            c.setScreen(null);
            VelocityClient.LOGGER.info("[smoketest] VELOCITY SMOKE TEST PASSED");
            c.scheduleStop();
        });
    }

    private static ButtonWidget findCreateButton(MinecraftClient client) {
        if (client.currentScreen == null) return null;
        for (var child : client.currentScreen.children()) {
            if (child instanceof ButtonWidget button
                    && button.getMessage().getContent() instanceof TranslatableTextContent t
                    && t.getKey().equals("selectWorld.create")) {
                return button;
            }
        }
        return null;
    }
}
