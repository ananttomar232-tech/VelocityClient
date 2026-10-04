package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.util.ClickCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

public final class CpsModule extends TextHudModule {
    private final BoolSetting showRight = add(new BoolSetting("Show Right Click", true));

    public CpsModule() {
        super("cps", "CPS", "Clicks per second for left and right mouse buttons.", Category.PVP, Items.STONE_BUTTON, true, 0.005f, 0.07f);
    }

    @Override
    protected String label() {
        return "CPS";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        int left = ClickCounter.left();
        return showRight.isOn() ? left + " | " + ClickCounter.right() : Integer.toString(left);
    }
}
