package com.velocity.client.mixin;

import com.velocity.client.theme.VanillaTheme;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @ModifyConstant(method = "drawForeground", constant = @Constant(intValue = VanillaTheme.VANILLA_DARK_TEXT))
    private int velocity$titleColor(int color) {
        return VanillaTheme.containerText(color);
    }
}
