package com.velocity.client.module;

import com.velocity.client.module.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** A feature that can be switched on and off from the Velocity menu. */
public abstract class Module {
    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final ItemStack icon;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String id, String name, String description, Category category, Item icon, boolean enabledByDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.icon = new ItemStack(icon);
        this.enabled = enabledByDefault;
    }

    protected <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    public void onEnable() {}

    public void onDisable() {}

    /** Called every client tick while enabled. */
    public void onTick(MinecraftClient client) {}

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    /** Sets the flag without running enable/disable hooks (used when loading the config). */
    public final void setEnabledSilently(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }
}
