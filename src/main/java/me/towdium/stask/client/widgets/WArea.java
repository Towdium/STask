package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public interface WArea extends Widget {
    boolean onTest(@Nullable Vector2i mouse);

    class Impl implements WArea {
        int x, y;

        public Impl(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean onTest(@Nullable Vector2i mouse) {
            return Quad.inside(mouse, x, y);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
        }
    }
}
