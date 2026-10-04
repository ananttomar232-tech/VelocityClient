package com.velocity.client.gui;

import com.velocity.client.VelocityClient;
import com.velocity.client.VelocityConfig;
import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import com.velocity.client.module.ModuleManager;
import com.velocity.client.module.hud.HudModule;
import com.velocity.client.module.impl.DynamicFpsModule;
import com.velocity.client.module.impl.LowSpecMenusModule;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.module.setting.ModeSetting;
import com.velocity.client.module.setting.NumberSetting;
import com.velocity.client.module.setting.Setting;
import com.velocity.client.util.FpsTracker;
import com.velocity.client.util.Gfx;
import com.velocity.client.util.PerformancePresets;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleConsumer;

/** The main Velocity menu (Right Shift): mods grid, per-mod options, performance centre and client settings. */
public final class VelocityMenuScreen extends BaseScreen {
    private enum Page { MODS, PERFORMANCE, SETTINGS }

    private record PerfMod(String modId, String slug, String name, String description) {}

    private static final List<PerfMod> PERF_MODS = List.of(
            new PerfMod("sodium", "sodium", "Sodium", "Modern renderer - the biggest FPS boost"),
            new PerfMod("lithium", "lithium", "Lithium", "Faster game logic, smoother singleplayer"),
            new PerfMod("ferritecore", "ferrite-core", "FerriteCore", "Uses a lot less RAM"),
            new PerfMod("immediatelyfast", "immediatelyfast", "ImmediatelyFast", "Faster HUD, text and item rendering"),
            new PerfMod("entityculling", "entityculling", "EntityCulling", "Skips drawing entities you can't see"));

    private static Page lastPage = Page.MODS;

    private Page page = lastPage;
    private Category filter = null;
    private Module selected = null;
    private TextFieldWidget search;

    private float scroll;
    private float targetScroll;
    private int maxScroll;

    private final Map<Object, Float> hoverAnim = new HashMap<>();
    private DoubleConsumer activeSlider;
    private int activeSliderX;
    private int activeSliderW;

    public VelocityMenuScreen(Screen parent) {
        super("Velocity", parent);
    }

    public VelocityMenuScreen(Screen parent, Module openOptionsFor) {
        this(parent);
        this.page = Page.MODS;
        this.selected = openOptionsFor;
    }

    @Override
    protected void init() {
        String previous = search == null ? "" : search.getText();
        search = new TextFieldWidget(textRenderer, 0, 0, 100, 14, Text.literal("Search"));
        search.setDrawsBackground(false);
        search.setMaxLength(32);
        search.setText(previous);
        search.setPlaceholder(Text.literal("Search mods...").formatted(Formatting.DARK_GRAY));
        search.setChangedListener(s -> targetScroll = 0);
        addSelectableChild(search);
    }

    // ------------------------------------------------------------------ layout helpers

    private int pw() { return Math.min(width - 16, 460); }
    private int ph() { return Math.min(height - 16, 290); }
    private int px() { return (width - pw()) / 2; }
    private int py() { return (height - ph()) / 2 + Math.round((1 - openProgress()) * 14); }

    private float anim(Object key, boolean target, float speed) {
        float v = hoverAnim.getOrDefault(key, target ? 1f : 0f);
        v = shouldAnimate() ? Gfx.approach(v, target ? 1f : 0f, speed, delta) : (target ? 1f : 0f);
        hoverAnim.put(key, v);
        return v;
    }

    private void switchPage(Page next) {
        page = next;
        lastPage = next;
        selected = null;
        scroll = targetScroll = 0;
    }

    // ------------------------------------------------------------------ rendering

    @Override
    protected void draw(DrawContext ctx) {
        float open = openProgress();
        ctx.fill(0, 0, width, height, Gfx.withAlpha(0x090B10, Math.round(175 * open)));

        int px = px(), py = py(), pw = pw(), ph = ph();
        Gfx.roundBox(ctx, px, py, pw, ph, 6, Gfx.PANEL, Gfx.STROKE);
        Gfx.roundRect(ctx, px + 6, py, pw - 12, 2, 1, Gfx.accent());

        drawHeader(ctx, px, py, pw);
        ctx.fill(px + 8, py + 28, px + pw - 8, py + 29, Gfx.STROKE);

        int cx = px + 10, cy = py + 34, cw = pw - 20, ch = ph - 42;
        scroll = shouldAnimate() ? Gfx.approach(scroll, targetScroll, 22f, delta) : targetScroll;

        search.visible = page == Page.MODS && selected == null;
        if (!search.visible && search.isFocused()) setFocused(null);

        switch (page) {
            case MODS -> {
                if (selected != null) drawOptions(ctx, cx, cy, cw, ch);
                else drawMods(ctx, cx, cy, cw, ch);
            }
            case PERFORMANCE -> drawPerformance(ctx, cx, cy, cw, ch);
            case SETTINGS -> drawSettings(ctx, cx, cy, cw, ch);
        }

        String hint = "Right Shift: menu  •  " + VelocityClient.ZOOM_KEY.getBoundKeyLocalizedText().getString() + ": zoom";
        Gfx.text(ctx, textRenderer, hint, 6, height - 11, Gfx.withAlpha(Gfx.MUTED, Math.round(255 * open)));
        String ver = VelocityClient.NAME + " " + VelocityClient.version();
        Gfx.text(ctx, textRenderer, ver, width - textRenderer.getWidth(ver) - 6, height - 11, Gfx.withAlpha(Gfx.MUTED, Math.round(255 * open)));
    }

    private void drawHeader(DrawContext ctx, int px, int py, int pw) {
        // Logo
        Gfx.roundRect(ctx, px + 9, py + 7, 15, 15, 4, Gfx.accent());
        Gfx.centered(ctx, textRenderer, "V", px + 17, py + 11, 0xFFFFFFFF);
        ctx.drawText(textRenderer, Text.literal("VELOCITY").formatted(Formatting.BOLD), px + 29, py + 11, 0xFFFFFFFF, true);

        // Close button
        int closeX = px + pw - 22;
        boolean closeHover = hovered(closeX, py + 7, 15, 15);
        Gfx.roundRect(ctx, closeX, py + 7, 15, 15, 4, closeHover ? Gfx.RED : 0xFF262B39);
        Gfx.centered(ctx, textRenderer, "✕", closeX + 8, py + 11, 0xFFFFFFFF);
        click(closeX, py + 7, 15, 15, this::close);

        // Tabs, right-aligned before the close button
        String[] labels = {"MODS", "HUD EDITOR", "PERFORMANCE", "SETTINGS"};
        int x = closeX - 6;
        for (int i = labels.length - 1; i >= 0; i--) x -= textRenderer.getWidth(labels[i]) + 14 + 3;
        for (int i = 0; i < labels.length; i++) {
            String label = labels[i];
            int w = textRenderer.getWidth(label) + 14;
            boolean active = (i == 0 && page == Page.MODS) || (i == 2 && page == Page.PERFORMANCE) || (i == 3 && page == Page.SETTINGS);
            float h = anim("tab" + i, hovered(x, py + 7, w, 15) || active, 16f);
            int bg = active ? Gfx.accent() : Gfx.mix(0x00262B39, 0xFF262B39, h);
            Gfx.roundRect(ctx, x, py + 7, w, 15, 4, bg);
            Gfx.centered(ctx, textRenderer, label, x + w / 2, py + 11, active ? 0xFFFFFFFF : Gfx.mix(Gfx.MUTED, 0xFFFFFFFF, h));
            final int index = i;
            click(x, py + 7, w, 15, () -> {
                switch (index) {
                    case 0 -> switchPage(Page.MODS);
                    case 1 -> client.setScreen(new HudEditorScreen(this));
                    case 2 -> switchPage(Page.PERFORMANCE);
                    default -> switchPage(Page.SETTINGS);
                }
            });
            x += w + 3;
        }
    }

    // ------------------------------------------------------------------ MODS page

    private List<Module> visibleModules() {
        String query = search == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
        List<Module> list = new ArrayList<>();
        for (Module m : ModuleManager.all()) {
            if (filter != null && m.getCategory() != filter) continue;
            if (!query.isEmpty() && !m.getName().toLowerCase(Locale.ROOT).contains(query)
                    && !m.getDescription().toLowerCase(Locale.ROOT).contains(query)) continue;
            list.add(m);
        }
        return list;
    }

    private void drawMods(DrawContext ctx, int cx, int cy, int cw, int ch) {
        // Category chips
        int x = cx;
        Category[] cats = Category.values();
        for (int i = -1; i < cats.length; i++) {
            Category cat = i < 0 ? null : cats[i];
            String label = cat == null ? "ALL" : cat.label;
            int w = textRenderer.getWidth(label) + 12;
            boolean active = filter == cat;
            float h = anim("chip" + i, hovered(x, cy, w, 13), 16f);
            int bg = active ? Gfx.accent() : Gfx.mix(0xFF1E2230, 0xFF2A3042, h);
            Gfx.roundRect(ctx, x, cy, w, 13, 6, bg);
            Gfx.centered(ctx, textRenderer, label, x + w / 2, cy + 3, active ? 0xFFFFFFFF : Gfx.MUTED);
            click(x, cy, w, 13, () -> { filter = cat; targetScroll = 0; });
            x += w + 4;
        }

        // Search box
        int sw = Math.max(60, Math.min(120, cx + cw - x - 4));
        int sx = cx + cw - sw;
        Gfx.roundBox(ctx, sx, cy - 1, sw, 15, 4, 0xFF10131A, search.isFocused() ? Gfx.accent() : Gfx.STROKE);
        search.setX(sx + 5);
        search.setY(cy + 3);
        search.setWidth(sw - 10);
        search.render(ctx, mouseX, mouseY, delta);

        // Cards grid
        int gy = cy + 20, gh = ch - 20;
        List<Module> modules = visibleModules();
        int gap = 6;
        int cols = Math.max(2, (cw + gap) / (100 + gap));
        int cardW = (cw - gap * (cols - 1)) / cols;
        int cardH = 80;
        int rows = (modules.size() + cols - 1) / cols;
        int contentH = rows * (cardH + gap) - gap;
        maxScroll = Math.max(0, contentH - gh);
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll));

        if (modules.isEmpty()) {
            Gfx.centered(ctx, textRenderer, "No mods match \"" + search.getText() + "\"", cx + cw / 2, gy + 30, Gfx.MUTED);
            return;
        }

        clip(ctx, cx - 2, gy, cx + cw + 2, gy + gh);
        for (int i = 0; i < modules.size(); i++) {
            int col = i % cols, row = i / cols;
            int x0 = cx + col * (cardW + gap);
            int y0 = gy + row * (cardH + gap) - Math.round(scroll);
            if (y0 + cardH < gy || y0 > gy + gh) continue;
            drawCard(ctx, modules.get(i), x0, y0, cardW, cardH);
        }
        unclip(ctx);
        drawScrollbar(ctx, cx + cw + 3, gy, gh, contentH);
    }

    private void drawCard(DrawContext ctx, Module m, int x, int y, int w, int h) {
        boolean hover = hovered(x, y, w, h);
        float hv = anim(m, hover, 14f);
        float on = anim(m.getId() + ":on", m.isEnabled(), 12f);

        int border = Gfx.mix(Gfx.STROKE, Gfx.withAlpha(Gfx.accent(), 200), on * 0.8f);
        Gfx.roundBox(ctx, x, y, w, h, 5, Gfx.mix(Gfx.CARD, Gfx.CARD_HOVER, hv), border);

        // Whole card: left click toggles, right click opens options
        region(x, y, w, h, button -> {
            if (button == 1) openOptions(m);
            else if (button == 0) m.toggle();
            VelocityConfig.save();
        });

        int lift = Math.round(hv * 2);
        Gfx.item(ctx, m.getIcon(), x + w / 2 - 12, y + 6 - lift, 1.5f);
        String name = m.getName();
        if (textRenderer.getWidth(name) > w - 8) name = textRenderer.trimToWidth(name, w - 14) + "..";
        Gfx.centered(ctx, textRenderer, name, x + w / 2, y + 34, 0xFFFFFFFF);

        int bx = x + 5, bw = w - 10;
        boolean optHover = hovered(bx, y + 48, bw, 12);
        Gfx.roundRect(ctx, bx, y + 48, bw, 12, 3, optHover ? 0xFF353C52 : 0xFF2A3042);
        Gfx.scaledCentered(ctx, textRenderer, "OPTIONS", x + w / 2, y + 51, 0.75f, optHover ? 0xFFFFFFFF : 0xFFB8BFCE);
        click(bx, y + 48, bw, 12, () -> openOptions(m));

        int toggleColor = Gfx.mix(Gfx.RED, Gfx.GREEN, on);
        boolean togHover = hovered(bx, y + 63, bw, 12);
        Gfx.roundRect(ctx, bx, y + 63, bw, 12, 3, togHover ? Gfx.mix(toggleColor, 0xFFFFFFFF, 0.15f) : toggleColor);
        Gfx.scaledCentered(ctx, textRenderer, m.isEnabled() ? "ENABLED" : "DISABLED", x + w / 2, y + 66, 0.75f, 0xFFFFFFFF);
        click(bx, y + 63, bw, 12, () -> { m.toggle(); VelocityConfig.save(); });
    }

    private void openOptions(Module m) {
        selected = m;
        scroll = targetScroll = 0;
        setFocused(null);
    }

    private void drawScrollbar(DrawContext ctx, int x, int y, int h, int contentH) {
        if (contentH <= h) return;
        int barH = Math.max(16, h * h / contentH);
        int barY = y + Math.round((h - barH) * (scroll / Math.max(1f, maxScroll)));
        ctx.fill(x, y, x + 2, y + h, 0x40FFFFFF);
        ctx.fill(x, barY, x + 2, barY + barH, Gfx.accent());
    }

    // ------------------------------------------------------------------ OPTIONS page

    private void drawOptions(DrawContext ctx, int cx, int cy, int cw, int ch) {
        Module m = selected;
        button(ctx, cx, cy, 44, 14, "← Back", 0xFF2A3042, () -> { selected = null; scroll = targetScroll = 0; });

        Gfx.item(ctx, m.getIcon(), cx + 52, cy - 2, 1f);
        ctx.drawText(textRenderer, Text.literal(m.getName()).formatted(Formatting.BOLD), cx + 72, cy, 0xFFFFFFFF, true);
        Gfx.text(ctx, textRenderer, m.getDescription(), cx + 72, cy + 10, Gfx.MUTED);
        Gfx.text(ctx, textRenderer, m.isEnabled() ? "Enabled" : "Disabled", cx + cw - 60, cy + 2, m.isEnabled() ? Gfx.GREEN : Gfx.RED);
        toggle(ctx, cx + cw - 24, cy + 1, m.isEnabled(), () -> { m.toggle(); VelocityConfig.save(); });

        int top = cy + 26, viewH = ch - 26;
        ctx.fill(cx, top - 4, cx + cw, top - 3, Gfx.STROKE);

        List<Setting<?>> settings = m.getSettings();
        int rowH = 22;
        int extra = m instanceof HudModule ? 1 : 0;
        int contentH = (settings.size() + extra + 1) * rowH;
        maxScroll = Math.max(0, contentH - viewH);
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll));

        clip(ctx, cx - 2, top, cx + cw + 2, top + viewH);
        int y = top - Math.round(scroll);
        if (settings.isEmpty()) {
            Gfx.text(ctx, textRenderer, "This mod has no extra options - just switch it on!", cx + 4, y + 6, Gfx.MUTED);
            y += rowH;
        }
        for (Setting<?> s : settings) {
            drawSettingRow(ctx, s, cx, y, cw, rowH);
            y += rowH;
        }
        if (m instanceof HudModule hud) {
            button(ctx, cx, y + 3, 110, 15, "Edit Position", Gfx.accent(), () -> client.setScreen(new HudEditorScreen(this)));
            button(ctx, cx + 116, y + 3, 110, 15, "Reset Position", 0xFF2A3042, () -> { hud.resetPosition(); VelocityConfig.save(); });
            y += rowH;
        }
        button(ctx, cx, y + 3, 110, 15, "Reset Options", 0xFF2A3042, () -> {
            for (Setting<?> s : settings) s.reset();
            VelocityConfig.save();
        });
        unclip(ctx);
        drawScrollbar(ctx, cx + cw + 3, top, viewH, contentH);
    }

    private void drawSettingRow(DrawContext ctx, Setting<?> s, int x, int y, int w, int h) {
        Gfx.roundRect(ctx, x, y + 1, w, h - 2, 3, hovered(x, y + 1, w, h - 2) ? 0xFF222737 : 0xFF1B1F2B);
        Gfx.text(ctx, textRenderer, s.getName(), x + 7, y + (h - 8) / 2, 0xFFFFFFFF);
        int right = x + w - 7;
        if (s instanceof BoolSetting b) {
            toggle(ctx, right - 22, y + (h - 11) / 2, b.isOn(), () -> { b.toggle(); VelocityConfig.save(); });
        } else if (s instanceof NumberSetting n) {
            int sw = Math.min(120, w / 3);
            int sx = right - sw;
            Gfx.text(ctx, textRenderer, n.display(), sx - 6 - textRenderer.getWidth(n.display()), y + (h - 8) / 2, Gfx.accent());
            slider(ctx, sx, y + h / 2, sw, n.progress(), p -> n.setFromProgress(p));
        } else if (s instanceof ModeSetting mode) {
            int bw = 92;
            int bx = right - bw;
            boolean hover = hovered(bx, y + 4, bw, h - 8);
            Gfx.roundRect(ctx, bx, y + 4, bw, h - 8, 3, hover ? 0xFF353C52 : 0xFF2A3042);
            Gfx.text(ctx, textRenderer, "‹", bx + 5, y + (h - 8) / 2, Gfx.MUTED);
            Gfx.text(ctx, textRenderer, "›", bx + bw - 9, y + (h - 8) / 2, Gfx.MUTED);
            Gfx.centered(ctx, textRenderer, mode.get(), bx + bw / 2, y + (h - 8) / 2, 0xFFFFFFFF);
            region(bx, y + 4, bw, h - 8, button -> {
                boolean back = button == 1 || mouseX < bx + bw / 3;
                mode.cycle(back ? -1 : 1);
                VelocityConfig.save();
            });
        }
    }

    /** Horizontal slider centred on y. The setter receives 0..1. */
    private void slider(DrawContext ctx, int x, int y, int w, double progress, DoubleConsumer setter) {
        int fill = x + (int) Math.round(w * Math.max(0, Math.min(1, progress)));
        Gfx.roundRect(ctx, x, y - 2, w, 4, 2, 0xFF3A4052);
        Gfx.roundRect(ctx, x, y - 2, fill - x, 4, 2, Gfx.accent());
        boolean hover = hovered(x - 3, y - 6, w + 6, 12);
        int knob = hover || activeSlider == setter ? 9 : 7;
        Gfx.roundRect(ctx, fill - knob / 2, y - knob / 2, knob, knob, knob / 2, 0xFFFFFFFF);
        region(x - 3, y - 6, w + 6, 12, button -> {
            activeSlider = setter;
            activeSliderX = x;
            activeSliderW = w;
            setter.accept((mouseX - x) / (double) w);
        });
    }

    // ------------------------------------------------------------------ PERFORMANCE page

    private void drawPerformance(DrawContext ctx, int cx, int cy, int cw, int ch) {
        int contentH = 330;
        maxScroll = Math.max(0, contentH - ch);
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll));
        clip(ctx, cx - 2, cy, cx + cw + 2, cy + ch);
        int y = cy - Math.round(scroll);

        // Live stats
        int boxW = 110;
        Gfx.roundBox(ctx, cx, y, boxW, 58, 5, Gfx.CARD, Gfx.STROKE);
        int fps = client.getCurrentFps();
        int fpsColor = fps >= 60 ? Gfx.GREEN : fps >= 30 ? 0xFFF5C542 : Gfx.RED;
        Gfx.text(ctx, textRenderer, "FPS", cx + 8, y + 7, Gfx.MUTED);
        Gfx.scaledText(ctx, textRenderer, Integer.toString(fps), cx + 8, y + 18, 3f, fpsColor);
        Gfx.text(ctx, textRenderer, "avg " + FpsTracker.average() + "  max " + FpsTracker.max(), cx + 8, y + 45, Gfx.MUTED);

        int gx = cx + boxW + 6, gw = cw - boxW - 6;
        Gfx.roundBox(ctx, gx, y, gw, 58, 5, Gfx.CARD, Gfx.STROKE);
        Gfx.text(ctx, textRenderer, "Last minute", gx + 8, y + 7, Gfx.MUTED);
        drawGraph(ctx, gx + 8, y + 18, gw - 16, 34);

        y += 66;
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        Gfx.text(ctx, textRenderer, "Memory  " + used + " / " + max + " MB", cx, y, 0xFFFFFFFF);
        int barW = cw - 150;
        int bx = cx + 145;
        Gfx.roundRect(ctx, bx, y + 2, barW, 5, 2, 0xFF2A3042);
        Gfx.roundRect(ctx, bx, y + 2, (int) (barW * Math.min(1.0, used / (double) Math.max(1, max))), 5, 2, Gfx.accent());
        y += 12;
        if (max < 3000) {
            Gfx.text(ctx, textRenderer, "Tip: give Minecraft 4 GB RAM in your launcher (you have 16 GB) to stop lag spikes.", cx, y, 0xFFF5C542);
        } else {
            Gfx.text(ctx, textRenderer, "RAM allocation looks good.", cx, y, Gfx.GREEN);
        }

        // Presets
        y += 16;
        section(ctx, "ONE-CLICK PRESETS", cx, y);
        y += 12;
        int gap = 6;
        int pw = (cw - gap * 2) / 3;
        for (int i = 0; i < PerformancePresets.NAMES.length; i++) {
            String name = PerformancePresets.NAMES[i];
            int x = cx + i * (pw + gap);
            boolean active = name.equals(VelocityConfig.preset);
            float hv = anim("preset" + i, hovered(x, y, pw, 40), 14f);
            Gfx.roundBox(ctx, x, y, pw, 40, 5, Gfx.mix(Gfx.CARD, Gfx.CARD_HOVER, hv), active ? Gfx.accent() : Gfx.STROKE);
            ctx.drawText(textRenderer, Text.literal(name).formatted(Formatting.BOLD), x + 7, y + 7, active ? Gfx.accent() : 0xFFFFFFFF, true);
            if (i == 1) Gfx.scaledText(ctx, textRenderer, "RECOMMENDED", x + pw - 4 - Math.round(textRenderer.getWidth("RECOMMENDED") * 0.6f), y + 8, 0.6f, Gfx.GREEN);
            Gfx.scaledText(ctx, textRenderer, PerformancePresets.DESCRIPTIONS[i], x + 7, y + 22, 0.75f, Gfx.MUTED);
            click(x, y, pw, 40, () -> PerformancePresets.apply(client, name));
        }

        // Boosters
        y += 50;
        section(ctx, "FPS BOOSTERS", cx, y);
        y += 12;
        moduleRow(ctx, ModuleManager.get(DynamicFpsModule.class), cx, y, cw);
        y += 22;
        moduleRow(ctx, ModuleManager.get(LowSpecMenusModule.class), cx, y, cw);
        y += 24;
        button(ctx, cx, y, 140, 15, "Video Settings", 0xFF2A3042, () -> client.setScreen(new VideoOptionsScreen(this, client, client.options)));

        // Recommended performance mods
        y += 24;
        section(ctx, "RECOMMENDED FPS MODS", cx, y);
        Gfx.scaledText(ctx, textRenderer, "the Velocity installer adds these for you", cx + 115, y + 1, 0.75f, Gfx.MUTED);
        y += 12;
        for (PerfMod mod : PERF_MODS) {
            boolean installed = FabricLoader.getInstance().isModLoaded(mod.modId());
            Gfx.roundRect(ctx, cx, y, cw, 18, 3, 0xFF1B1F2B);
            Gfx.text(ctx, textRenderer, mod.name(), cx + 7, y + 5, 0xFFFFFFFF);
            Gfx.text(ctx, textRenderer, mod.description(), cx + 95, y + 5, Gfx.MUTED);
            if (installed) {
                Gfx.text(ctx, textRenderer, "✔ Installed", cx + cw - 64, y + 5, Gfx.GREEN);
            } else {
                button(ctx, cx + cw - 50, y + 2, 46, 14, "Get", Gfx.accent(), () ->
                        Util.getOperatingSystem().open(URI.create("https://modrinth.com/mod/" + mod.slug() + "/versions?g=1.21.1&l=fabric")));
            }
            y += 20;
        }
        unclip(ctx);
        drawScrollbar(ctx, cx + cw + 3, cy, ch, contentH);
    }

    private void drawGraph(DrawContext ctx, int x, int y, int w, int h) {
        int n = FpsTracker.count();
        if (n < 2) {
            Gfx.text(ctx, textRenderer, "collecting...", x, y + h / 2 - 4, Gfx.MUTED);
            return;
        }
        int top = Math.max(60, FpsTracker.max());
        float step = w / (float) (FpsTracker.SIZE - 1);
        int offset = FpsTracker.SIZE - n;
        for (int i = 0; i < n; i++) {
            int v = FpsTracker.get(i);
            int bh = Math.max(1, Math.round(h * v / (float) top));
            int bx = x + Math.round((i + offset) * step);
            int c = v >= 60 ? Gfx.accent() : v >= 30 ? 0xFFF5C542 : Gfx.RED;
            ctx.fill(bx, y + h - bh, bx + Math.max(1, Math.round(step) - 1), y + h, Gfx.withAlpha(c, 200));
        }
        int line60 = y + h - Math.round(h * 60f / top);
        ctx.fill(x, line60, x + w, line60 + 1, 0x40FFFFFF);
        Gfx.scaledText(ctx, textRenderer, "60", x + w - 10, line60 - 6, 0.6f, Gfx.MUTED);
    }

    private void moduleRow(DrawContext ctx, Module m, int x, int y, int w) {
        if (m == null) return;
        Gfx.roundRect(ctx, x, y, w, 20, 3, 0xFF1B1F2B);
        Gfx.item(ctx, m.getIcon(), x + 3, y + 2, 1f);
        Gfx.text(ctx, textRenderer, m.getName(), x + 23, y + 6, 0xFFFFFFFF);
        Gfx.text(ctx, textRenderer, m.getDescription(), x + 23 + textRenderer.getWidth(m.getName()) + 8, y + 6, Gfx.MUTED);
        toggle(ctx, x + w - 28, y + 5, m.isEnabled(), () -> { m.toggle(); VelocityConfig.save(); });
    }

    private void section(DrawContext ctx, String title, int x, int y) {
        Gfx.text(ctx, textRenderer, title, x, y, Gfx.accent());
    }

    // ------------------------------------------------------------------ SETTINGS page

    private void drawSettings(DrawContext ctx, int cx, int cy, int cw, int ch) {
        int contentH = 230;
        maxScroll = Math.max(0, contentH - ch);
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll));
        clip(ctx, cx - 2, cy, cx + cw + 2, cy + ch);
        int y = cy - Math.round(scroll);

        section(ctx, "ACCENT COLOR", cx, y);
        y += 13;
        for (int i = 0; i < VelocityConfig.ACCENTS.length; i++) {
            int x = cx + i * 24;
            boolean active = VelocityConfig.accent == i;
            if (active) Gfx.roundRect(ctx, x - 2, y - 2, 20, 20, 7, 0xFFFFFFFF);
            float hv = anim("swatch" + i, hovered(x, y, 16, 16), 16f);
            Gfx.roundRect(ctx, x, y, 16, 16, 6, Gfx.mix(VelocityConfig.ACCENTS[i], 0xFFFFFFFF, hv * 0.25f));
            final int index = i;
            click(x, y, 16, 16, () -> { VelocityConfig.accent = index; VelocityConfig.save(); });
        }
        Gfx.text(ctx, textRenderer, VelocityConfig.ACCENT_NAMES[VelocityConfig.accent], cx + VelocityConfig.ACCENTS.length * 24 + 4, y + 4, Gfx.accent());

        y += 26;
        section(ctx, "CLIENT", cx, y);
        y += 12;
        y = boolRow(ctx, "Velocity main menu", VelocityConfig.customMainMenu, () -> VelocityConfig.customMainMenu = !VelocityConfig.customMainMenu, cx, y, cw);
        y = boolRow(ctx, "Menu animations", VelocityConfig.animations, () -> VelocityConfig.animations = !VelocityConfig.animations, cx, y, cw);

        Gfx.roundRect(ctx, cx, y + 1, cw, 20, 3, 0xFF1B1F2B);
        Gfx.text(ctx, textRenderer, "Chroma speed", cx + 7, y + 7, 0xFFFFFFFF);
        String speed = String.format("%.1fx", VelocityConfig.chromaSpeed);
        Gfx.text(ctx, textRenderer, speed, cx + cw - 133 - textRenderer.getWidth(speed), y + 7, Gfx.chroma(0));
        slider(ctx, cx + cw - 127, y + 11, 120, (VelocityConfig.chromaSpeed - 0.2) / 2.8,
                p -> VelocityConfig.chromaSpeed = Math.round((0.2 + Math.max(0, Math.min(1, p)) * 2.8) * 10) / 10.0);
        y += 26;

        section(ctx, "QUICK ACTIONS", cx, y);
        y += 12;
        int bw = (cw - 8) / 3;
        button(ctx, cx, y, bw, 16, "Change Keybinds", 0xFF2A3042, () -> client.setScreen(new KeybindsScreen(this, client.options)));
        button(ctx, cx + bw + 4, y, bw, 16, "Open Mods Folder", 0xFF2A3042, () -> openFolder("mods"));
        button(ctx, cx + (bw + 4) * 2, y, bw, 16, "Reset HUD Layout", 0xFF2A3042, () -> {
            for (HudModule hud : ModuleManager.hudModules()) hud.resetPosition();
            VelocityConfig.save();
        });
        y += 22;
        Gfx.text(ctx, textRenderer, "Drop any Fabric 1.21.1 mod into the mods folder - Velocity works alongside it.", cx, y, Gfx.MUTED);
        unclip(ctx);
    }

    private int boolRow(DrawContext ctx, String label, boolean value, Runnable flip, int x, int y, int w) {
        Gfx.roundRect(ctx, x, y + 1, w, 20, 3, 0xFF1B1F2B);
        Gfx.text(ctx, textRenderer, label, x + 7, y + 7, 0xFFFFFFFF);
        toggle(ctx, x + w - 29, y + 6, value, () -> { flip.run(); VelocityConfig.save(); });
        return y + 22;
    }

    private void openFolder(String name) {
        try {
            var dir = FabricLoader.getInstance().getGameDir().resolve(name);
            java.nio.file.Files.createDirectories(dir);
            Util.getOperatingSystem().open(dir.toFile());
        } catch (Exception e) {
            VelocityClient.LOGGER.warn("Could not open folder {}", name, e);
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (search.visible && !search.isMouseOver(mx, my) && search.isFocused()) setFocused(null);
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (activeSlider != null) {
            activeSlider.accept((mx - activeSliderX) / activeSliderW);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (activeSlider != null) {
            activeSlider = null;
            VelocityConfig.save();
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll - (float) vertical * 28f));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean typing = search.visible && search.isFocused();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && selected != null) {
            selected = null;
            scroll = targetScroll = 0;
            return true;
        }
        if (!typing && VelocityClient.OPEN_MENU.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        // Start typing anywhere on the mods page to search, like a launcher.
        if (!typing && search.visible && keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z && modifiers == 0) {
            setFocused(search);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
