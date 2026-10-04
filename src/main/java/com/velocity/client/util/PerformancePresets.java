package com.velocity.client.util;

import com.velocity.client.VelocityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

/** One-click video settings. Tuned with a 10th-gen i3 laptop with Intel UHD graphics in mind. */
public final class PerformancePresets {
    public static final String[] NAMES = {"Potato", "Balanced", "Quality"};
    public static final String[] DESCRIPTIONS = {
            "Max FPS. Short view, no extras.",
            "Smooth + good looking. Best pick.",
            "Prettier, needs a stronger PC."
    };

    private PerformancePresets() {}

    public static void apply(MinecraftClient client, String name) {
        if (client == null || client.options == null) return;
        GameOptions o = client.options;
        switch (name) {
            case "Potato" -> {
                o.getGraphicsMode().setValue(GraphicsMode.FAST);
                o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
                o.getParticles().setValue(ParticlesMode.MINIMAL);
                o.getAo().setValue(false);
                o.getEntityShadows().setValue(false);
                o.getViewDistance().setValue(6);
                o.getSimulationDistance().setValue(5);
                o.getBiomeBlendRadius().setValue(0);
                o.getMipmapLevels().setValue(0);
                o.getEntityDistanceScaling().setValue(0.75);
                o.getEnableVsync().setValue(false);
                o.getMaxFps().setValue(260);
                o.getMenuBackgroundBlurriness().setValue(0);
            }
            case "Quality" -> {
                o.getGraphicsMode().setValue(GraphicsMode.FANCY);
                o.getCloudRenderMode().setValue(CloudRenderMode.FAST);
                o.getParticles().setValue(ParticlesMode.ALL);
                o.getAo().setValue(true);
                o.getEntityShadows().setValue(true);
                o.getViewDistance().setValue(12);
                o.getSimulationDistance().setValue(8);
                o.getBiomeBlendRadius().setValue(2);
                o.getMipmapLevels().setValue(4);
                o.getEntityDistanceScaling().setValue(1.0);
                o.getEnableVsync().setValue(false);
                o.getMaxFps().setValue(120);
            }
            default -> {
                name = "Balanced";
                o.getGraphicsMode().setValue(GraphicsMode.FAST);
                o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
                o.getParticles().setValue(ParticlesMode.DECREASED);
                o.getAo().setValue(true);
                o.getEntityShadows().setValue(false);
                o.getViewDistance().setValue(8);
                o.getSimulationDistance().setValue(5);
                o.getBiomeBlendRadius().setValue(1);
                o.getMipmapLevels().setValue(2);
                o.getEntityDistanceScaling().setValue(1.0);
                o.getEnableVsync().setValue(false);
                o.getMaxFps().setValue(144);
                o.getMenuBackgroundBlurriness().setValue(0);
            }
        }
        o.write();
        // Changing graphics mode / mipmaps needs a chunk + texture reload to fully apply.
        if (client.worldRenderer != null) client.worldRenderer.reload();
        VelocityConfig.preset = name;
        VelocityConfig.save();
    }
}
