package com.velocity.client.module.hud;

import com.velocity.client.module.Category;
import com.velocity.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;

/** A HUD element that shows one line of text like "[FPS: 144]". */
public abstract class TextHudModule extends HudModule {
    private static final int PAD_X = 5;
    private static final int HEIGHT = 16;

    protected final BoolSetting brackets;
    protected final BoolSetting showLabel;

    protected TextHudModule(String id, String name, String description, Category category, Item icon,
                            boolean enabledByDefault, float defaultX, float defaultY) {
        super(id, name, description, category, icon, enabledByDefault, defaultX, defaultY);
        this.brackets = add(new BoolSetting("Brackets", false));
        this.showLabel = add(new BoolSetting("Show Label", true));
    }

    /** Short label such as "FPS". */
    protected abstract String label();

    /** Current value; editor is true inside the HUD editor where sample data may be shown. */
    protected abstract String value(MinecraftClient client, boolean editor);

    public String line(MinecraftClient client, boolean editor) {
        String text = showLabel.isOn() ? label() + ": " + value(client, editor) : value(client, editor);
        return brackets.isOn() ? "[" + text + "]" : text;
    }

    @Override
    public int getWidth(MinecraftClient client, boolean editor) {
        return client.textRenderer.getWidth(line(client, editor)) + PAD_X * 2;
    }

    @Override
    public int getHeight(MinecraftClient client, boolean editor) {
        return HEIGHT;
    }

    @Override
    protected void draw(DrawContext ctx, MinecraftClient client, boolean editor) {
        String text = line(client, editor);
        int w = client.textRenderer.getWidth(text) + PAD_X * 2;
        drawBackground(ctx, w, HEIGHT);
        ctx.drawText(client.textRenderer, text, PAD_X, 4, color(0), true);
    }
}
