package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.HudModule;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.module.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

public final class ArmorStatusModule extends HudModule {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final ItemStack[] PREVIEW = {
            new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE),
            new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS), new ItemStack(Items.DIAMOND_SWORD)};

    private final ModeSetting durability = add(new ModeSetting("Durability", "Value", "Value", "Percent", "None"));
    private final BoolSetting showHand = add(new BoolSetting("Show Held Item", true));

    public ArmorStatusModule() {
        super("armor", "Armor Status", "Your armor and held item with durability.", Category.HUD, Items.DIAMOND_CHESTPLATE, true, 0.005f, 0.75f);
    }

    private List<ItemStack> stacks(MinecraftClient client, boolean editor) {
        List<ItemStack> list = new ArrayList<>();
        if (client.player == null) {
            for (int i = 0; i < PREVIEW.length; i++) if (i < 4 || showHand.isOn()) list.add(PREVIEW[i]);
            return list;
        }
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = client.player.getEquippedStack(slot);
            if (!stack.isEmpty()) list.add(stack);
        }
        if (showHand.isOn() && !client.player.getMainHandStack().isEmpty()) list.add(client.player.getMainHandStack());
        if (list.isEmpty() && editor) {
            for (int i = 0; i < 4; i++) list.add(PREVIEW[i]);
        }
        return list;
    }

    private String label(ItemStack stack) {
        if (durability.is("None")) return "";
        if (!stack.isDamageable()) return stack.getCount() > 1 ? Integer.toString(stack.getCount()) : "";
        int left = stack.getMaxDamage() - stack.getDamage();
        return durability.is("Percent") ? (left * 100 / Math.max(1, stack.getMaxDamage())) + "%" : Integer.toString(left);
    }

    @Override
    public boolean hasContent(MinecraftClient client) {
        return !stacks(client, false).isEmpty();
    }

    @Override
    public int getWidth(MinecraftClient client, boolean editor) {
        int widest = 0;
        for (ItemStack stack : stacks(client, editor)) widest = Math.max(widest, client.textRenderer.getWidth(label(stack)));
        return 16 + (widest > 0 ? widest + 6 : 0) + 4;
    }

    @Override
    public int getHeight(MinecraftClient client, boolean editor) {
        return Math.max(1, stacks(client, editor).size()) * 17 + 2;
    }

    @Override
    protected void draw(DrawContext ctx, MinecraftClient client, boolean editor) {
        List<ItemStack> list = stacks(client, editor);
        drawBackground(ctx, getWidth(client, editor), getHeight(client, editor));
        int y = 2;
        for (ItemStack stack : list) {
            ctx.drawItem(stack, 2, y);
            String text = label(stack);
            if (!text.isEmpty()) {
                int c = color(y * 0.01f);
                if (stack.isDamageable() && textColor.is("White")) c = 0xFF000000 | stack.getItemBarColor();
                ctx.drawText(client.textRenderer, text, 22, y + 4, c, true);
            }
            y += 17;
        }
    }
}
