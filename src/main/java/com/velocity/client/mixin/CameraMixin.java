package com.velocity.client.mixin;

import com.velocity.client.module.impl.FreelookModule;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Freelook: point the camera where the player is looking around instead of the player's own rotation. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", ordinal = 0))
    private void velocity$freelookRotation(Camera camera, float yaw, float pitch) {
        if (FreelookModule.isActive()) setRotation(FreelookModule.yaw(), FreelookModule.pitch());
        else setRotation(yaw, pitch);
    }
}
