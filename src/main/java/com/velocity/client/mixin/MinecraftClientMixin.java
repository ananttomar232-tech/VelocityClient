package com.velocity.client.mixin;

import com.velocity.client.VelocityConfig;
import com.velocity.client.gui.VelocityTitleScreen;
import com.velocity.client.module.impl.DynamicFpsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true, require = 0)
    private void velocity$dynamicFps(CallbackInfoReturnable<Integer> cir) {
        int limit = DynamicFpsModule.frameLimit((MinecraftClient) (Object) this);
        if (limit > 0) cir.setReturnValue(limit);
    }

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true, require = 0)
    private Screen velocity$customTitle(Screen screen) {
        if (screen instanceof TitleScreen) {
            if (VelocityTitleScreen.showVanillaOnce) {
                VelocityTitleScreen.showVanillaOnce = false;
                return screen;
            }
            if (VelocityConfig.customMainMenu) return new VelocityTitleScreen();
        }
        return screen;
    }
}
