package com.velocity.client.mixin;

import com.velocity.client.module.impl.LowFireModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Low Fire: shift the first-person fire overlay down. */
@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void velocity$lowFireStart(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        matrices.push();
        matrices.translate(0f, -LowFireModule.offset(), 0f);
    }

    @Inject(method = "renderFireOverlay", at = @At("RETURN"))
    private static void velocity$lowFireEnd(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        matrices.pop();
    }
}
