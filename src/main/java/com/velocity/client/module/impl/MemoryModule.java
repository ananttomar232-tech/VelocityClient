package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class MemoryModule extends TextHudModule {
    public MemoryModule() {
        super("memory", "Memory", "How much RAM Minecraft is using.", Category.PERFORMANCE, Items.WRITABLE_BOOK, false, 0.005f, 0.43f);
    }

    @Override
    protected String label() {
        return "RAM";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        return (used * 100 / Math.max(1, max)) + "% " + used + "/" + max + " MB";
    }
}
