package com.velocity.client.module.impl;

import com.velocity.client.VelocityClient;
import com.velocity.client.module.Category;
import com.velocity.client.module.Module;
import com.velocity.client.module.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

/** Hold Left Alt to look around your character in third person without turning where you walk. */
public final class FreelookModule extends Module {
    private static FreelookModule instance;

    private final BoolSetting invertPitch = add(new BoolSetting("Invert Up/Down", false));

    private boolean active;
    private Perspective previous;
    private float cameraYaw;
    private float cameraPitch;

    public FreelookModule() {
        super("freelook", "Freelook", "Hold Left Alt to look around without turning.", Category.UTILITY, Items.ENDER_EYE, true);
        instance = this;
    }

    @Override
    public void onTick(MinecraftClient client) {
        boolean want = VelocityClient.FREELOOK_KEY.isPressed() && client.currentScreen == null && client.player != null;
        if (want && !active) {
            active = true;
            previous = client.options.getPerspective();
            cameraYaw = client.player.getYaw();
            cameraPitch = client.player.getPitch();
            if (previous.isFirstPerson()) client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        } else if (!want && active) {
            stop(client);
        }
    }

    @Override
    public void onDisable() {
        if (active) stop(MinecraftClient.getInstance());
    }

    private void stop(MinecraftClient client) {
        active = false;
        if (previous != null) client.options.setPerspective(previous);
    }

    public static boolean isActive() {
        return instance != null && instance.isEnabled() && instance.active;
    }

    /** Mouse movement goes to the camera instead of the player while freelooking. */
    public static void turnCamera(double dx, double dy) {
        FreelookModule m = instance;
        m.cameraYaw += (float) dx * 0.15f;
        m.cameraPitch = MathHelper.clamp(m.cameraPitch + (float) dy * 0.15f * (m.invertPitch.isOn() ? -1 : 1), -90f, 90f);
    }

    public static float yaw() {
        return instance.cameraYaw;
    }

    public static float pitch() {
        return instance.cameraPitch;
    }
}
