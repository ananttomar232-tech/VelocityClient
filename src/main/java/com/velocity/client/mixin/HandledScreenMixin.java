package com.velocity.client.mixin;

import com.velocity.client.theme.VanillaTheme;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Light container titles so they stay readable on the dark inventory theme. */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @ModifyConstant(method = "drawForeground", constant = @Constant(intValue = VanillaTheme.VANILLA_DARK_TEXT))
    private int velocity$titleColor(int color) {
        return VanillaTheme.containerText(color);
    }
}
