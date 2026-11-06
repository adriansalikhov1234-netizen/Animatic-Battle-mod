package com.example.totemimpact;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Что происходит при смерти:
 * 1) появляется экран смерти -> сразу включается импакт-сцена (ImpactScreen, 5 секунд, белый фон)
 *    и музыка;
 * 2) через 5 секунд возвращается обычный экран смерти с морским фоном и кнопками.
 * Музыка выключается, когда ни импакт-сцены, ни экрана смерти больше нет.
 */
public final class DeathFx {
    private DeathFx() {}

    private static final Identifier SOUND_ID =
            Identifier.fromNamespaceAndPath(TotemImpactClient.MOD_ID, "death");

    private static Screen handled = null;
    private static SoundInstance sound = null;
    private static long startMs = 0;

    /** Вызывается каждый тик клиента. */
    public static void tick(Minecraft mc) {
        Screen s = mc.gui.screen();

        if (s instanceof ImpactScreen impact) {
            if (impact.isFinished()) {
                mc.gui.setScreen(impact.parent());
            }
            return;
        }

        if (s instanceof DeathScreen) {
            if (s != handled) {
                handled = s;
                startMs = System.currentTimeMillis();
                startSound(mc);
                mc.gui.setScreen(new ImpactScreen(s));
            }
            return;
        }

        // ни импакт-сцены, ни экрана смерти: возродились / вышли
        if (handled != null || sound != null) {
            stopSound(mc);
            handled = null;
        }
    }

    private static void startSound(Minecraft mc) {
        stopSound(mc);
        SoundEvent event = SoundEvent.createVariableRangeEvent(SOUND_ID);
        sound = SimpleSoundInstance.forUI(event, 1.0f, 1.0f);
        mc.getSoundManager().play(sound);
    }

    private static void stopSound(Minecraft mc) {
        if (sound != null) {
            mc.getSoundManager().stop(sound);
            sound = null;
        }
    }

    /** Морской фон экрана смерти (вызывается из DeathScreenMixin). */
    public static void renderBackground(GuiGraphicsExtractor g, Screen screen) {
        drawSea(g, screen.width, screen.height, System.currentTimeMillis() - startMs);
    }

    /** Полупрозрачный морской фон: градиент от бирюзы к глубокому синему + бегущие волны. */
    private static void drawSea(GuiGraphicsExtractor g, int w, int h, long elapsed) {
        double t = elapsed / 1000.0;
        final int strip = 2;
        for (int y = 0; y < h; y += strip) {
            float f = y / (float) h;
            int r = Math.round(30 + (5 - 30) * f);
            int gr = Math.round(165 + (55 - 165) * f);
            int b = Math.round(195 + (110 - 195) * f);
            int a = Math.round(0x68 + (0xB4 - 0x68) * f);
            double wave = Math.sin(y * 0.045 + t * 1.6) * 0.5 + 0.5;
            a = Math.min(255, a + (int) (wave * 18));
            g.fill(0, y, w, Math.min(h, y + strip), argb(a, r, gr, b));
        }
        for (int i = 0; i < 4; i++) {
            int y = (int) (((t * (18 + i * 7) + i * 97) % (h + 20)) - 10);
            g.fill(0, y, w, y + 1, argb(28, 200, 245, 255));
        }
    }

    static int argb(int a, int r, int g, int b) {
        return (Math.max(0, Math.min(255, a)) << 24) | (r << 16) | (g << 8) | b;
    }
}
