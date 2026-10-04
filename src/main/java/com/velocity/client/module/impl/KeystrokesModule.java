package com.velocity.client.module.impl;

import com.velocity.client.module.Category;
import com.velocity.client.module.hud.HudModule;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.util.ClickCounter;
import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;

public final class KeystrokesModule extends HudModule {
    private static final int KEY = 22;
    private static final int GAP = 2;

    private final BoolSetting showMouse = add(new BoolSetting("Show Mouse Buttons", true));
    private final BoolSetting showSpace = add(new BoolSetting("Show Space Bar", true));
    private final BoolSetting showCps = add(new BoolSetting("CPS On Mouse Buttons", true));
    private final float[] glow = new float[7];
    private long lastFrame = System.nanoTime();

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes", "Shows WASD, mouse and jump presses with smooth highlights.", Category.PVP, Items.TRIPWIRE_HOOK, false, 0.99f, 0.62f);
    }

    @Override
    public int getWidth(MinecraftClient client, boolean editor) {
        return KEY * 3 + GAP * 2;
    }

    @Override
    public int getHeight(MinecraftClient client, boolean editor) {
        int h = KEY * 2 + GAP;
        if (showMouse.isOn()) h += GAP + KEY;
        if (showSpace.isOn()) h += GAP + 10;
        return h;
    }

    @Override
    protected void draw(DrawContext ctx, MinecraftClient client, boolean editor) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;

        var o = client.options;
        int full = getWidth(client, editor);
        int half = (full - GAP) / 2;

        key(ctx, client, 0, o.forwardKey, "W", KEY + GAP, 0, KEY, KEY, dt);
        key(ctx, client, 1, o.leftKey, "A", 0, KEY + GAP, KEY, KEY, dt);
        key(ctx, client, 2, o.backKey, "S", KEY + GAP, KEY + GAP, KEY, KEY, dt);
        key(ctx, client, 3, o.rightKey, "D", (KEY + GAP) * 2, KEY + GAP, KEY, KEY, dt);
        int y = (KEY + GAP) * 2;
        if (showMouse.isOn()) {
            String l = showCps.isOn() ? ClickCounter.left() + " CPS" : "LMB";
            String r = showCps.isOn() ? ClickCounter.right() + " CPS" : "RMB";
            key(ctx, client, 4, o.attackKey, l, 0, y, half, KEY, dt);
            key(ctx, client, 5, o.useKey, r, half + GAP, y, full - half - GAP, KEY, dt);
            y += KEY + GAP;
        }
        if (showSpace.isOn()) {
            key(ctx, client, 6, o.jumpKey, "", 0, y, full, 10, dt);
            ctx.fill(full / 2 - 10, y + 4, full / 2 + 10, y + 6, Gfx.mix(color(0.2f), 0xFF000000, glow[6]));
        }
    }

    private void key(DrawContext ctx, MinecraftClient client, int index, KeyBinding binding, String label,
                     int x, int y, int w, int h, float dt) {
        boolean down = binding.isPressed();
        glow[index] = Gfx.approach(glow[index], down ? 1f : 0f, 18f, dt);
        int bg = Gfx.mix(background.isOn() ? 0x80000000 : 0x00000000, 0xD0FFFFFF, glow[index]);
        Gfx.roundRect(ctx, x, y, w, h, 2, bg);
        if (!label.isEmpty()) {
            int tw = client.textRenderer.getWidth(label);
            int fg = Gfx.mix(color(index * 0.05f), 0xFF111111, glow[index]);
            ctx.drawText(client.textRenderer, label, x + (w - tw) / 2, y + (h - 8) / 2, fg, glow[index] < 0.5f);
        }
    }
}
