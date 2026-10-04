package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class DayCounterModule extends TextHudModule {
    public DayCounterModule() {
        super("days", "Day Counter", "How many Minecraft days this world has lasted.", Category.HUD, Items.SUNFLOWER, false, 0.4f, 0.13f);
    }

    @Override
    protected String label() {
        return "Day";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        if (client.world == null) return "42";
        return Long.toString(client.world.getTimeOfDay() / 24000L + 1);
    }
}
