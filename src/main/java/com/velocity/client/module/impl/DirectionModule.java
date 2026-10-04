package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

public final class DirectionModule extends TextHudModule {
    private static final String[] NAMES = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    public DirectionModule() {
        super("direction", "Direction", "The way you are facing, with yaw angle.", Category.HUD, Items.RECOVERY_COMPASS, false, 0.005f, 0.31f);
    }

    public static String shortFacing(float yaw) {
        int index = Math.floorMod(Math.round(MathHelper.wrapDegrees(yaw) / 45f), 8);
        return NAMES[index];
    }

    @Override
    protected String label() {
        return "Facing";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        if (client.player == null) return "N (180.0)";
        float yaw = MathHelper.wrapDegrees(client.player.getYaw());
        return shortFacing(yaw) + String.format(" (%.1f)", yaw);
    }
}
