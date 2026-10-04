package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import net.minecraft.item.Items;

public final class NoHurtCamModule extends Module {
    private static NoHurtCamModule instance;

    public NoHurtCamModule() {
        super("nohurtcam", "No Hurt Cam", "Stops the screen shaking when you take damage.", Category.PVP, Items.SHIELD, false);
        instance = this;
    }

    public static boolean active() {
        return instance != null && instance.isEnabled();
    }
}
