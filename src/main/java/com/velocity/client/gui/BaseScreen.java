package com.velocity.client.gui;

import com.velocity.client.VelocityConfig;
import com.velocity.client.util.Gfx;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tiny immediate-mode UI base: widgets are drawn every frame and register a click area while drawing.
 * It keeps the menu code short and everything animates without widget bookkeeping.
 */
public abstract class BaseScreen extends Screen {
    protected final Screen parent;
    private final List<Region> regions = new ArrayList<>();
    private final List<Region> lastRegions = new ArrayList<>();
    private int clipX1 = Integer.MIN_VALUE, clipY1 = Integer.MIN_VALUE, clipX2 = Integer.MAX_VALUE, clipY2 = Integer.MAX_VALUE;
    protected int mouseX;
    protected int mouseY;
    protected float delta;
    private long lastFrame = System.nanoTime();
    protected final long openedAt = System.currentTimeMillis();

    private record Region(int x1, int y1, int x2, int y2, Consumer<Integer> action) {}

    protected BaseScreen(String title, Screen parent) {
        super(Text.literal(title));
        this.parent = parent;
    }

    /** Draw the whole screen. Click regions must be registered here via {@link #region}. */
    protected abstract void draw(DrawContext ctx);

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        long now = System.nanoTime();
        this.delta = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        this.lastFrame = now;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        regions.clear();
        draw(ctx);
        lastRegions.clear();
        lastRegions.addAll(regions);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Velocity draws its own lightweight background (no blur pass = more FPS on integrated graphics).
    }

    /** Out of a world there is nothing behind the menu, so show the rotating panorama like the main menu. */
    protected void drawBackdrop(DrawContext ctx) {
        if (client != null && client.world == null) {
            renderPanoramaBackground(ctx, delta * 20f);
            ctx.fill(0, 0, width, height, 0x66080A10);
        }
    }

    /** 0..1 progress of the open animation. */
    protected float openProgress() {
        if (!VelocityConfig.animations) return 1f;
        return Gfx.easeOutCubic((System.currentTimeMillis() - openedAt) / 220f);
    }

    protected void clip(DrawContext ctx, int x1, int y1, int x2, int y2) {
        clipX1 = x1;
        clipY1 = y1;
        clipX2 = x2;
        clipY2 = y2;
        ctx.enableScissor(x1, y1, x2, y2);
    }

    protected void unclip(DrawContext ctx) {
        clipX1 = clipY1 = Integer.MIN_VALUE;
        clipX2 = clipY2 = Integer.MAX_VALUE;
        ctx.disableScissor();
    }

    protected boolean hovered(int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h
                && mouseX >= clipX1 && mouseX < clipX2 && mouseY >= clipY1 && mouseY < clipY2;
    }

    /** Registers a clickable area; the action receives the mouse button. */
    protected void region(int x, int y, int w, int h, Consumer<Integer> action) {
        int x1 = Math.max(x, clipX1), y1 = Math.max(y, clipY1);
        int x2 = Math.min(x + w, clipX2), y2 = Math.min(y + h, clipY2);
        if (x2 > x1 && y2 > y1) regions.add(new Region(x1, y1, x2, y2, action));
    }

    protected void click(int x, int y, int w, int h, Runnable action) {
        region(x, y, w, h, button -> { if (button == 0) action.run(); });
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        // Last registered = drawn on top, so walk backwards.
        for (int i = lastRegions.size() - 1; i >= 0; i--) {
            Region r = lastRegions.get(i);
            if (mx >= r.x1 && mx < r.x2 && my >= r.y1 && my < r.y2) {
                r.action.accept(button);
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- widgets

    /** Rounded button. Returns nothing; registers its own click. */
    protected void button(DrawContext ctx, int x, int y, int w, int h, String label, int color, Runnable action) {
        boolean hover = hovered(x, y, w, h);
        int bg = hover ? Gfx.mix(color, 0xFFFFFFFF, 0.15f) : color;
        Gfx.roundRect(ctx, x, y, w, h, 3, bg);
        Gfx.centered(ctx, textRenderer, label, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
        click(x, y, w, h, action);
    }

    /** Pill shaped on/off switch. */
    protected void toggle(DrawContext ctx, int x, int y, boolean on, Runnable action) {
        int w = 22, h = 11;
        Gfx.roundRect(ctx, x, y, w, h, 5, on ? Gfx.accent() : 0xFF3A4052);
        int knob = on ? x + w - 10 : x + 1;
        Gfx.roundRect(ctx, knob, y + 1, 9, 9, 4, 0xFFFFFFFF);
        click(x - 2, y - 2, w + 4, h + 4, action);
    }

    protected boolean shouldAnimate() {
        return VelocityConfig.animations;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        VelocityConfig.save();
        if (client != null) client.setScreen(parent);
    }
}
