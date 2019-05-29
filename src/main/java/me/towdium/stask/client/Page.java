package me.towdium.stask.client;

import me.towdium.stask.client.Widgets.WContainer;
import me.towdium.stask.client.Window.Mouse;
import org.joml.Vector2i;

import java.util.List;

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
            multiplier = Math.max(Math.min((y + 179) / 360, (x + 319) / 640), 1);
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
        public boolean onMouse(Vector2i mouse, Mouse button, boolean state) {
            return super.onMouse(convert(mouse), button, state);
        }

        @Override
        public boolean onTooltip(Vector2i mouse, List<String> tooltip) {
            return super.onTooltip(convert(mouse), tooltip);
        }

        @Override
        public boolean onScroll(Vector2i mouse, int diff) {
            return super.onScroll(convert(mouse), diff);
        }

        private Vector2i convert(Vector2i in) {
            return new Vector2i(in.x / multiplier, in.y / multiplier);
        }

        protected void onLayout(int x, int y) {
        }
    }
}
