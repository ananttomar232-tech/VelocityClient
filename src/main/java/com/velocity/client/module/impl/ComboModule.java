package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import com.velocity.client.util.CombatTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class ComboModule extends TextHudModule {
    public ComboModule() {
        super("combo", "Combo Counter", "Hits in a row without getting hit back.", Category.PVP, Items.IRON_SWORD, false, 0.99f, 0.49f);
    }

    @Override
    protected String label() {
        return "Combo";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        int combo = CombatTracker.combo();
        return combo == 0 && editor ? "3" : Integer.toString(combo);
    }
}
