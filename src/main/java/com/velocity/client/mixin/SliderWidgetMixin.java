package com.velocity.client.mixin;

import com.velocity.client.VelocityConfig;
import com.velocity.client.theme.VanillaTheme;
import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Vanilla sliders (FOV, render distance, volume...) in the Velocity style. */
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin extends ClickableWidget {
    @Shadow protected double value;
    @Unique private float velocity$hover;
    @Unique private long velocity$lastFrame;

    public SliderWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void velocity$themedSlider(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!VelocityConfig.uiTheme) return;
        long now = System.nanoTime();
        float dt = velocity$lastFrame == 0 ? 0 : Math.min(0.1f, (now - velocity$lastFrame) / 1_000_000_000f);
        velocity$lastFrame = now;
        velocity$hover = Gfx.approach(velocity$hover, this.active && (this.isHovered() || this.isFocused()) ? 1f : 0f, 16f, dt);

        VanillaTheme.slider(ctx, getX(), getY(), getWidth(), getHeight(), value, this.active, velocity$hover, this.alpha);
        int color = this.active ? 0xFFFFFF : 0x6E7484;
        drawScrollableText(ctx, MinecraftClient.getInstance().textRenderer, 2, color | MathHelper.ceil(this.alpha * 255f) << 24);
        ci.cancel();
    }
}
