package com.example.totemimpact.mixin;

import com.example.totemimpact.ImpactScene;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ваниль вызывает LocalPlayer.displayItemActivation(...) когда тотем бессмертия
 * срабатывает у нас. Мы отменяем стандартную анимацию летящего тотема и
 * запускаем свою импакт-сцену. Если тотем лежал во второй руке — он будет
 * нарисован слева, иначе справа.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void totemimpact$replaceTotemAnimation(ItemStack stack, CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        boolean offhand = self.getOffhandItem() == stack;
        ImpactScene.start(offhand);
        ci.cancel();
    }
}
