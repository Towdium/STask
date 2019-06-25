package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 28/05/19
 */
@ParametersAreNonnullByDefault
public abstract class WFocus implements WArea {
    static Graph.Work focus;
    static WFocus active;

    @Override
    public void onRefresh(Vector2i mouse) {
        Graph.Work t = onTest(mouse) ? onFocus() : null;
        if (t != null) {
            active = this;
            focus = t;
        } else if (active == this) {
            active = null;
            focus = null;
        }
    }

    @Override
    public void onRemove() {
        if (active != this) return;
        active = null;
        focus = null;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
    }

    @Nullable
    public abstract Graph.Work onFocus();

    public abstract static class Impl extends WFocus {
        int x, y;

        public Impl(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean onTest(@Nullable Vector2i mouse) {
            return Quad.inside(mouse, x, y);
        }
    }
}
