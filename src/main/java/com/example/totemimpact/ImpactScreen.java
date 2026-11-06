package com.example.totemimpact;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Импакт-сцена после смерти: белый экран на 5 секунд, по центру та же картинка,
 * которая каждые 0,5 с становится больше (до полного размера). Нажать ничего
 * нельзя. Когда 5 секунд прошли, DeathFx.tick() возвращает обычный экран смерти
 * (с кнопками «Возродиться» и «Выйти в меню»).
 */
public class ImpactScreen extends Screen {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(TotemImpactClient.MOD_ID, "textures/gui/impact.png");

    private static final int TEX_W = 971;
    private static final int TEX_H = 1034;

    /** Сцена длится 5 секунд, картинка растёт раз в 0,5 с: 10 шагов от 3% до 100% высоты. */
    public static final long DURATION_MS = 5000;
    private static final long STEP_MS = 500;
    private static final int LAST_STEP = 9;
    private static final float MIN_SCALE = 0.03f;

    private final Screen parent;
    private final long startMs = System.currentTimeMillis();

    public ImpactScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    public Screen parent() {
        return parent;
    }

    public boolean isFinished() {
        return System.currentTimeMillis() - startMs >= DURATION_MS;
    }

    /** Во время импакт-сцены Esc ничего не закрывает. */
    @Override
    public void onClose() {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        long elapsed = System.currentTimeMillis() - startMs;

        // Импакт-кадр: всё белое
        g.fill(0, 0, this.width, this.height, 0xFFFFFFFF);

        int step = (int) Math.min(elapsed / STEP_MS, LAST_STEP);
        float scale = (float) (MIN_SCALE * Math.pow(1.0 / MIN_SCALE, step / (double) LAST_STEP));

        int ph = Math.max(2, Math.round(this.height * scale));
        int pw = Math.max(2, Math.round(ph * (TEX_W / (float) TEX_H)));
        int x = (this.width - pw) / 2;
        int y = (this.height - ph) / 2;

        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y, 0f, 0f, pw, ph, TEX_W, TEX_H, TEX_W, TEX_H);
    }
}
