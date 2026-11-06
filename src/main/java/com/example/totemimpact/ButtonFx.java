package com.example.totemimpact;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.DeathScreen;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Анимация наведения на кнопки экрана смерти: плавно нарастающее голубое
 * свечение, рамка и бегущий блик.
 */
public final class ButtonFx {
    private ButtonFx() {}

    private static final class State {
        float p;
        long lastMs = System.currentTimeMillis();
    }

    private static final Map<AbstractWidget, State> STATES = new WeakHashMap<>();

    public static void apply(AbstractWidget widget, GuiGraphicsExtractor g) {
        if (!(widget instanceof AbstractButton)) {
            return;
        }
        if (!(Minecraft.getInstance().screen instanceof DeathScreen)) {
            return;
        }

        long now = System.currentTimeMillis();
        State s = STATES.computeIfAbsent(widget, k -> new State());
        float dt = Math.min(0.1f, (now - s.lastMs) / 1000f);
        s.lastMs = now;

        float target = widget.isHoveredOrFocused() ? 1f : 0f;
        s.p += (target - s.p) * Math.min(1f, dt * 14f);
        if (Math.abs(target - s.p) < 0.01f) {
            s.p = target;
        }
        float p = s.p;
        if (p <= 0.01f) {
            return;
        }

        int x = widget.getX();
        int y = widget.getY();
        int bw = widget.getWidth();
        int bh = widget.getHeight();
        int x1 = x + bw;
        int y1 = y + bh;

        // внешнее свечение: несколько расходящихся колец
        for (int i = 1; i <= 4; i++) {
            int grow = i;
            int alpha = Math.round(80 * p / i);
            ring(g, x - grow, y - grow, x1 + grow, y1 + grow, DeathFx.argb(alpha, 120, 215, 255));
        }

        // лёгкая голубая заливка
        g.fill(x, y, x1, y1, DeathFx.argb(Math.round(55 * p), 140, 220, 255));

        // яркая рамка
        ring(g, x, y, x1, y1, DeathFx.argb(Math.round(255 * p), 175, 238, 255));

        // бегущий блик
        double phase = ((now / 1000.0) * 0.9) % 1.0;
        int sx = x + (int) ((bw + 24) * phase) - 12;
        int bx0 = Math.max(x + 1, sx);
        int bx1 = Math.min(x1 - 1, sx + 10);
        if (bx1 > bx0) {
            g.fill(bx0, y + 1, bx1, y1 - 1, DeathFx.argb(Math.round(75 * p), 255, 255, 255));
        }
    }

    /** Рамка толщиной 1 пиксель внутри прямоугольника. */
    private static void ring(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        g.fill(x0, y0, x1, y0 + 1, color);
        g.fill(x0, y1 - 1, x1, y1, color);
        g.fill(x0, y0 + 1, x0 + 1, y1 - 1, color);
        g.fill(x1 - 1, y0 + 1, x1, y1 - 1, color);
    }
}
