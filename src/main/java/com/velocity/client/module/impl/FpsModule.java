package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class FpsModule extends TextHudModule {
    public FpsModule() {
        super("fps", "FPS", "Shows your frames per second.", Category.HUD, Items.REDSTONE_TORCH, true, 0.005f, 0.01f);
    }

    @Override
    protected String label() {
        return "FPS";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        return Integer.toString(client.getCurrentFps());
    }
}
