package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class SpeedModule extends TextHudModule {
    private double lastX;
    private double lastZ;
    private double speed;

    public SpeedModule() {
        super("speed", "Speed", "Horizontal movement speed in blocks per second.", Category.HUD, Items.FEATHER, false, 0.005f, 0.37f);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        double dx = client.player.getX() - lastX;
        double dz = client.player.getZ() - lastZ;
        lastX = client.player.getX();
        lastZ = client.player.getZ();
        double now = Math.sqrt(dx * dx + dz * dz) * 20.0;
        if (now > 100) now = 0; // teleport / respawn
        speed = speed * 0.6 + now * 0.4;
    }

    @Override
    protected String label() {
        return "Speed";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        return String.format("%.2f b/s", client.player == null ? 5.61 : speed);
    }
}
