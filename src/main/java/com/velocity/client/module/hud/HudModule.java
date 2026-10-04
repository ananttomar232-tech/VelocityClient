package com.velocity.client.module.hud;

import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.module.setting.ModeSetting;
import com.velocity.client.module.setting.NumberSetting;
import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;

/**
 * A module that draws something on the HUD. Its position is stored as a fraction of the screen
 * so layouts survive window resizes and GUI scale changes.
 */
public abstract class HudModule extends Module {
    private final float defaultX;
    private final float defaultY;
    public float x;
    public float y;

    protected final NumberSetting scale;
    protected final BoolSetting background;
    protected final ModeSetting textColor;

    protected HudModule(String id, String name, String description, Category category, Item icon,
                        boolean enabledByDefault, float defaultX, float defaultY) {
        super(id, name, description, category, icon, enabledByDefault);
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        this.x = defaultX;
        this.y = defaultY;
        this.scale = add(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
        this.background = add(new BoolSetting("Background", true));
        this.textColor = add(new ModeSetting("Text Color", "White", "White", "Accent", "Chroma"));
    }

    /** Unscaled width of the element in GUI pixels. */
    public abstract int getWidth(MinecraftClient client, boolean editor);

    /** Unscaled height of the element in GUI pixels. */
    public abstract int getHeight(MinecraftClient client, boolean editor);

    /** Draw the element with its top-left corner at 0,0 (the matrix is already translated and scaled). */
    protected abstract void draw(DrawContext ctx, MinecraftClient client, boolean editor);

    /** Some elements have nothing to show (e.g. no potion effects); they are hidden outside the editor. */
    public boolean hasContent(MinecraftClient client) {
        return true;
    }

    public float getScale() {
        return scale.getFloat();
    }

    public void setScale(double value) {
        scale.set(value);
    }

    public int scaledWidth(MinecraftClient client, boolean editor) {
        return Math.round(getWidth(client, editor) * getScale());
    }

    public int scaledHeight(MinecraftClient client, boolean editor) {
        return Math.round(getHeight(client, editor) * getScale());
    }

    public int screenX(MinecraftClient client, int screenWidth, boolean editor) {
        int max = Math.max(0, screenWidth - scaledWidth(client, editor));
        return Math.max(0, Math.min(max, Math.round(x * screenWidth)));
    }

    public int screenY(MinecraftClient client, int screenHeight, boolean editor) {
        int max = Math.max(0, screenHeight - scaledHeight(client, editor));
        return Math.max(0, Math.min(max, Math.round(y * screenHeight)));
    }

    public void setScreenPosition(int px, int py, int screenWidth, int screenHeight) {
        x = screenWidth <= 0 ? 0 : px / (float) screenWidth;
        y = screenHeight <= 0 ? 0 : py / (float) screenHeight;
    }

    public void resetPosition() {
        x = defaultX;
        y = defaultY;
    }

    public void render(DrawContext ctx, MinecraftClient client, int screenWidth, int screenHeight, boolean editor) {
        int px = screenX(client, screenWidth, editor);
        int py = screenY(client, screenHeight, editor);
        ctx.getMatrices().push();
        ctx.getMatrices().translate(px, py, 0);
        ctx.getMatrices().scale(getScale(), getScale(), 1);
        draw(ctx, client, editor);
        ctx.getMatrices().pop();
    }

    protected void drawBackground(DrawContext ctx, int w, int h) {
        if (background.isOn()) Gfx.roundRect(ctx, 0, 0, w, h, 2, 0x80000000);
    }

    /** Colour for HUD text according to the "Text Color" setting. Offset makes chroma text ripple. */
    protected int color(float offset) {
        if (textColor.is("Accent")) return Gfx.accent();
        if (textColor.is("Chroma")) return Gfx.chroma(x + offset);
        return 0xFFFFFFFF;
    }
}
