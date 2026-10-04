package com.velocity.client.mixin;

import com.velocity.client.theme.VanillaTheme;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Light "Advancements" title on the dark advancement window. */
@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin {
    @ModifyConstant(method = "drawWindow", constant = @Constant(intValue = VanillaTheme.VANILLA_DARK_TEXT))
    private int velocity$titleColor(int color) {
        return VanillaTheme.containerText(color);
    }
}
