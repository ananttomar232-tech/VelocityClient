package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import com.velocity.client.module.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ClockModule extends TextHudModule {
    private static final DateTimeFormatter H24 = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter H12 = DateTimeFormatter.ofPattern("h:mm a");
    private final ModeSetting format = add(new ModeSetting("Format", "24 Hour", "24 Hour", "12 Hour"));

    public ClockModule() {
        super("clock", "Clock", "Real-world time so you know when to stop playing.", Category.HUD, Items.CLOCK, false, 0.88f, 0.01f);
    }

    @Override
    protected String label() {
        return "Time";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        return LocalTime.now().format(format.is("24 Hour") ? H24 : H12);
    }
}
