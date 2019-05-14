package me.towdium.stask.client;

import me.towdium.stask.client.Widgets.WContainer;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 14/05/19
 */
public interface Page extends Widget {
    void onResize(int x, int y);

    class Simple extends WContainer implements Page {
        int multiplier = 1;

        @Override
        public void onResize(int x, int y) {
            multiplier = Math.max((y + 179) / 360, 1);
            onLayout((x + multiplier - 1) / multiplier, (y + multiplier - 1) / multiplier);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.SMatrix s = p.matrix()) {
                s.scale(multiplier, multiplier, 1);
                super.onDraw(p, mouse);
            }
        }

        protected void onLayout(int x, int y) {
        }
    }
}
