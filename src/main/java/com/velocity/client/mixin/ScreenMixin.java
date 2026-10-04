package com.velocity.client.mixin;

import com.velocity.client.VelocityConfig;
import com.velocity.client.gui.BaseScreen;
import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Velocity backgrounds for every vanilla menu, plus a quick fade-in when an in-game screen opens. */
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow public int width;
    @Shadow public int height;
    @Shadow protected MinecraftClient client;

    @Unique private long velocity$shownAt;
    @Unique private boolean velocity$fading;

    @Inject(method = "renderInGameBackground", at = @At("HEAD"), cancellable = true)
    private void velocity$inGameBackground(DrawContext ctx, CallbackInfo ci) {
        if (!VelocityConfig.uiTheme) return;
        ctx.fillGradient(0, 0, width, height, 0x90080A10, 0xC80D1018);
        ci.cancel();
    }

    @Inject(method = "renderDarkening(Lnet/minecraft/client/gui/DrawContext;IIII)V", at = @At("HEAD"), cancellable = true)
    private void velocity$darkening(DrawContext ctx, int x, int y, int w, int h, CallbackInfo ci) {
        if (!VelocityConfig.uiTheme) return;
        ctx.fill(x, y, x + w, y + h, 0xB0080A10);
        ci.cancel();
    }

    @Inject(method = "onDisplayed", at = @At("HEAD"))
    private void velocity$displayed(CallbackInfo ci) {
        velocity$shownAt = System.currentTimeMillis();
    }

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void velocity$fadeStart(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        velocity$fading = false;
        Object self = this;
        // Only in-game screens: the world behind them makes a fade look smooth. Velocity's own screens animate themselves.
        if (!VelocityConfig.screenAnimations || client == null || client.world == null
                || self instanceof BaseScreen || self instanceof HandledScreen<?>) return;
        float t = (System.currentTimeMillis() - velocity$shownAt) / 160f;
        if (t >= 1f || t < 0f) return;
        velocity$fading = true;
        ctx.setShaderColor(1f, 1f, 1f, Math.max(0.05f, Gfx.easeOutCubic(t)));
    }

    @Inject(method = "renderWithTooltip", at = @At("TAIL"))
    private void velocity$fadeEnd(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (velocity$fading) ctx.setShaderColor(1f, 1f, 1f, 1f);
        velocity$fading = false;
    }
}
