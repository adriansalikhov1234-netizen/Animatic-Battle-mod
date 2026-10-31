package com.example.totemimpact;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Импакт-сцена: весь экран белый, по центру картинка, которая каждые 0,5 с
 * становится больше (от крошечной до размера на весь экран). Сцена идёт
 * ровно столько, сколько длится музыка (impact.ogg).
 *
 * Пока идёт сцена, голубой тотем со свечением остаётся «в руке»
 * (внизу экрана) и пропадает вместе с концом сцены.
 */
public final class ImpactScene {
    private ImpactScene() {}

    private static final String NS = TotemImpactClient.MOD_ID;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NS, "textures/gui/impact.png");
    private static final Identifier TOTEM = Identifier.fromNamespaceAndPath(NS, "textures/gui/totem_blue.png");
    private static final Identifier GLOW = Identifier.fromNamespaceAndPath(NS, "textures/gui/glow.png");
    private static final Identifier SOUND_ID = Identifier.fromNamespaceAndPath(NS, "impact");

    /** Размер картинки в файле. */
    private static final int TEX_W = 971;
    private static final int TEX_H = 1034;

    /** Размеры спрайтов тотема и свечения в файлах. */
    private static final int TOTEM_TEX = 128;
    private static final int GLOW_TEX = 256;

    /** Длина музыки в мс (impact.ogg = 2,208 с). Сцена не кончится раньше. */
    private static final long DURATION_MS = 2208;

    /** Картинка увеличивается раз в 0,5 секунды. */
    private static final long STEP_MS = 500;

    /** Доля высоты экрана на каждом шаге: от очень маленькой до во весь экран. */
    private static final float[] SCALES = {0.04f, 0.15f, 0.35f, 0.65f, 1.0f};

    private static long startMs = -1;
    private static boolean totemInOffhand = false;

    public static boolean isActive() {
        return startMs >= 0;
    }

    /** Запускает сцену и музыку (если сцена уже идёт — ничего не делает). */
    public static void start(boolean offhand) {
        if (isActive()) {
            return;
        }
        totemInOffhand = offhand;
        startMs = System.currentTimeMillis();
        Minecraft mc = Minecraft.getInstance();
        SoundEvent event = SoundEvent.createVariableRangeEvent(SOUND_ID);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0f, 1.0f));
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        if (!isActive()) {
            return;
        }
        long elapsed = System.currentTimeMillis() - startMs;
        if (elapsed >= DURATION_MS) {
            startMs = -1; // музыка кончилась — сцена кончилась, тотем пропал
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        int sw = window.getGuiScaledWidth();
        int sh = window.getGuiScaledHeight();

        // Импакт-кадр: всё белое
        graphics.fill(0, 0, sw, sh, 0xFFFFFFFF);

        // Какой сейчас шаг роста (каждые 0,5 с)
        int step = (int) Math.min(elapsed / STEP_MS, SCALES.length - 1);
        float scale = SCALES[step];

        int h = Math.max(2, Math.round(sh * scale));
        int w = Math.max(2, Math.round(h * (TEX_W / (float) TEX_H)));
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;

        // renderPipeline, texture, x, y, u, v, width, height, regionW, regionH, texW, texH
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y, 0f, 0f, w, h, TEX_W, TEX_H, TEX_W, TEX_H);

        renderHeldTotem(graphics, sw, sh, elapsed);
    }

    /** Голубой тотем со свечением в «руке» (внизу экрана), поверх всего. */
    private static void renderHeldTotem(GuiGraphicsExtractor graphics, int sw, int sh, long elapsed) {
        int size = Math.round(sh * 0.30f);
        // Положение примерно как у предмета в руке от первого лица
        int cx = Math.round(sw * (totemInOffhand ? 0.20f : 0.80f));
        int cy = Math.round(sh * 0.80f);

        // Пульсация свечения
        float pulse = 1.0f + 0.12f * (float) Math.sin(elapsed / 110.0);
        int glowSize = Math.round(size * 2.4f * pulse);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GLOW,
                cx - glowSize / 2, cy - glowSize / 2, 0f, 0f,
                glowSize, glowSize, GLOW_TEX, GLOW_TEX, GLOW_TEX, GLOW_TEX);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TOTEM,
                cx - size / 2, cy - size / 2, 0f, 0f,
                size, size, TOTEM_TEX, TOTEM_TEX, TOTEM_TEX, TOTEM_TEX);
    }
}
