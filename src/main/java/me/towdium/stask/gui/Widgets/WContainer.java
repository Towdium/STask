package me.towdium.stask.gui.Widgets;

import me.towdium.stask.gui.IWidget;
import me.towdium.stask.gui.States;
import me.towdium.stask.utils.Pair;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Towdium
 * Date: 09/03/19
 */
public class WContainer implements IWidget {
    List<Pair<Vector2i, IWidget>> widgets = new ArrayList<>();

    @Override
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void onDraw(Vector2i mouse) {
        for (int i = 0; i < widgets.size(); i++) {
            Pair<Vector2i, IWidget> w = widgets.get(i);
            try (States.SMatrix mat = States.matrix()) {
                mat.translate(w.one.x, w.one.y);
                w.two.onDraw(mouse.sub(w.one, new Vector2i()));
            }
        }
    }

    @Override
    public boolean onTooltip(Vector2i mouse, List<String> tooltip) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Pair<Vector2i, IWidget> w = widgets.get(i);
            if (w.two.onTooltip(mouse.sub(w.one, new Vector2i()), tooltip)) return true;
        }
        return false;
    }

    @Override
    public boolean onMouse(Vector2i mouse, int button, boolean state) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Pair<Vector2i, IWidget> w = widgets.get(i);
            if (w.two.onMouse(mouse.sub(w.one, new Vector2i()), button, state)) return true;
        }
        return false;
    }

    @Override
    public boolean onKey(char ch, int code) {
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Pair<Vector2i, IWidget> w = widgets.get(i);
            if (w.two.onKey(ch, code)) return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(Vector2i mouse, int diff) {
        for (int i = widgets.size() - 1; i >= 0; i++) {
            Pair<Vector2i, IWidget> w = widgets.get(i);
            if (w.two.onScroll(mouse, diff)) return true;
        }
        return false;
    }

    public WContainer add(int x, int y, IWidget widget) {
        widgets.add(new Pair<>(new Vector2i(x, y), widget));
        return this;
    }

    public WContainer remove(IWidget widget) {
        widgets.removeIf(i -> i.two == widget);
        return this;
    }
}
