package com.velocity.client.module.impl;

import com.velocity.client.VelocityClient;
import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import com.velocity.client.module.setting.BoolSetting;
import com.velocity.client.module.setting.NumberSetting;
import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;

/** OptiFine-style zoom: hold the zoom key (C by default), scroll to zoom further. */
public final class ZoomModule extends Module {
    private static ZoomModule instance;

    private final NumberSetting factor = add(new NumberSetting("Zoom Amount", 4, 2, 15, 0.5));
    private final BoolSetting smooth = add(new BoolSetting("Smooth Zoom", true));
    private final BoolSetting scroll = add(new BoolSetting("Scroll To Adjust", true));
    private final BoolSetting cinematic = add(new BoolSetting("Cinematic Camera", false));

    private boolean zooming;
    private double scrollFactor = 1;
    private float current = 1f;
    private long lastFrame = System.nanoTime();
    private boolean savedSmoothCamera;

    public ZoomModule() {
        super("zoom", "Zoom", "Hold C to zoom in. Scroll while zooming to adjust.", Category.UTILITY, Items.SPYGLASS, true);
        instance = this;
    }

    @Override
    public void onTick(MinecraftClient client) {
        boolean want = VelocityClient.ZOOM_KEY.isPressed() && client.currentScreen == null;
        if (want && !zooming) {
            scrollFactor = 1;
            if (cinematic.isOn()) {
                savedSmoothCamera = client.options.smoothCameraEnabled;
                client.options.smoothCameraEnabled = true;
            }
        } else if (!want && zooming && cinematic.isOn()) {
            client.options.smoothCameraEnabled = savedSmoothCamera;
        }
        zooming = want;
    }

    @Override
    public void onDisable() {
        zooming = false;
    }

    /** Called from the GameRenderer mixin every frame with the vanilla field of view. */
    public static double modifyFov(double fov) {
        ZoomModule zoom = instance;
        if (zoom == null) return fov;
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - zoom.lastFrame) / 1_000_000_000f);
        zoom.lastFrame = now;

        float target = zoom.isEnabled() && zoom.zooming ? (float) (zoom.factor.get() * zoom.scrollFactor) : 1f;
        zoom.current = zoom.smooth.isOn() ? Gfx.approach(zoom.current, target, 14f, dt) : target;
        if (Math.abs(zoom.current - 1f) < 0.001f) return fov;
        return fov / zoom.current;
    }

    /** Returns true when the scroll was used for zooming and should not change the hotbar slot. */
    public static boolean onScroll(double amount) {
        ZoomModule zoom = instance;
        if (zoom == null || !zoom.isEnabled() || !zoom.zooming || !zoom.scroll.isOn()) return false;
        zoom.scrollFactor = Math.max(0.5, Math.min(6, zoom.scrollFactor * (amount > 0 ? 1.15 : 1 / 1.15)));
        return true;
    }

    public static boolean isZooming() {
        return instance != null && instance.isEnabled() && instance.zooming;
    }
}
