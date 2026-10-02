package com.velocity.client.gui;

import com.velocity.client.VelocityClient;
import com.velocity.client.VelocityConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class VelocityScreen extends Screen {
    private final Screen parent;
    private Tab tab = Tab.PERFORMANCE;

    private enum Tab { PERFORMANCE, HUD, MODS }

    public VelocityScreen(Screen parent) {
        super(Text.literal("Velocity Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();

        int center = width / 2;
        int top = 34;

        addDrawableChild(ButtonWidget.builder(Text.literal("Performance"), b -> switchTab(Tab.PERFORMANCE))
                .dimensions(center - 150, 8, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD"), b -> switchTab(Tab.HUD))
                .dimensions(center - 48, 8, 96, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Mods"), b -> switchTab(Tab.MODS))
                .dimensions(center + 54, 8, 96, 20).build());

        switch (tab) {
            case PERFORMANCE -> buildPerformance(center, top);
            case HUD -> buildHud(center, top);
            case MODS -> buildMods(center, top);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(center - 70, height - 30, 140, 20).build());
    }

    private void switchTab(Tab next) {
        tab = next;
        init(this.client, width, height);
    }

    private void buildPerformance(int center, int top) {
        addDrawableChild(toggleButton(center - 155, top + 46, 310, "Performance Profile", VelocityConfig.performanceProfile,
                () -> {
                    VelocityConfig.performanceProfile = !VelocityConfig.performanceProfile;
                    VelocityClient.applyPerformanceProfile(client);
                    VelocityConfig.save();
                }));

        addDrawableChild(ButtonWidget.builder(Text.literal("Apply Low-Spec Settings Now"), b -> {
                    VelocityConfig.performanceProfile = true;
                    VelocityClient.applyPerformanceProfile(client);
                    VelocityConfig.save();
                    b.setMessage(Text.literal("Applied ✓"));
                })
                .dimensions(center - 155, top + 80, 310, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Open Minecraft Video Settings"), b -> {
                    if (client != null) client.setScreen(new net.minecraft.client.gui.screen.option.VideoOptionsScreen(this, client.options));
                })
                .dimensions(center - 155, top + 112, 310, 20).build());
    }

    private void buildHud(int center, int top) {
        addDrawableChild(toggleButton(center - 155, top + 46, 310, "FPS Counter", VelocityConfig.fpsHud,
                () -> { VelocityConfig.fpsHud = !VelocityConfig.fpsHud; VelocityConfig.save(); }));
        addDrawableChild(toggleButton(center - 155, top + 80, 310, "Coordinates", VelocityConfig.coordsHud,
                () -> { VelocityConfig.coordsHud = !VelocityConfig.coordsHud; VelocityConfig.save(); }));
        addDrawableChild(toggleButton(center - 155, top + 114, 310, "Sprint State", VelocityConfig.sprintHud,
                () -> { VelocityConfig.sprintHud = !VelocityConfig.sprintHud; VelocityConfig.save(); }));
        addDrawableChild(toggleButton(center - 155, top + 148, 310, "Zoom Key Ready", VelocityConfig.zoom,
                () -> { VelocityConfig.zoom = !VelocityConfig.zoom; VelocityConfig.save(); }));
    }

    private void buildMods(int center, int top) {
        List<ModContainer> mods = FabricLoader.getInstance().getAllMods().stream()
                .sorted(Comparator.comparing(m -> m.getMetadata().getName()))
                .collect(Collectors.toList());

        int shown = Math.min(mods.size(), 9);
        for (int i = 0; i < shown; i++) {
            ModContainer mod = mods.get(i);
            String name = mod.getMetadata().getName();
            String version = String.valueOf(mod.getMetadata().getVersion());
            addDrawableChild(ButtonWidget.builder(Text.literal(name + "  •  " + version), b -> {})
                    .dimensions(center - 155, top + 40 + i * 25, 310, 20).build());
        }

        int bottom = top + 40 + shown * 25 + 8;
        addDrawableChild(ButtonWidget.builder(Text.literal("Open Mods Folder"), b -> openFolder(FabricLoader.getInstance().getGameDir().resolve("mods")))
                .dimensions(center - 155, Math.min(bottom, height - 56), 310, 20).build());
    }

    private ButtonWidget toggleButton(int x, int y, int w, String label, boolean enabled, Runnable action) {
        return ButtonWidget.builder(Text.literal(label + ": " + (enabled ? "ON" : "OFF")), b -> {
                    action.run();
                    b.setMessage(Text.literal(label + ": " + (label.equals("Performance Profile") ? VelocityConfig.performanceProfile :
                            label.equals("FPS Counter") ? VelocityConfig.fpsHud :
                            label.equals("Coordinates") ? VelocityConfig.coordsHud :
                            label.equals("Sprint State") ? VelocityConfig.sprintHud : VelocityConfig.zoom) ? "ON" : "OFF"));
                })
                .dimensions(x, y, w, 20).build();
    }

    private void openFolder(Path folder) {
        try {
            java.nio.file.Files.createDirectories(folder);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder.toFile());
            }
        } catch (IOException ignored) {
            // Opening a folder is optional; do not interrupt the game if the OS blocks it.
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xD9101118);

        int left = width / 2 - 190;
        int right = width / 2 + 190;
        int top = 34;
        int bottom = height - 40;

        context.fill(left, top, right, bottom, 0xE61B1F2A);
        context.fill(left + 1, top + 1, right - 1, top + 3, 0xFF8C7CFF);

        context.drawCenteredTextWithShadow(textRenderer, Text.literal("VELOCITY"), width / 2, top + 10, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Lightweight client shell • " + VelocityClient.loadedModSummary()), width / 2, top + 25, 0xFFB9BEC9);

        super.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(textRenderer, Text.literal("Right Shift  •  open menu"), 8, height - 14, 0xFF8E94A2);
    }

    @Override
    public void close() {
        VelocityConfig.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
