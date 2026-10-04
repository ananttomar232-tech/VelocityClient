package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.module.setting.ModeSetting;
import com.velocity.client.module.setting.NumberSetting;
import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;

/** Custom crosshair: cross, dot, circle or T-shape with any colour. */
public final class CrosshairModule extends Module {
    private static CrosshairModule instance;

    private final ModeSetting style = add(new ModeSetting("Style", "Cross", "Cross", "Dot", "Circle", "T Shape"));
    private final ModeSetting color = add(new ModeSetting("Color", "White", "White", "Accent", "Green", "Red", "Cyan", "Chroma"));
    private final NumberSetting size = add(new NumberSetting("Size", 5, 2, 12, 1));
    private final NumberSetting gap = add(new NumberSetting("Gap", 2, 0, 8, 1));
    private final NumberSetting thickness = add(new NumberSetting("Thickness", 1, 1, 3, 1));
    private final BoolSetting outline = add(new BoolSetting("Outline", true));
    private final BoolSetting centerDot = add(new BoolSetting("Center Dot", false));

    public CrosshairModule() {
        super("crosshair", "Crosshair", "Your own crosshair: style, size, gap and colour.", Category.PVP, Items.TARGET, false);
        instance = this;
    }

    /** Called from the InGameHud mixin. Returns true if Velocity drew the crosshair (vanilla is skipped). */
    public static boolean render(DrawContext ctx, MinecraftClient client) {
        CrosshairModule m = instance;
        if (m == null || !m.isEnabled()) return false;
        if (client.getDebugHud().shouldShowDebugHud()) return false; // keep vanilla's debug axes
        if (!client.options.getPerspective().isFirstPerson()) return true;
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int c = m.colorValue();
        int s = m.size.getInt(), g = m.gap.getInt(), t = m.thickness.getInt();
        int lo = -(t / 2), hi = lo + t;
        switch (m.style.get()) {
            case "Dot" -> m.rect(ctx, cx - t, cy - t, cx + t + 1, cy + t + 1, c);
            case "Circle" -> {
                int r = s;
                for (int a = 0; a < 360; a += 6) {
                    int px = cx + (int) Math.round(Math.cos(Math.toRadians(a)) * r);
                    int py = cy + (int) Math.round(Math.sin(Math.toRadians(a)) * r);
                    m.rect(ctx, px, py, px + t, py + t, c);
                }
            }
            case "T Shape" -> {
                m.rect(ctx, cx - g - s, cy + lo, cx - g, cy + hi, c);
                m.rect(ctx, cx + g + 1, cy + lo, cx + g + 1 + s, cy + hi, c);
                m.rect(ctx, cx + lo, cy + g + 1, cx + hi, cy + g + 1 + s, c);
            }
            default -> {
                m.rect(ctx, cx - g - s, cy + lo, cx - g, cy + hi, c);
                m.rect(ctx, cx + g + 1, cy + lo, cx + g + 1 + s, cy + hi, c);
                m.rect(ctx, cx + lo, cy - g - s, cx + hi, cy - g, c);
                m.rect(ctx, cx + lo, cy + g + 1, cx + hi, cy + g + 1 + s, c);
            }
        }
        if (m.centerDot.isOn() && !m.style.is("Dot")) m.rect(ctx, cx, cy, cx + 1, cy + 1, c);
        return true;
    }

    private void rect(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        if (outline.isOn()) ctx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xA0000000);
        ctx.fill(x1, y1, x2, y2, color);
    }

    private int colorValue() {
        return switch (color.get()) {
            case "Accent" -> Gfx.accent();
            case "Green" -> 0xFF4CFF7A;
            case "Red" -> 0xFFFF4A5A;
            case "Cyan" -> 0xFF35E6FF;
            case "Chroma" -> Gfx.chroma(0);
            default -> 0xFFFFFFFF;
        };
    }
}
