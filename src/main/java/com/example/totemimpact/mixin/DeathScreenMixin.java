package com.example.totemimpact.mixin;

import com.example.totemimpact.DeathFx;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Экран смерти: вместо полупрозрачного красного фона рисуем морской фон,
 * растущую картинку и запускаем музыку.
 */
@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true, require = 0)
    private void totemimpact$seaBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                           float partialTick, CallbackInfo ci) {
        DeathFx.renderBackground(graphics, (Screen) (Object) this);
        ci.cancel();
    }
}
