package com.velocity.client.gui;

import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Small toasts that slide in from the bottom-right, e.g. "Zoom enabled". */
public final class Notifications {
    private static final long LIFE = 1800;
    private static final List<Toast> TOASTS = new ArrayList<>();

    private record Toast(String text, int color, long start) {}

    private Notifications() {}

    public static void push(String text, int color) {
        TOASTS.removeIf(t -> t.text.equals(text));
        TOASTS.add(new Toast(text, color, System.currentTimeMillis()));
        if (TOASTS.size() > 4) TOASTS.remove(0);
    }

    public static void render(DrawContext ctx, int width, int height) {
        if (TOASTS.isEmpty()) return;
        var tr = MinecraftClient.getInstance().textRenderer;
        long now = System.currentTimeMillis();
        int y = height - 26;
        for (Iterator<Toast> it = TOASTS.iterator(); it.hasNext(); ) {
            if (now - it.next().start > LIFE + 300) it.remove();
        }
        for (int i = TOASTS.size() - 1; i >= 0; i--) {
            Toast t = TOASTS.get(i);
            long age = now - t.start;
            float in = Gfx.easeOutCubic(age / 220f);
            float out = age > LIFE ? Gfx.easeOutCubic((age - LIFE) / 300f) : 0f;
            int w = tr.getWidth(t.text) + 22;
            int x = width - 6 - w + Math.round((1 - in + out) * (w + 10));
            Gfx.roundBox(ctx, x, y, w, 18, 4, 0xF0151821, Gfx.withAlpha(t.color, 200));
            Gfx.roundRect(ctx, x + 6, y + 6, 6, 6, 3, t.color);
            ctx.drawText(tr, t.text, x + 16, y + 5, 0xFFFFFFFF, true);
            y -= 22;
        }
    }
}
