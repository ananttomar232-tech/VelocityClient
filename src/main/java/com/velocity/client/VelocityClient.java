package com.velocity.client;

import com.velocity.client.gui.VelocityScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class VelocityClient implements net.fabricmc.api.ClientModInitializer {
    public static final String MOD_ID = "velocity";
    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.velocity.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.velocity")
    );

    @Override
    public void onInitializeClient() {
        VelocityConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.wasPressed()) {
                client.setScreen(new VelocityScreen(client.currentScreen));
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                client.execute(() -> {
                    // A compact button that opens Velocity from the vanilla title screen.
                    int x = 8;
                    int y = Math.max(8, scaledHeight - 28);
                    ((TitleScreen) screen).addDrawableChild(
                            net.minecraft.client.gui.widget.ButtonWidget.builder(
                                            Text.literal("Velocity"),
                                            button -> client.setScreen(new VelocityScreen(screen))
                                    )
                                    .dimensions(x, y, 90, 20)
                                    .build()
                    );
                });
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.hudHidden || client.player == null) return;

            int x = 8;
            int y = 8;
            int line = 11;

            if (VelocityConfig.fpsHud) {
                String fps = "FPS " + client.getCurrentFps();
                drawContext.drawTextWithShadow(client.textRenderer, Text.literal(fps), x, y, 0xFFFFFFFF);
                y += line;
            }

            if (VelocityConfig.coordsHud && client.player != null) {
                String coords = String.format("XYZ %.1f / %.1f / %.1f", client.player.getX(), client.player.getY(), client.player.getZ());
                drawContext.drawTextWithShadow(client.textRenderer, Text.literal(coords), x, y, 0xFFFFFFFF);
                y += line;
            }

            if (VelocityConfig.sprintHud) {
                String state = client.player.isSprinting() ? "SPRINT" : "WALK";
                drawContext.drawTextWithShadow(client.textRenderer, Text.literal(state), x, y, 0xFFEFEFEF);
            }
        });
    }

    public static void applyPerformanceProfile(MinecraftClient client) {
        if (client == null) return;
        var options = client.options;

        if (VelocityConfig.performanceProfile) {
            options.getGraphicsMode().setValue(GraphicsMode.FAST);
            options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            options.getParticles().setValue(ParticlesMode.DECREASED);
            options.getEntityShadows().setValue(false);
            options.getAo().setValue(false);
            options.getViewDistance().setValue(Math.min(options.getViewDistance().getValue(), 8));
            options.getSimulationDistance().setValue(Math.min(options.getSimulationDistance().getValue(), 5));
            options.write();
        }
    }

    public static String loadedModSummary() {
        int count = FabricLoader.getInstance().getAllMods().size();
        return count + " loaded mods";
    }
}
