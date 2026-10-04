package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import net.minecraft.item.Items;

public final class NoPumpkinModule extends Module {
    private static NoPumpkinModule instance;

    public NoPumpkinModule() {
        super("nopumpkin", "No Pumpkin Blur", "See clearly while wearing a carved pumpkin.", Category.UTILITY, Items.CARVED_PUMPKIN, true);
        instance = this;
    }

    public static boolean active() {
        return instance != null && instance.isEnabled();
    }
}
