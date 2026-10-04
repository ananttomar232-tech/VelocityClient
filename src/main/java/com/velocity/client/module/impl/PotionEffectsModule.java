package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.HudModule;
import com.velocity.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PotionEffectsModule extends HudModule {
    private static final String[] ROMAN = {"", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    private static final int ROW = 20;

    private final BoolSetting showIcons = add(new BoolSetting("Show Icons", true));

    public PotionEffectsModule() {
        super("potions", "Potion Effects", "Active effects with time remaining.", Category.HUD, Items.POTION, false, 0.85f, 0.3f);
    }

    private Collection<StatusEffectInstance> effects(MinecraftClient client, boolean editor) {
        if (client.player != null && !client.player.getStatusEffects().isEmpty()) return client.player.getStatusEffects();
        List<StatusEffectInstance> preview = new ArrayList<>();
        if (editor || client.player == null) {
            preview.add(new StatusEffectInstance(StatusEffects.SPEED, 1800, 1));
            preview.add(new StatusEffectInstance(StatusEffects.STRENGTH, 900, 0));
        }
        return preview;
    }

    private String name(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString();
        int amp = effect.getAmplifier();
        if (amp > 0) name += " " + (amp < ROMAN.length ? ROMAN[amp] : Integer.toString(amp + 1));
        return name;
    }

    private String time(StatusEffectInstance effect) {
        if (effect.isInfinite()) return "**:**";
        int seconds = effect.getDuration() / 20;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public boolean hasContent(MinecraftClient client) {
        return client.player != null && !client.player.getStatusEffects().isEmpty();
    }

    @Override
    public int getWidth(MinecraftClient client, boolean editor) {
        int widest = 40;
        for (StatusEffectInstance e : effects(client, editor)) {
            widest = Math.max(widest, Math.max(client.textRenderer.getWidth(name(e)), client.textRenderer.getWidth(time(e))));
        }
        return widest + (showIcons.isOn() ? 24 : 6);
    }

    @Override
    public int getHeight(MinecraftClient client, boolean editor) {
        return Math.max(1, effects(client, editor).size()) * ROW + 2;
    }

    @Override
    protected void draw(DrawContext ctx, MinecraftClient client, boolean editor) {
        drawBackground(ctx, getWidth(client, editor), getHeight(client, editor));
        int y = 2;
        int textX = showIcons.isOn() ? 22 : 3;
        for (StatusEffectInstance e : effects(client, editor)) {
            if (showIcons.isOn()) {
                ctx.drawSprite(2, y, 0, 18, 18, client.getStatusEffectSpriteManager().getSprite(e.getEffectType()));
            }
            ctx.drawText(client.textRenderer, name(e), textX, y + 1, color(y * 0.01f), true);
            ctx.drawText(client.textRenderer, time(e), textX, y + 10, 0xFFAAAAAA, true);
            y += ROW;
        }
    }
}
