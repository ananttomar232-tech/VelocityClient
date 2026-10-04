package com.velocity.client.mixin;

import com.velocity.client.VelocityConfig;
import com.velocity.client.theme.VanillaTheme;
import com.velocity.client.util.Gfx;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every vanilla button (title, pause, options, advancements...) gets Velocity's rounded, animated style. */
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin extends ClickableWidget {
    @Unique private float velocity$hover;
    @Unique private long velocity$lastFrame;

    public PressableWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Shadow
    public abstract void drawMessage(DrawContext context, TextRenderer textRenderer, int color);

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void velocity$themedButton(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!VelocityConfig.uiTheme) return;
        long now = System.nanoTime();
        float dt = velocity$lastFrame == 0 ? 0 : Math.min(0.1f, (now - velocity$lastFrame) / 1_000_000_000f);
        velocity$lastFrame = now;
        velocity$hover = Gfx.approach(velocity$hover, this.active && this.isSelected() ? 1f : 0f, 16f, dt);

        VanillaTheme.button(ctx, getX(), getY(), getWidth(), getHeight(), this.active, velocity$hover, this.alpha);
        int color = this.active ? Gfx.mix(0xFFDDE1EA, 0xFFFFFFFF, velocity$hover) : 0xFF6E7484;
        drawMessage(ctx, MinecraftClient.getInstance().textRenderer, (color & 0xFFFFFF) | MathHelper.ceil(this.alpha * 255f) << 24);
        ci.cancel();
    }
}
