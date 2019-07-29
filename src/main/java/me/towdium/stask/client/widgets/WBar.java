package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Colour;
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
    public static final int SIZE = 10;
    ListenerValue<WBar, Float> listener;
    boolean drag = false;
    boolean vertical;
    int size;
    float pos = 0, ratio = 1;

    public WBar(int size, boolean vertical) {
        this.size = size;
        this.vertical = vertical;
        put(new Handle(size), 0, 0);
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
        int w = (int) (size * ratio);
        int p = (int) ((size - w) * pos);
        if (vertical) put(new Handle(w), 0, p);
        else put(new Handle(w), p, 0);
    }

    @Override
    public void onMove(Vector2i mouse) {
        super.onMove(mouse);
        if (drag) {
            float w = size * ratio;
            float m = vertical ? mouse.y : mouse.x;
            float p = (m - w / 2) / (size - w);
            float n = Math.min(1, Math.max(0, p));
            float o = pos;
            setPos(n);
            if (listener != null) listener.invoke(this, o, n);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(Colour.DISABLED)) {
            if (vertical) p.drawRect(0, 0, SIZE, size);
            else p.drawRect(0, 0, size, SIZE);
        }
        super.onDraw(p, mouse);
    }

    public WBar setListener(ListenerValue<WBar, Float> listener) {
        this.listener = listener;
        return this;
    }

    class Handle implements WArea {
        int s;

        public Handle(int s) {
            this.s = s;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            int color = onTest(mouse) || drag ? Colour.HOVERED : Colour.INTERACT;
            try (Painter.State ignore = p.color(color)) {
                if (vertical) p.drawRect(0, 0, SIZE, s);
                else p.drawRect(0, 0, s, SIZE);
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
            return vertical ? Quad.inside(mouse, SIZE, s) : Quad.inside(mouse, s, SIZE);
        }
    }
}
