package com.velocity.client.mixin;

import com.velocity.client.module.impl.CrosshairModule;
import com.velocity.client.module.impl.NoPumpkinModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void velocity$crosshair(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (CrosshairModule.render(ctx, MinecraftClient.getInstance())) ci.cancel();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void velocity$noPumpkin(DrawContext ctx, Identifier texture, float opacity, CallbackInfo ci) {
        if (NoPumpkinModule.active() && texture.getPath().contains("pumpkinblur")) ci.cancel();
    }
}
