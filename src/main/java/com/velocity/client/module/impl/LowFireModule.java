package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import com.velocity.client.module.setting.NumberSetting;
import net.minecraft.item.Items;

public final class LowFireModule extends Module {
    private static LowFireModule instance;

    private final NumberSetting height = add(new NumberSetting("Lower By", 0.3, 0.1, 0.6, 0.05));

    public LowFireModule() {
        super("lowfire", "Low Fire", "Moves the fire overlay down so you can see when burning.", Category.PVP, Items.BLAZE_POWDER, true);
        instance = this;
    }

    /** How far to move the fire overlay down, or 0 when off. */
    public static float offset() {
        return instance != null && instance.isEnabled() ? instance.height.getFloat() : 0f;
    }
}
