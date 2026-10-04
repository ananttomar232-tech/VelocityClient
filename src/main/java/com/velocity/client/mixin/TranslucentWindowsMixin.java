package com.velocity.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Containers draw their window with blending on, so the inventory opacity setting can show the world behind. */
@Mixin(HandledScreen.class)
public abstract class TranslucentWindowsMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"))
    private void velocity$blend(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
