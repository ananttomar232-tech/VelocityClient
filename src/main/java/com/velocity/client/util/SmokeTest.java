package com.velocity.client.util;

import com.velocity.client.VelocityClient;
import com.velocity.client.gui.HudEditorScreen;
import com.velocity.client.gui.VelocityMenuScreen;
import com.velocity.client.module.ModuleManager;
import com.velocity.client.module.impl.KeystrokesModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Automated check used by CI (-Dvelocity.smoketest=true): opens every Velocity screen, takes screenshots,
 * then quits. If anything throws, the game crashes and the CI job fails.
 */
public final class SmokeTest {
    private static final List<Consumer<MinecraftClient>> STEPS = new ArrayList<>();
    private static int step = -1;
    private static int wait = 0;

    private SmokeTest() {}

    public static boolean enabled() {
        return Boolean.getBoolean("velocity.smoketest");
    }

    public static void tick(MinecraftClient client) {
        if (client.currentScreen == null && step < 0) return; // still loading
        if (client.getOverlay() != null) return;
        if (STEPS.isEmpty()) build();
        if (++wait < 30) return;
        wait = 0;
        step++;
        if (step < STEPS.size()) {
            STEPS.get(step).accept(client);
        }
    }

    private static void shot(MinecraftClient client, String name) {
        ScreenshotRecorder.saveScreenshot(client.runDirectory, "velocity-" + name + ".png", client.getFramebuffer(),
                text -> VelocityClient.LOGGER.info("[smoketest] {}", text.getString()));
    }

    private static void build() {
        STEPS.add(c -> shot(c, "1-title"));
        STEPS.add(c -> c.setScreen(new VelocityMenuScreen(c.currentScreen)));
        STEPS.add(c -> shot(c, "2-mods"));
        STEPS.add(c -> ((VelocityMenuScreen) c.currentScreen).showPage("PERFORMANCE"));
        STEPS.add(c -> shot(c, "3-performance"));
        STEPS.add(c -> ((VelocityMenuScreen) c.currentScreen).showPage("SETTINGS"));
        STEPS.add(c -> shot(c, "4-settings"));
        STEPS.add(c -> c.setScreen(new VelocityMenuScreen(null, ModuleManager.get(KeystrokesModule.class))));
        STEPS.add(c -> shot(c, "5-options"));
        STEPS.add(c -> c.setScreen(new HudEditorScreen(null)));
        STEPS.add(c -> shot(c, "6-hud-editor"));
        STEPS.add(c -> {
            VelocityClient.LOGGER.info("[smoketest] VELOCITY SMOKE TEST PASSED");
            c.scheduleStop();
        });
    }
}
