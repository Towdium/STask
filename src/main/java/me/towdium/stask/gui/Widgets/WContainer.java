package me.towdium.stask.gui.Widgets;

import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.Widget;
import me.towdium.stask.utils.Pair;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Towdium
 * Date: 09/03/19
 */
public class WContainer implements Widget {
    protected List<Pair<Vector2i, Widget>> widgets = new ArrayList<>();

    @Override
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void onDraw(Painter p, Vector2i mouse) {
        for (int i = 0; i < widgets.size(); i++) {
            Pair<Vector2i, Widget> w = widgets.get(i);
            try (Painter.SMatrix mat = p.matrix()) {
                mat.translate(w.a.x, w.a.y);
                w.b.onDraw(p, mouse.sub(w.a, new Vector2i()));
            }
        }
    }

    @Override
    public boolean onTooltip(Vector2i mouse, List<String> tooltip) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Pair<Vector2i, Widget> w = widgets.get(i);
            if (w.b.onTooltip(mouse.sub(w.a, new Vector2i()), tooltip)) return true;
        }
        return false;
    }

    @Override
    public boolean onMouse(Vector2i mouse, int button, boolean state) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Pair<Vector2i, Widget> w = widgets.get(i);
            if (w.b.onMouse(mouse.sub(w.a, new Vector2i()), button, state)) return true;
        }
        return false;
    }

    @Override
    public boolean onKey(char ch, int code) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Pair<Vector2i, Widget> w = widgets.get(i);
            if (w.b.onKey(ch, code)) return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(Vector2i mouse, int diff) {
        for (int i = widgets.size() - 1; i >= 0; i++) {
            Pair<Vector2i, Widget> w = widgets.get(i);
            if (w.b.onScroll(mouse, diff)) return true;
        }
        return false;
    }

    public WContainer add(int x, int y, Widget widget) {
        widgets.add(new Pair<>(new Vector2i(x, y), widget));
        return this;
    }

    public WContainer remove(Widget widget) {
        widgets.removeIf(i -> i.b == widget);
        return this;
    }

    public WContainer clear() {
        widgets.clear();
        return this;
    }
}
