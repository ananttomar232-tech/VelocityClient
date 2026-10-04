package com.velocity.client.util;

import com.velocity.client.VelocityConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Small drawing helpers used by Velocity's menus and HUD. Everything is plain fills, so it is cheap on integrated GPUs. */
public final class Gfx {
    public static final int GREEN = 0xFF2BD47D;
    public static final int RED = 0xFFF0475C;
    public static final int TEXT = 0xFFF2F4F8;
    public static final int MUTED = 0xFF9097A6;
    public static final int PANEL = 0xF0151821;
    public static final int CARD = 0xFF1E2230;
    public static final int CARD_HOVER = 0xFF272C3D;
    public static final int STROKE = 0xFF2E3446;

    private Gfx() {}

    public static int accent() {
        return VelocityConfig.accentColor();
    }

    /** Filled rectangle with softened corners (radius in GUI pixels, 0-4 looks best). */
    public static void roundRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            ctx.fill(x, y, x + w, y + h, color);
            return;
        }
        ctx.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            // Inset for this row of the corner, approximating a quarter circle.
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            ctx.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            ctx.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
    }

    /** Rounded rectangle with a 1px border. */
    public static void roundBox(DrawContext ctx, int x, int y, int w, int h, int r, int fill, int border) {
        roundRect(ctx, x, y, w, h, r, border);
        roundRect(ctx, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    public static void outline(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static void centered(DrawContext ctx, TextRenderer tr, String text, int cx, int y, int color) {
        ctx.drawText(tr, text, cx - tr.getWidth(text) / 2, y, color, true);
    }

    public static void text(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color) {
        ctx.drawText(tr, text, x, y, color, true);
    }

    /** Draws an item icon scaled up, e.g. scale 2 gives a 32px icon. */
    public static void item(DrawContext ctx, ItemStack stack, int x, int y, float scale) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(scale, scale, 1);
        ctx.drawItem(stack, 0, 0);
        ctx.getMatrices().pop();
    }

    /** Text drawn at a custom scale; returns nothing, use width * scale to measure. */
    public static void scaledText(DrawContext ctx, TextRenderer tr, String text, int x, int y, float scale, int color) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(scale, scale, 1);
        ctx.drawText(tr, text, 0, 0, color, true);
        ctx.getMatrices().pop();
    }

    public static void scaledCentered(DrawContext ctx, TextRenderer tr, String text, int cx, int y, float scale, int color) {
        scaledText(ctx, tr, text, cx - Math.round(tr.getWidth(text) * scale / 2f), y, scale, color);
    }

    public static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF);
    }

    public static int mix(int a, int b, float t) {
        t = Math.max(0, Math.min(1, t));
        int aa = a >>> 24, ar = a >> 16 & 255, ag = a >> 8 & 255, ab = a & 255;
        int ba = b >>> 24, br = b >> 16 & 255, bg = b >> 8 & 255, bb = b & 255;
        return (int) (aa + (ba - aa) * t) << 24
                | (int) (ar + (br - ar) * t) << 16
                | (int) (ag + (bg - ag) * t) << 8
                | (int) (ab + (bb - ab) * t);
    }

    /** Rainbow colour that slowly cycles; offset shifts the hue so text can wave. */
    public static int chroma(float offset) {
        double speed = VelocityConfig.chromaSpeed;
        float hue = (float) ((System.currentTimeMillis() * speed / 4000.0 + offset) % 1.0);
        return 0xFF000000 | hsv(hue, 0.65f, 1f);
    }

    public static int hsv(float h, float s, float v) {
        float r, g, b;
        int i = (int) (h * 6) % 6;
        float f = h * 6 - (int) (h * 6);
        float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        switch (i) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
    }

    /** Frame-rate independent smoothing toward a target value. */
    public static float approach(float current, float target, float speedPerSecond, float deltaSeconds) {
        float k = 1f - (float) Math.exp(-speedPerSecond * deltaSeconds);
        return current + (target - current) * k;
    }

    public static float easeOutCubic(float t) {
        t = Math.max(0, Math.min(1, t));
        return 1 - (float) Math.pow(1 - t, 3);
    }
}
