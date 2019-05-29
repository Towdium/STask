package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.client.Window.Mouse;
import org.joml.Vector2i;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Author: Towdium
 * Date: 09/03/19
 */
public class WContainer implements Widget {
    protected WidgetMap widgets = new WidgetMap();
    float transparency = 0;

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        Runnable r = () -> widgets.forward((w, v) -> {
            try (Painter.SMatrix mat = p.matrix()) {
                mat.translate(v.x, v.y);
                w.onDraw(p, mouse.sub(v, new Vector2i()));
            }
            return false;
        });
        if (transparency == 0) r.run();
        else {
            int color = ((int) (255 * transparency) << 24) + 0xFFFFFF;
            try (Painter.State s = p.color(color)) {
                r.run();
            }
        }
    }

    public void setTransparency(float f) {
        transparency = f;
    }

    @Override
    public boolean onTooltip(Vector2i mouse, List<String> tooltip) {
        return widgets.backward((w, v) -> w.onTooltip(mouse.sub(v, new Vector2i()), tooltip));
    }

    @Override
    public boolean onMouse(Vector2i mouse, Mouse button, boolean state) {
        return widgets.backward((w, v) -> w.onMouse(mouse.sub(v, new Vector2i()), button, state));
    }

    @Override
    public boolean onKey(char ch, int code) {
        return widgets.backward((w, v) -> w.onKey(ch, code));
    }

    @Override
    public boolean onScroll(Vector2i mouse, int diff) {
        return widgets.backward((w, v) -> w.onScroll(mouse, diff));
    }

    public WContainer put(Widget widget, int x, int y) {
        widgets.put(widget, new Vector2i(x, y));
        return this;
    }

    public WContainer setX(Widget widget, int x) {
        widgets.get(widget).x = x;
        return this;
    }

    public WContainer setY(Widget widget, int y) {
        widgets.get(widget).y = y;
        return this;
    }

    public WContainer remove(Widget widget) {
        widgets.remove(widget);
        return this;
    }

    public WContainer clear() {
        widgets.clear();
        return this;
    }

    public Vector2i find(Widget widget) {
        return widgets.get(widget);
    }

    static class WidgetMap {
        Entry head, tail;
        IdentityHashMap<Widget, Entry> map = new IdentityHashMap<>();

        {
            head = new Entry(null, null);
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
