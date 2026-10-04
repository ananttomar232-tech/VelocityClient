package com.velocity.client;

import com.velocity.client.gui.HudEditorScreen;
import com.velocity.client.gui.VelocityMenuScreen;
import com.velocity.client.module.ModuleManager;
import com.velocity.client.module.impl.FullbrightModule;
import com.velocity.client.theme.BuiltinPacks;
import com.velocity.client.theme.DarkTextures;
import com.velocity.client.util.CombatTracker;
import com.velocity.client.util.FpsTracker;
import com.velocity.client.util.Gfx;
import com.velocity.client.util.PerformancePresets;
import com.velocity.client.util.SmokeTest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.ActionResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VelocityClient implements ClientModInitializer {
    public static final String MOD_ID = "velocity";
    public static final String NAME = "Velocity Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.velocity.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.velocity"));
    public static final KeyBinding ZOOM_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.velocity.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.velocity"));
    public static final KeyBinding FREELOOK_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.velocity.freelook", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.velocity"));
    public static final KeyBinding HUD_EDITOR = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.velocity.hud_editor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.velocity"));

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        VelocityConfig.load();
        LOGGER.info("{} {} ready with {} mods", NAME, version(), ModuleManager.all().size());

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new DarkTextures());
        BuiltinPacks.register();

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) CombatTracker.onAttack(player, entity);
            return ActionResult.PASS;
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (!VelocityConfig.firstRunDone) {
                // First launch: pick sensible settings for low/mid-range PCs so the game feels smooth right away.
                PerformancePresets.apply(client, "Balanced");
                VelocityConfig.firstRunDone = true;
                VelocityConfig.save();
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            FullbrightModule fullbright = ModuleManager.get(FullbrightModule.class);
            if (fullbright != null) fullbright.restore();
            VelocityConfig.save();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FpsTracker.tick(client);
            CombatTracker.tick(client);
            if (SmokeTest.enabled()) SmokeTest.tick(client);
            ModuleManager.tick(client);
            while (OPEN_MENU.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new VelocityMenuScreen(null));
            }
            while (HUD_EDITOR.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new HudEditorScreen(null));
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> ModuleManager.renderHud(context, MinecraftClient.getInstance()));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen || screen instanceof GameMenuScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("⚡ Velocity").withColor(Gfx.accent() & 0xFFFFFF),
                                b -> client.setScreen(new VelocityMenuScreen(screen)))
                        .dimensions(6, scaledHeight - 26, 84, 20)
                        .build());
            }
        });
    }

    public static String version() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
    }
}
