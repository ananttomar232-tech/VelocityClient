package com.velocity.client.gui;

import com.velocity.client.VelocityConfig;
import com.velocity.client.module.ModuleManager;
import com.velocity.client.module.hud.HudModule;
import com.velocity.client.util.Gfx;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

/** Drag-and-drop HUD layout editor with snapping guides. */
public final class HudEditorScreen extends BaseScreen {
    private static final int SNAP = 4;

    private HudModule dragging;
    private int grabX;
    private int grabY;
    private boolean snappedX;
    private boolean snappedY;

    public HudEditorScreen(Screen parent) {
        super("HUD Editor", parent);
    }

    private List<HudModule> visible() {
        return ModuleManager.hudModules().stream().filter(HudModule::isEnabled).toList();
    }

    private HudModule at(double mx, double my) {
        List<HudModule> list = visible();
        for (int i = list.size() - 1; i >= 0; i--) {
            HudModule hud = list.get(i);
            int x = hud.screenX(client, width, true), y = hud.screenY(client, height, true);
            if (mx >= x && mx < x + hud.scaledWidth(client, true) && my >= y && my < y + hud.scaledHeight(client, true)) return hud;
        }
        return null;
    }

    @Override
    protected void draw(DrawContext ctx) {
        float open = openProgress();
        ctx.fill(0, 0, width, height, Gfx.withAlpha(0x000000, Math.round(90 * open)));

        // Centre guides
        int guide = Gfx.withAlpha(0xFFFFFF, 28);
        ctx.fill(width / 2, 0, width / 2 + 1, height, snappedX && dragging != null ? Gfx.accent() : guide);
        ctx.fill(0, height / 2, width, height / 2 + 1, snappedY && dragging != null ? Gfx.accent() : guide);

        HudModule hover = dragging != null ? dragging : at(mouseX, mouseY);
        for (HudModule hud : visible()) {
            hud.render(ctx, client, width, height, true);
            int x = hud.screenX(client, width, true), y = hud.screenY(client, height, true);
            int w = hud.scaledWidth(client, true), h = hud.scaledHeight(client, true);
            boolean active = hud == hover;
            if (active) ctx.fill(x, y, x + w, y + h, Gfx.withAlpha(Gfx.accent(), 40));
            Gfx.outline(ctx, x - 1, y - 1, w + 2, h + 2, active ? Gfx.accent() : 0x60FFFFFF);
            if (active) {
                String tag = hud.getName() + "  " + Math.round(hud.getScale() * 100) + "%";
                int ty = y > 12 ? y - 11 : y + h + 3;
                Gfx.roundRect(ctx, x - 1, ty - 1, textRenderer.getWidth(tag) + 6, 10, 2, Gfx.accent());
                Gfx.text(ctx, textRenderer, tag, x + 2, ty, 0xFFFFFFFF);
            }
        }

        // Toolbar
        int tw = 220, th = 50;
        int tx = (width - tw) / 2, ty = height / 2 - th / 2 + Math.round((1 - open) * 10);
        if (dragging == null) {
            Gfx.roundBox(ctx, tx, ty, tw, th, 6, 0xE6151821, Gfx.STROKE);
            Gfx.centered(ctx, textRenderer, "Drag to move • Scroll to resize", width / 2, ty + 7, 0xFFFFFFFF);
            Gfx.centered(ctx, textRenderer, "Right-click a mod for its options", width / 2, ty + 17, Gfx.MUTED);
            button(ctx, tx + 8, ty + 30, 98, 14, "Mods", 0xFF2A3042, () -> client.setScreen(new VelocityMenuScreen(this)));
            button(ctx, tx + tw - 106, ty + 30, 98, 14, "Done", Gfx.accent(), this::close);
        }
        if (visible().isEmpty()) {
            Gfx.centered(ctx, textRenderer, "No HUD mods enabled - turn some on in the Mods menu.", width / 2, ty - 16, 0xFFF5C542);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        HudModule hud = at(mx, my);
        if (hud == null) return false;
        if (button == 1) {
            client.setScreen(new VelocityMenuScreen(this, hud));
            return true;
        }
        if (button == 0) {
            dragging = hud;
            grabX = (int) mx - hud.screenX(client, width, true);
            grabY = (int) my - hud.screenY(client, height, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging == null) return false;
        int w = dragging.scaledWidth(client, true), h = dragging.scaledHeight(client, true);
        int x = (int) mx - grabX, y = (int) my - grabY;

        snappedX = snappedY = false;
        if (Math.abs(x + w / 2 - width / 2) < SNAP) { x = width / 2 - w / 2; snappedX = true; }
        if (Math.abs(y + h / 2 - height / 2) < SNAP) { y = height / 2 - h / 2; snappedY = true; }
        if (x < SNAP) x = 0;
        if (y < SNAP) y = 0;
        if (x + w > width - SNAP) x = width - w;
        if (y + h > height - SNAP) y = height - h;

        // Snap to the edges of other elements so things line up neatly.
        for (HudModule other : visible()) {
            if (other == dragging) continue;
            int ox = other.screenX(client, width, true), oy = other.screenY(client, height, true);
            int ow = other.scaledWidth(client, true), oh = other.scaledHeight(client, true);
            if (Math.abs(x - ox) < SNAP) x = ox;
            if (Math.abs(y - (oy + oh + 2)) < SNAP) y = oy + oh + 2;
            if (Math.abs(x + w - (ox + ow)) < SNAP) x = ox + ow - w;
        }
        dragging.setScreenPosition(x, y, width, height);
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) {
            dragging = null;
            VelocityConfig.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        HudModule hud = at(mx, my);
        if (hud == null) return false;
        hud.setScale(hud.getScale() + (vertical > 0 ? 0.1 : -0.1));
        VelocityConfig.save();
        return true;
    }
}
