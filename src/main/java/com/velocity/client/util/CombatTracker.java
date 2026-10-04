package com.velocity.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Remembers your last hit distance and current combo for the Reach and Combo mods. */
public final class CombatTracker {
    private static double lastReach = -1;
    private static long lastHitAt;
    private static int combo;
    private static int lastHurtTime;

    private CombatTracker() {}

    public static void onAttack(PlayerEntity player, Entity target) {
        Vec3d eye = player.getEyePos();
        Box box = target.getBoundingBox();
        double dx = MathHelper.clamp(eye.x, box.minX, box.maxX) - eye.x;
        double dy = MathHelper.clamp(eye.y, box.minY, box.maxY) - eye.y;
        double dz = MathHelper.clamp(eye.z, box.minZ, box.maxZ) - eye.z;
        lastReach = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (target instanceof LivingEntity) {
            combo++;
            lastHitAt = System.currentTimeMillis();
        }
    }

    public static void tick(MinecraftClient client) {
        if (client.player == null) return;
        int hurt = client.player.hurtTime;
        if (hurt > 0 && lastHurtTime == 0) combo = 0; // you got hit: combo broken
        lastHurtTime = hurt;
        if (combo > 0 && System.currentTimeMillis() - lastHitAt > 2500) combo = 0;
    }

    public static double reach() {
        return lastReach;
    }

    public static long lastHitAt() {
        return lastHitAt;
    }

    public static int combo() {
        return combo;
    }
}
