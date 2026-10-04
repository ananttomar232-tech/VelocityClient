package com.velocity.client.theme;

import com.velocity.client.util.Gfx;
import net.minecraft.client.gui.DrawContext;

/** Drawing for vanilla widgets when the Velocity UI theme is on. */
public final class VanillaTheme {
    private VanillaTheme() {}

    public static void button(DrawContext ctx, int x, int y, int w, int h, boolean active, float hover, float alpha) {
        int fill = active ? Gfx.mix(0xE81A1E29, 0xF5262C3C, hover) : 0xB0141720;
        int border = active ? Gfx.mix(0x40FFFFFF, Gfx.accent(), hover) : 0x20FFFFFF;
        Gfx.roundBox(ctx, x, y, w, h, 3, Gfx.fade(fill, alpha), Gfx.fade(border, alpha));
        int bar = Math.round((w - 8) * hover);
        if (bar > 1 && active) {
            Gfx.roundRect(ctx, x + w / 2 - bar / 2, y + h - 3, bar, 2, 1, Gfx.fade(Gfx.accent(), alpha));
        }
    }

    public static void slider(DrawContext ctx, int x, int y, int w, int h, double value, boolean active, float hover, float alpha) {
        button(ctx, x, y, w, h, active, hover * 0.6f, alpha);
        int fill = Math.round((w - 4) * (float) Math.max(0, Math.min(1, value)));
        if (fill > 0) Gfx.roundRect(ctx, x + 2, y + 2, fill, h - 4, 2, Gfx.fade(Gfx.withAlpha(Gfx.accent(), 70), alpha));
        int knobX = x + 2 + Math.round((w - 10) * (float) Math.max(0, Math.min(1, value)));
        Gfx.roundRect(ctx, knobX, y + 2, 6, h - 4, 2, Gfx.fade(Gfx.mix(0xFFCDD2DD, 0xFFFFFFFF, hover), alpha));
    }

    /** Dark text colour used by vanilla containers, replaced so titles stay readable on dark windows. */
    public static final int VANILLA_DARK_TEXT = 4210752;

    public static int containerText(int original) {
        return com.velocity.client.VelocityConfig.darkInventories ? 0xE3E6EE : original;
    }
}
