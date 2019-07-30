package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Colour;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 17/06/19
 */
@ParametersAreNonnullByDefault
public class WHistory extends WContainer {
    public static final int HEIGHT = WHistory.Rail.HEIGHT * 4 + WBar.SIZE;
    Game game;
    Map<Cluster.Processor, Rail> processors = new HashMap<>();
    int latest = 0;
    WBar bar;
    int width;
    int offset = 0;

    public WHistory(int x, Game g) {
        game = g;
        width = x - 2;
        List<Cluster.Processor> ps = game.getCluster().getLayout();
        for (int i = 0; i < ps.size(); i++) {
            Cluster.Processor p = ps.get(i);
            Rail r = new Rail(p, width, i % 2 == 0);
            put(r, 0, Rail.HEIGHT * i);
            processors.put(p, r);
        }
        bar = new WBar(width - Rail.MARGIN, false).setListener((w, o, n) -> {
            int count = game.getCount();
            if (count < width - Rail.MARGIN) offset = 0;
            else offset = (int) ((count - width + Rail.MARGIN) * n);
            processors.values().forEach(i -> i.setOffset(offset));
        });
        put(bar, Rail.MARGIN, 4 * Rail.HEIGHT);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(Colour.DISABLED)) {
            p.drawRect(0, 0, Rail.MARGIN, 4 * Rail.HEIGHT);
        }
        super.onDraw(p, mouse);
        try (Painter.State ignore = p.mask(Rail.MARGIN - 1, 0, width - Rail.MARGIN + 2, HEIGHT)) {
            p.drawRect(Rail.MARGIN + game.getCount() / 4 - 1 - offset, 0, 2, 4 * Rail.HEIGHT);
        }
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        int c = game.getCount();
        if (c != latest) {
            latest = c;
            bar.setPos(1);
            bar.setRatio(latest == 0 ? 1 : Math.min((width - Rail.MARGIN) / (float) (latest / 4), 1));
            offset = Math.max(0, latest / 4 - (width - Rail.MARGIN));
            processors.values().forEach(i -> i.setOffset(offset));
        }
        super.onRefresh(mouse);
    }

    private void overlay(List<Graph.Work> ws, Vector2i m) {
        Overlay o = new Overlay(ws);
        Page p = Widget.page();
        Vector2i v = p.mouse().sub(m, new Vector2i()).add(Math.max(m.x - o.x / 2, 0), -o.y);
        Page.Overlay s = new Page.Overlay();
        s.put(o, v);
        p.overlay(s);
    }

    static class Overlay extends WContainer {
        static final int MARGIN = 10;
        int x, y;

        public Overlay(List<Graph.Work> ws) {
            x = Node.WIDTH + 2 * MARGIN;
            y = ws.size() * Node.HEIGHT + 2 * MARGIN;
            put(new WOverlay(x, y), 0, 0);
            for (int i = 0; i < ws.size(); i++) put(new Node(ws.get(i)), MARGIN, MARGIN + i * Node.HEIGHT);
        }

        static class Node extends WFocus.Impl {
            public static final int WIDTH = 170;
            public static final int HEIGHT = 28;
            Graph.Work work;

            public Node(Graph.Work work) {
                super(WIDTH, HEIGHT);
                this.work = work;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                super.onDraw(p, mouse);
                try (Painter.State ignore = p.color(0x666666)) {
                    p.drawRect(0, 0, WIDTH, HEIGHT);
                }
                if (work instanceof Graph.Task) {
                    Graph.Task t = (Graph.Task) work;
                    p.drawTextRight(t.getName(), WIDTH - 4, 2 + Painter.fontAscent);
                } else if (work instanceof Graph.Comm) {
                    Graph.Comm c = (Graph.Comm) work;
                    String s = c.getSrc().getName() + " \u25B6 " + c.getDst().getName();
                    p.drawTextRight(s, WIDTH - 4, 2 + Painter.fontAscent);
                }
                if (WFocus.isFocused(work)) {
                    try (Painter.State ignore = p.color(0xAAFFFFFF)) {
                        p.drawRect(0, 0, WIDTH, HEIGHT);
                    }
                }
            }

            @Nullable
            @Override
            public Object onFocus() {
                return work;
            }
        }
    }

    class Rail extends WContainer {
        static final int HEIGHT = 30;
        static final int MARGIN = 70;
        Cluster.Processor processor;
        boolean highlight;
        int x;
        int offset = 0;
        WContainer container = new WContainer();

        public Rail(Cluster.Processor p, int x, boolean h) {
            processor = p;
            highlight = h;
            this.x = x;
            put(container, MARGIN, 0);
        }

        public void setOffset(int x) {
            offset = x;
            put(container, -offset, 0);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.State ignore = p.mask(MARGIN, 0, x - MARGIN, HEIGHT)) {
                super.onDraw(p, mouse);
            }
            try (Painter.State ignore = p.color(highlight ? Colour.ACTIVE2 : Colour.ACTIVE1)) {
                p.drawRect(0, 0, MARGIN, HEIGHT);
            }
            p.drawTextRight(processor.getName(), MARGIN - 4, 2 + Painter.fontAscent);
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            container.clear();
            game.getHistory().getRecord(processor).forEach((w, p) ->
                    container.put(new Node(w, (p.y - p.x) / 4), p.x / 4 + MARGIN, 0));
            super.onRefresh(mouse);
        }

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left) {
            if (super.onClick(mouse, left)) return true;
            else if (mouse == null) return false;
            else if (!game.isRunning() && Quad.inside(mouse, x, HEIGHT)) {
                List<Graph.Work> ws = new ArrayList<>();
                container.widgets.backward((w, v) -> {
                    if (w instanceof Node) {
                        Node n = (Node) w;
                        if (!n.onTest(mouse.sub(v, new Vector2i()).add(offset, 0))) return false;
                        Graph.Work o = n.onFocus();
                        if (o != null) ws.add(o);
                    }
                    return false;
                });
                if (ws.size() > 1) {
                    overlay(ws, mouse);
                    return true;
                } else return false;
            } else return false;
        }

        class Node extends WFocus.Impl {
            Graph.Work work;
            int width;

            public Node(Graph.Work w, int i) {
                super(i, HEIGHT);
                work = w;
                width = i;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                int color = work instanceof Graph.Task ? 0x88DD6666 : 0x8866BBDD;
                try (Painter.State ignore = p.color(color)) {
                    p.drawRect(0, 0, width, HEIGHT);
                }
                if (WFocus.isFocused(work)) {
                    try (Painter.State ignore = p.color(0xCCFFFFFF)) {
                        p.drawRect(0, 0, width, HEIGHT);
                    }
                }
            }

            @Nullable
            @Override
            public Graph.Work onFocus() {
                return work;
            }
        }
    }
}
