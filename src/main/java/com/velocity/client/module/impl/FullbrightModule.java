package com.velocity.client.module.impl;

import com.velocity.client.mixin.SimpleOptionAccessor;
import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

/** See in caves without torches or night vision. */
public final class FullbrightModule extends Module {
    private static final double BRIGHT = 15.0;
    private double previousGamma = 1.0;

    public FullbrightModule() {
        super("fullbright", "Fullbright", "Maximum brightness everywhere, no torches needed.", Category.UTILITY, Items.GLOWSTONE, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTick(MinecraftClient client) {
        var gamma = client.options.getGamma();
        if (gamma.getValue() < BRIGHT) {
            previousGamma = Math.min(1.0, gamma.getValue());
            ((SimpleOptionAccessor<Double>) (Object) gamma).velocity$setValue(BRIGHT);
        }
    }

    @Override
    public void onDisable() {
        restore();
    }

    /** Puts gamma back to a valid value so it is never saved out of range. */
    @SuppressWarnings("unchecked")
    public void restore() {
        var gamma = MinecraftClient.getInstance().options.getGamma();
        if (gamma.getValue() > 1.0) {
            ((SimpleOptionAccessor<Double>) (Object) gamma).velocity$setValue(previousGamma);
        }
    }
}
