package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Colour;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Author: Towdium
 * Date: 29/07/2019
 */
@ParametersAreNonnullByDefault
public class WList extends WCompose {
    List<String> strs;
    int x, y, pos = 0, select = -1;
    WContainer nodes = new WContainer();
    WContainer mask = new WContainer();
    WBar bar;
    ListenerValue<WList, Integer> listener = null;
    boolean drag;

    public WList(List<String> strs, int x, int y) {
        this(strs, x, y, false);
    }

    public WList(List<String> strs, int x, int y, boolean drag) {
        this.strs = strs;
        this.x = x;
        this.y = y;
        this.drag = drag;
        put(new WPanel(x, y), 0, 0);
        put(mask, 0, 0);
        put(bar = new WBar(y, true), x, 0);
        mask.setMask(0, 0, x, y);
        mask.put(nodes, 0, 0);
        refresh();
        bar.setListener((w, o, n) -> {
            int h = strs.size() * Node.HEIGHT;
            pos = (int) (Math.max(h - y, 0) * n);
            mask.put(nodes, 0, -pos);
        });
    }

    public String get(int idx) {
        return strs.get(idx);
    }

    public void setListener(ListenerValue<WList, Integer> listener) {
        this.listener = listener;
    }

    public int getSelect() {
        return select;
    }

    private void refresh() {
        nodes.clear();
        for (int i = 0; i < strs.size(); i++) {
            int c = i % 2 == 0 ? Colour.ACTIVE1 : Colour.ACTIVE2;
            nodes.put(new Node(strs.get(i), x, c, i), 0, Node.HEIGHT * i);
        }
        int h = strs.size() * Node.HEIGHT;
        bar.setRatio(Math.min(1, y / (float) h));
        pos = Math.min(pos, Math.max(h - y, 0));
        bar.setPos(pos / (float) Math.max(h - y, 0));
        mask.put(nodes, 0, -pos);
    }

    public void setStrs(List<String> strs) {
        this.strs = strs;
        int old = select;
        select = -1;
        if (listener != null) listener.invoke(this, old, select);
        refresh();
    }

    class Node extends WDrag.Impl {
        static final int HEIGHT = 28;
        int color, index;
        String text;

        public Node(String text, int x, int color, int index) {
            super(x, HEIGHT);
            this.color = color;
            this.index = index;
            this.text = text;
        }

        @Nullable
        @Override
        public Object onStarting() {
            return drag ? text : super.onStarting();
        }

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left) {
            if (onTest(mouse)) {
                int old = select;
                select = index;
                if (listener != null) listener.invoke(WList.this, old, select);
                return true;
            } else return false;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            try (Painter.State ignore = p.color(select == index ? Colour.ACTIVE3 : color)) {
                p.drawRect(0, 0, x, HEIGHT);
            }
            p.drawText(text, 4, Painter.fontAscent);
            if (WDrag.isSending(this)) {
                Widget.page().overlay(new Page.Once((a, m) -> {
                    try (Painter.SMatrix matrix = a.matrix()) {
                        matrix.translate(m.x - x / 2, m.y - HEIGHT / 2);
                        try (Painter.State ignore = a.color(color)) {
                            a.drawRect(0, 0, x, HEIGHT);
                        }
                        a.drawText(text, 4, Painter.fontAscent);
                    }
                }));
            }
        }
    }
}
