package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.TextHudModule;
import com.velocity.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

/** Press your sprint key once to keep sprinting, press again to stop. */
public final class ToggleSprintModule extends TextHudModule {
    private final BoolSetting showText = add(new BoolSetting("Show HUD Text", true));
    private boolean toggled = true;

    public ToggleSprintModule() {
        super("togglesprint", "Toggle Sprint", "Tap sprint once and never hold Ctrl again.", Category.PVP, Items.LEATHER_BOOTS, true, 0.005f, 0.13f);
        showLabel.set(false);
        brackets.set(true);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null) return;
        var sprintKey = client.options.sprintKey;
        while (sprintKey.wasPressed()) {
            toggled = !toggled;
            if (!toggled) sprintKey.setPressed(false);
        }
        // Only press when not already pressed so the vanilla "Sprint: Toggle" option is not flipped every tick.
        if (toggled && !sprintKey.isPressed()) sprintKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        MinecraftClient.getInstance().options.sprintKey.setPressed(false);
    }

    @Override
    public boolean hasContent(MinecraftClient client) {
        return showText.isOn();
    }

    @Override
    protected String label() {
        return "Sprint";
    }

    @Override
    protected String value(MinecraftClient client, boolean editor) {
        if (!toggled) return "Sprinting (Off)";
        boolean sprinting = client.player != null && client.player.isSprinting();
        return sprinting ? "Sprinting (Toggled)" : "Sprint (Toggled)";
    }
}
