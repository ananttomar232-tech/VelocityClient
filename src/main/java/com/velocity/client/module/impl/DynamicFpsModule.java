package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.module.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

/**
 * Lowers the frame rate when Minecraft is in the background. On laptops with integrated graphics this keeps
 * the CPU and GPU cool, so they are not thermal-throttled when you come back to the game.
 */
public final class DynamicFpsModule extends Module {
    private static DynamicFpsModule instance;

    private final NumberSetting unfocusedFps = add(new NumberSetting("Background FPS", 10, 1, 60, 1));
    private final BoolSetting capMenus = add(new BoolSetting("Cap Menus To 60 FPS", true));

    public DynamicFpsModule() {
        super("dynamicfps", "Dynamic FPS", "Saves power and heat when tabbed out or in menus.", Category.PERFORMANCE, Items.COMPARATOR, true);
        instance = this;
    }

    /** Returns a frame limit to use instead of vanilla's, or -1 to keep vanilla's. */
    public static int frameLimit(MinecraftClient client) {
        DynamicFpsModule m = instance;
        if (m == null || !m.isEnabled()) return -1;
        if (!client.isWindowFocused()) return m.unfocusedFps.getInt();
        if (m.capMenus.isOn() && client.world == null) return 60;
        return -1;
    }
}
