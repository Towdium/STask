package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 11/07/19
 */
@ParametersAreNonnullByDefault
public class WBar extends WContainer {
    public static final int HEIGHT = 10;
    ListenerValue<WBar, Float> listener;
    boolean drag = false;
    int width;
    float pos = 0, ratio = 1;

    public WBar(int width) {
        this.width = width;
        put(new Handle(width), 0, 0);
    }

    public void setPos(float pos) {
        this.pos = pos;
        refresh();
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
        refresh();
    }

    private void refresh() {
        clear();
        int w = (int) (width * ratio);
        put(new Handle(w), (int) ((width - w) * pos), 0);
    }

    @Override
    public void onMove(Vector2i mouse) {
        super.onMove(mouse);
        if (drag) {
            float w = width * ratio;
            float p = (mouse.x - w / 2) / (width - w);
            float n = Math.min(1, Math.max(0, p));
            float o = pos;
            setPos(n);
            if (listener != null) listener.invoke(this, o, n);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(0x333333)) {
            p.drawRect(0, 0, width, HEIGHT);
        }
        super.onDraw(p, mouse);
    }

    public WBar setListener(ListenerValue<WBar, Float> listener) {
        this.listener = listener;
        return this;
    }

    class Handle implements WArea {
        int x;

        public Handle(int x) {
            this.x = x;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            int color = onTest(mouse) || drag ? 0xAAAAAA : 0x666666;
            try (Painter.State ignore = p.color(color)) {
                p.drawRect(0, 0, x, HEIGHT);
            }
        }

        @Override
        public boolean onDrag(@Nullable Vector2i mouse, boolean left) {
            if (onTest(mouse)) {
                drag = true;
                return true;
            } else return false;
        }

        @Override
        public boolean onDrop(boolean left) {
            if (drag) {
                drag = false;
                return true;
            } else return false;
        }

        @Override
        public boolean onTest(@Nullable Vector2i mouse) {
            return Quad.inside(mouse, x, HEIGHT);
        }
    }
}
