package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import com.velocity.client.util.CombatTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class ReachDisplayModule extends TextHudModule {
    public ReachDisplayModule() {
        super("reach", "Reach Display", "How far away your last hit landed.", Category.PVP, Items.STICK, false, 0.99f, 0.42f);
    }

    @Override
    protected String label() {
        return "Reach";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        double reach = CombatTracker.reach();
        if (reach < 0 || System.currentTimeMillis() - CombatTracker.lastHitAt() > 5000) return editor ? "2.87 blocks" : "--";
        return String.format("%.2f blocks", reach);
    }
}
