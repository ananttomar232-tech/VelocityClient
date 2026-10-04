package com.velocity.client.mixin;

import com.velocity.client.module.impl.FreelookModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Freelook: while active, mouse movement turns the camera, not the player. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void velocity$freelook(double dx, double dy, CallbackInfo ci) {
        if (FreelookModule.isActive() && (Object) this == MinecraftClient.getInstance().player) {
            FreelookModule.turnCamera(dx, dy);
            ci.cancel();
        }
    }
}
