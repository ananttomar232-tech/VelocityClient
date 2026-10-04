package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

/** Turns off the expensive background blur behind menus, which is slow on integrated graphics. */
public final class LowSpecMenusModule extends Module {
    private int savedBlur = 5;

    public LowSpecMenusModule() {
        super("nomenublur", "No Menu Blur", "Removes the heavy menu blur for smoother menus.", Category.PERFORMANCE, Items.GLASS_PANE, true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        var blur = client.options.getMenuBackgroundBlurriness();
        if (blur.getValue() != 0) {
            savedBlur = blur.getValue();
            blur.setValue(0);
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient.getInstance().options.getMenuBackgroundBlurriness().setValue(savedBlur);
    }
}
