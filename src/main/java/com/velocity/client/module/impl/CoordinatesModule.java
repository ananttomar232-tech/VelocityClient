package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import com.velocity.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class CoordinatesModule extends TextHudModule {
    private final BoolSetting decimals = add(new BoolSetting("Decimals", false));
    private final BoolSetting facing = add(new BoolSetting("Show Facing", true));

    public CoordinatesModule() {
        super("coordinates", "Coordinates", "Your X / Y / Z position.", Category.HUD, Items.COMPASS, false, 0.005f, 0.19f);
    }

    @Override
    protected String label() {
        return "XYZ";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        if (client.player == null) return "128, 64, -256";
        var p = client.player;
        String pos = decimals.isOn()
                ? String.format("%.1f, %.1f, %.1f", p.getX(), p.getY(), p.getZ())
                : p.getBlockX() + ", " + p.getBlockY() + ", " + p.getBlockZ();
        if (facing.isOn()) pos += " " + DirectionModule.shortFacing(p.getYaw());
        return pos;
    }
}
