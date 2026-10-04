package com.velocity.client.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets Fullbright set gamma above the slider's normal 0-100% range. */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor<T> {
    @Accessor("value")
    void velocity$setValue(T value);
}
