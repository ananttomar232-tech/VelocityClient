package com.velocity.client.module;

import com.velocity.client.module.hud.HudModule;
import com.velocity.client.module.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {}

    public static void init() {
        if (!MODULES.isEmpty()) return;
        // HUD
        MODULES.add(new FpsModule());
        MODULES.add(new CoordinatesModule());
        MODULES.add(new ArmorStatusModule());
        MODULES.add(new PotionEffectsModule());
        MODULES.add(new PingModule());
        MODULES.add(new ClockModule());
        MODULES.add(new DirectionModule());
        MODULES.add(new SpeedModule());
        MODULES.add(new ServerAddressModule());
        MODULES.add(new BiomeModule());
        MODULES.add(new DayCounterModule());
        // PvP
        MODULES.add(new KeystrokesModule());
        MODULES.add(new CpsModule());
        MODULES.add(new ToggleSprintModule());
        MODULES.add(new CrosshairModule());
        MODULES.add(new ReachDisplayModule());
        MODULES.add(new ComboModule());
        MODULES.add(new NoHurtCamModule());
        MODULES.add(new LowFireModule());
        // Utility
        MODULES.add(new ZoomModule());
        MODULES.add(new FullbrightModule());
        MODULES.add(new FreelookModule());
        MODULES.add(new NoPumpkinModule());
        // Performance
        MODULES.add(new DynamicFpsModule());
        MODULES.add(new LowSpecMenusModule());
        MODULES.add(new MemoryModule());
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static Module byId(String id) {
        for (Module m : MODULES) if (m.getId().equals(id)) return m;
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> type) {
        for (Module m : MODULES) if (type.isInstance(m)) return (T) m;
        return null;
    }

    public static List<HudModule> hudModules() {
        List<HudModule> list = new ArrayList<>();
        for (Module m : MODULES) if (m instanceof HudModule hud) list.add(hud);
        return list;
    }

    public static void tick(MinecraftClient client) {
        for (Module m : MODULES) {
            if (m.isEnabled()) m.onTick(client);
        }
    }

    public static void renderHud(DrawContext ctx, MinecraftClient client) {
        if (client.options.hudHidden || client.player == null) return;
        if (client.getDebugHud().shouldShowDebugHud()) return;
        if (client.currentScreen instanceof com.velocity.client.gui.HudEditorScreen) return;
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        for (HudModule hud : hudModules()) {
            if (hud.isEnabled() && hud.hasContent(client)) hud.render(ctx, client, sw, sh, false);
        }
        com.velocity.client.gui.Notifications.render(ctx, sw, sh);
    }
}
