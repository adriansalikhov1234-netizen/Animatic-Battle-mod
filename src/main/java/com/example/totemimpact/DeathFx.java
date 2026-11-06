package com.example.totemimpact;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Экран смерти: морской полупрозрачный фон вместо красного, та же картинка,
 * которая растёт каждые 0,5 с в течение 5 секунд, и музыка.
 */
public final class DeathFx {
    private DeathFx() {}

    private static final String NS = TotemImpactClient.MOD_ID;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NS, "textures/gui/impact.png");
    private static final Identifier SOUND_ID = Identifier.fromNamespaceAndPath(NS, "death");

    private static final int TEX_W = 971;
    private static final int TEX_H = 1034;

    /** Рост картинки: раз в 0,5 с, всего 5 секунд (11 шагов: от 3% до 100% высоты). */
    private static final long STEP_MS = 500;
    private static final int STEPS = 10;
    private static final float MIN_SCALE = 0.03f;

    private static Screen current = null;
    private static long startMs = 0;
    private static SoundInstance sound = null;

    /** Вызывается каждый тик клиента: когда экран смерти закрыт, музыка выключается. */
    public static void tick(Minecraft mc) {
        if (current != null && !(mc.screen instanceof DeathScreen)) {
            stopSound(mc);
            current = null;
        }
    }

    private static void stopSound(Minecraft mc) {
        if (sound != null) {
            mc.getSoundManager().stop(sound);
            sound = null;
        }
    }

    public static void renderBackground(GuiGraphicsExtractor g, Screen screen) {
        Minecraft mc = Minecraft.getInstance();
        if (screen != current) {
            stopSound(mc);
            current = screen;
            startMs = System.currentTimeMillis();
            SoundEvent event = SoundEvent.createVariableRangeEvent(SOUND_ID);
            sound = SimpleSoundInstance.forUI(event, 1.0f, 1.0f);
            mc.getSoundManager().play(sound);
        }

        int w = screen.width;
        int h = screen.height;
        long elapsed = System.currentTimeMillis() - startMs;

        drawSea(g, w, h, elapsed);
        drawGrowingPicture(g, w, h, elapsed);
    }

    /** Полупрозрачный морской фон: градиент от бирюзы к глубокому синему + бегущие волны. */
    private static void drawSea(GuiGraphicsExtractor g, int w, int h, long elapsed) {
        double t = elapsed / 1000.0;
        final int strip = 2;
        for (int y = 0; y < h; y += strip) {
            float f = y / (float) h;
            // верх: светлая бирюза, низ: тёмно-синий
            int r = Math.round(30 + (5 - 30) * f);
            int gr = Math.round(165 + (55 - 165) * f);
            int b = Math.round(195 + (110 - 195) * f);
            // полупрозрачность 0x68 → 0xB4
            int a = Math.round(0x68 + (0xB4 - 0x68) * f);
            // волны: лёгкое «дыхание» яркости
            double wave = Math.sin(y * 0.045 + t * 1.6) * 0.5 + 0.5;
            a = Math.min(255, a + (int) (wave * 18));
            g.fill(0, y, w, Math.min(h, y + strip), argb(a, r, gr, b));
        }
        // светлые бегущие блики
        for (int i = 0; i < 4; i++) {
            int y = (int) (((t * (18 + i * 7) + i * 97) % (h + 20)) - 10);
            g.fill(0, y, w, y + 1, argb(28, 200, 245, 255));
        }
    }

    /** Картинка на белой «карточке»: каждые 0,5 с становится больше, за 5 секунд — до полного размера. */
    private static void drawGrowingPicture(GuiGraphicsExtractor g, int w, int h, long elapsed) {
        int step = (int) Math.min(elapsed / STEP_MS, STEPS);
        float scale = (float) (MIN_SCALE * Math.pow(1.0 / MIN_SCALE, step / (double) STEPS));

        int ph = Math.max(2, Math.round(h * scale));
        int pw = Math.max(2, Math.round(ph * (TEX_W / (float) TEX_H)));
        int x = (w - pw) / 2;
        int y = (h - ph) / 2;

        g.fill(x, y, x + pw, y + ph, 0xFFFFFFFF);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y, 0f, 0f, pw, ph, TEX_W, TEX_H, TEX_W, TEX_H);
    }

    static int argb(int a, int r, int g, int b) {
        return (Math.max(0, Math.min(255, a)) << 24) | (r << 16) | (g << 8) | b;
    }
}
