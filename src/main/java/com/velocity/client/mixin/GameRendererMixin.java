package com.velocity.client.mixin;

import com.velocity.client.module.impl.NoHurtCamModule;
import com.velocity.client.module.impl.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void velocity$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        // Only the world camera FOV is zoomed; the hand keeps its normal FOV.
        if (!changingFov) return;
        cir.setReturnValue(ZoomModule.modifyFov(cir.getReturnValue()));
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void velocity$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoHurtCamModule.active()) ci.cancel();
    }
}
