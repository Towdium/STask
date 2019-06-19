package me.towdium.stask.client;

import me.towdium.stask.client.Widgets.WContainer;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Author: Towdium
 * Date: 14/05/19
 */
@ParametersAreNonnullByDefault
public interface Page extends Widget {
    void onResize(int x, int y);

    class Simple extends WContainer implements Page {
        int multiplier = 1;
        static final int WIDTH = 1280;
        static final int HEIGHT = 720;

        @Override
        public void onResize(int x, int y) {
            multiplier = Math.max(Math.min((y + HEIGHT / 2 - 1) / HEIGHT, (x + WIDTH / 2 - 1) / WIDTH), 1);
            onLayout((x + multiplier - 1) / multiplier, (y + multiplier - 1) / multiplier);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.SMatrix s = p.matrix()) {
                s.scale(multiplier, multiplier, 1);
                super.onDraw(p, convert(mouse));
            }
        }

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left, boolean state) {
            return super.onClick(convert(mouse), left, state);
        }

        @Override
        public boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
            return super.onTooltip(convert(mouse), tooltip);
        }

        @Override
        public void onMove(Vector2i mouse) {
            super.onMove(convert(mouse));
        }

        @Override
        public boolean onScroll(@Nullable Vector2i mouse, int diff) {
            return super.onScroll(convert(mouse), diff);
        }

        private Vector2i convert(@Nullable Vector2i in) {
            return in == null ? null : new Vector2i(in.x / multiplier, in.y / multiplier);
        }

        protected void onLayout(int x, int y) {
        }
    }
}
