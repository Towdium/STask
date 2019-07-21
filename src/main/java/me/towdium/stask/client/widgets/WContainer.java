package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Author: Towdium
 * Date: 09/03/19
 */
@ParametersAreNonnullByDefault
public class WContainer implements Widget {
    WidgetMap widgets = new WidgetMap();
    Quad mask = null;

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        Painter.State s = null;
        if (mask != null)
            s = p.mask(mask);
        widgets.forward((w, v) -> {
            try (Painter.SMatrix mat = p.matrix()) {
                mat.translate(v.x, v.y);
                w.onDraw(p, mouse.sub(v, new Vector2i()));
            }
            return false;
        });
        if (s != null) s.close();
    }

    @Override
    public boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
        Vector2i m = (mask != null && !mask.inside(mouse)) ? null : mouse;
        return widgets.backward((w, v) -> w.onTooltip(m == null ? null : m.sub(v, new Vector2i()), tooltip));
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left) {
        Vector2i m = (mask != null && !mask.inside(mouse)) ? null : mouse;
        return widgets.backward((w, v) -> w.onClick(m == null ? null : m.sub(v, new Vector2i()), left));
    }

    @Override
    public void onMove(Vector2i mouse) {
        widgets.forward((w, v) -> {
            w.onMove(mouse.sub(v, new Vector2i()));
            return false;
        });
    }

    @Override
    public boolean onKey(int code) {
        return widgets.backward((w, v) -> w.onKey(code));
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        widgets.forward((w, v) -> {
            w.onRefresh(mouse.sub(v, new Vector2i()));
            return false;
        });
    }

    @Override
    public boolean onScroll(@Nullable Vector2i mouse, int diff) {
        if (mask != null && !mask.inside(mouse)) return false;
        return widgets.backward((w, v) -> w.onScroll(mouse, diff));
    }

    @Override
    public boolean onDrag(@Nullable Vector2i mouse, boolean left) {
        Vector2i m = (mask != null && !mask.inside(mouse)) ? null : mouse;
        return widgets.backward((w, v) -> w.onDrag(m == null ? null : m.sub(v, new Vector2i()), left));
    }

    @Override
    public boolean onPress(@Nullable Vector2i mouse, boolean left) {
        Vector2i m = (mask != null && !mask.inside(mouse)) ? null : mouse;
        return widgets.backward((w, v) -> w.onPress(m == null ? null : m.sub(v, new Vector2i()), left));
    }

    @Override
    public boolean onDrop(boolean left) {
        return widgets.backward((w, v) -> w.onDrop(left));
    }

    public WContainer put(Widget widget, int x, int y) {
        widgets.put(widget, new Vector2i(x, y));
        return this;
    }

    public WContainer put(Widget widget, Vector2i p) {
        widgets.put(widget, p);
        return this;
    }

    public void setMask(int xp, int yp, int xs, int ys) {
        mask = new Quad(xp, yp, xs, ys);
    }

    public WContainer remove(Widget widget) {
        widget.onRemove();
        widgets.remove(widget);
        return this;
    }

    public WContainer clear() {
        remove();
        widgets.clear();
        return this;
    }

    public int size() {
        return widgets.map.size();
    }

    @Override
    public void onRemove() {
        remove();
    }

    protected void remove() {
        widgets.forward((w, v) -> {
            w.onRemove();
            return false;
        });
    }

    public Vector2i find(Widget widget) {
        return widgets.get(widget);
    }

    static class WidgetMap {
        Entry head, tail;
        IdentityHashMap<Widget, Entry> map = new IdentityHashMap<>();

        {
            //noinspection ConstantConditions
            head = new Entry(null, null);
            //noinspection ConstantConditions
            tail = new Entry(null, null);
            head.next = tail;
            tail.last = head;
        }

        public void put(Widget w, Vector2i v) {
            Entry e = map.get(w);
            if (e != null) e.vec = v;
            else {
                map.put(w, e = new Entry(w, v));
                e.link(tail);
            }
        }

        public Vector2i get(Widget w) {
            return map.get(w).vec;
        }

        public void remove(Widget w) {
            Entry e = map.get(w);
            if (e != null) {
                map.remove(w);
                e.unlink();
            }
        }

        public void clear() {
            map.clear();
            head.next = tail;
            tail.last = head;
        }

        public boolean forward(BiPredicate<Widget, Vector2i> p) {
            Entry e = head.next;
            while (e.next != null) {
                if (p.test(e.wgt, e.vec)) return true;
                e = e.next;
            }
            return false;
        }

        public boolean backward(BiPredicate<Widget, Vector2i> p) {
            Entry e = tail.last;
            while (e.last != null) {
                if (p.test(e.wgt, e.vec)) return true;
                e = e.last;
            }
            return false;
        }

        static class Entry {
            Entry last, next;
            Vector2i vec;
            Widget wgt;

            public Entry(Widget wgt, Vector2i vec) {
                this.vec = vec;
                this.wgt = wgt;
            }

            public void unlink() {
                last.next = next;
                next.last = last;
            }

            public void link(Entry next) {
                this.next = next;
                last = next.last;
                last.next = this;
                next.last = this;
            }
        }
    }
}
