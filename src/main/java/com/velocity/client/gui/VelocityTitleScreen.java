package com.velocity.client.gui;

import com.velocity.client.VelocityClient;
import com.velocity.client.util.Gfx;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Velocity's main menu: panorama, glowing logo and big friendly buttons. */
public final class VelocityTitleScreen extends BaseScreen {
    /** Set by the "Vanilla Menu" link so the next TitleScreen is not replaced. */
    public static boolean showVanillaOnce = false;

    private static final String[] TIPS = {
            "Press Right Shift in-game to open the Velocity menu",
            "Hold C to zoom, scroll to zoom further",
            "Tap sprint once - Toggle Sprint does the rest",
            "Try the Potato preset if your FPS drops",
            "Drag your HUD around in the HUD Editor",
            "Pick your favourite accent colour in Settings",
            "Dynamic FPS keeps your laptop cool when tabbed out",
    };

    private final String tip = TIPS[new Random().nextInt(TIPS.length)];
    private final Map<String, Float> hover = new HashMap<>();

    public VelocityTitleScreen() {
        super("Velocity Client", null);
    }

    @Override
    protected void draw(DrawContext ctx) {
        renderPanoramaBackground(ctx, delta * 20f);
        float open = openProgress();
        ctx.fillGradient(0, 0, width, height, 0x88080A10, 0xDD080A10);

        int cx = width / 2;
        int logoY = Math.max(16, height / 2 - 92) + Math.round((1 - open) * -12);

        // Logo: big wavy-coloured VELOCITY
        String logo = "VELOCITY";
        float scale = width < 360 ? 3f : 4f;
        int logoW = Math.round(textRenderer.getWidth(logo) * scale);
        int x = cx - logoW / 2;
        for (int i = 0; i < logo.length(); i++) {
            String ch = String.valueOf(logo.charAt(i));
            float wave = (float) Math.sin(System.currentTimeMillis() / 400.0 + i * 0.6) * 1.5f;
            int color = Gfx.mix(Gfx.accent(), 0xFFFFFFFF, 0.25f + 0.25f * (float) Math.sin(System.currentTimeMillis() / 500.0 + i));
            Gfx.scaledText(ctx, textRenderer, ch, x, logoY + Math.round(wave), scale, Gfx.withAlpha(color, Math.round(255 * open)));
            x += Math.round(textRenderer.getWidth(ch) * scale);
        }
        String sub = "C L I E N T";
        Gfx.centered(ctx, textRenderer, sub, cx, logoY + Math.round(9 * scale) + 4, Gfx.withAlpha(0xFFFFFF, Math.round(200 * open)));
        int barW = 60;
        Gfx.roundRect(ctx, cx - barW / 2, logoY + Math.round(9 * scale) + 16, barW, 2, 1, Gfx.accent());

        // Main buttons
        int bw = 190, bh = 20, gap = 5;
        int by = logoY + Math.round(9 * scale) + 28 + Math.round((1 - open) * 16);
        bigButton(ctx, "Singleplayer", cx - bw / 2, by, bw, bh, () -> client.setScreen(new SelectWorldScreen(this)));
        by += bh + gap;
        bigButton(ctx, "Multiplayer", cx - bw / 2, by, bw, bh, () -> client.setScreen(
                client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this)));
        by += bh + gap;
        bigButton(ctx, "⚡ Velocity Mods", cx - bw / 2, by, bw, bh, () -> client.setScreen(new VelocityMenuScreen(this)));
        by += bh + gap;
        int half = (bw - gap) / 2;
        bigButton(ctx, "Options", cx - bw / 2, by, half, bh, () -> client.setScreen(new OptionsScreen(this, client.options)));
        bigButton(ctx, "Quit", cx - bw / 2 + half + gap, by, half, bh, () -> client.scheduleStop());

        // Tip under the buttons
        Gfx.centered(ctx, textRenderer, "Tip: " + tip, cx, by + bh + 12, Gfx.withAlpha(0xC9CEDA, Math.round(220 * open)));

        // Footer
        String user = "Playing as " + client.getSession().getUsername();
        Gfx.text(ctx, textRenderer, user, 6, height - 12, 0xFFB0B6C3);
        String version = "Minecraft " + SharedConstants.getGameVersion().getName() + "  •  " + VelocityClient.NAME + " " + VelocityClient.version();
        Gfx.text(ctx, textRenderer, version, width - textRenderer.getWidth(version) - 6, height - 12, 0xFFB0B6C3);

        String vanilla = "Vanilla Menu";
        int vw = textRenderer.getWidth(vanilla);
        boolean vh = hovered(width - vw - 6, 6, vw, 10);
        Gfx.text(ctx, textRenderer, vanilla, width - vw - 6, 6, vh ? 0xFFFFFFFF : 0xFF8E94A2);
        click(width - vw - 6, 4, vw, 12, () -> {
            showVanillaOnce = true;
            client.setScreen(new TitleScreen());
        });
    }

    private void bigButton(DrawContext ctx, String label, int x, int y, int w, int h, Runnable action) {
        float t = hover.getOrDefault(label, 0f);
        t = Gfx.approach(t, hovered(x, y, w, h) ? 1f : 0f, 16f, delta);
        hover.put(label, t);
        Gfx.roundBox(ctx, x, y, w, h, 4, Gfx.mix(0xC0151821, 0xE0232838, t), Gfx.mix(0x60FFFFFF & 0x40FFFFFF, Gfx.accent(), t));
        int bar = Math.round((w - 8) * t);
        if (bar > 0) Gfx.roundRect(ctx, x + w / 2 - bar / 2, y + h - 3, bar, 2, 1, Gfx.accent());
        Gfx.centered(ctx, textRenderer, label, x + w / 2, y + (h - 8) / 2, Gfx.mix(0xFFDDE1EA, 0xFFFFFFFF, t));
        click(x, y, w, h, action);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        // The title screen has nowhere to go back to.
    }
}
