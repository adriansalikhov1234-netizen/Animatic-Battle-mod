package com.example.totemimpact.mixin;

import com.example.totemimpact.ButtonFx;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Анимация наведения на кнопки (только на экране смерти). */
@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void totemimpact$hoverFx(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                     float partialTick, CallbackInfo ci) {
        ButtonFx.apply((AbstractWidget) (Object) this, graphics);
    }
}
