package com.velocity.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.velocity.client.VelocityConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Hotbar opacity: only the hotbar frame and selection fade, items stay fully visible. */
@Mixin(InGameHud.class)
public abstract class InGameHudHotbarMixin {
    @WrapOperation(method = "renderHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void velocity$hotbarOpacity(DrawContext ctx, Identifier texture, int x, int y, int w, int h, Operation<Void> original) {
        float alpha = (float) VelocityConfig.hotbarOpacity;
        if (alpha >= 0.999f) {
            original.call(ctx, texture, x, y, w, h);
            return;
        }
        ctx.setShaderColor(1f, 1f, 1f, alpha);
        original.call(ctx, texture, x, y, w, h);
        ctx.setShaderColor(1f, 1f, 1f, 1f);
    }
}
