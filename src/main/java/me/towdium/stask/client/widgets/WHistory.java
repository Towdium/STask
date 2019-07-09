package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

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
    static final int HEIGHT = 30;
    static final int MARGIN = 48;
    Game game;
    Map<Cluster.Processor, Rail> processors = new HashMap<>();

    public WHistory(int x, Game g) {
        game = g;
        List<Cluster.Processor> ps = game.getCluster().getLayout();
        for (int i = 0; i < ps.size(); i++) {
            Cluster.Processor p = ps.get(i);
            Rail r = new Rail(p, x, i % 2 + 1);
            put(r, 0, HEIGHT * i);
            processors.put(p, r);
        }
    }

    private void overlay(List<Graph.Work> ws, Vector2i m) {
        Overlay o = new Overlay(ws);
        Page p = Widget.page();
        Vector2i v = p.mouse().sub(m, new Vector2i()).add(Math.max(m.x - o.x / 2, 0), -o.y);
        Page.Simple s = new Page.Simple(o, v);
        p.overlay(s);
    }

    class Overlay extends WContainer {
        static final int MARGIN = 10;
        int x, y;
        WPanel panel;

        public Overlay(List<Graph.Work> ws) {
            x = Node.WIDTH + 2 * MARGIN;
            y = ws.size() * Node.HEIGHT + 2 * MARGIN;
            put(panel = new WPanel(x, y), 0, 0);
            for (int i = 0; i < ws.size(); i++) put(new Node(ws.get(i)), MARGIN, MARGIN + i * Node.HEIGHT);
        }

        @Override
        public boolean onPress(@Nullable Vector2i mouse, boolean left) {
            if (!panel.onTest(mouse)) Widget.page().overlay(null);
            return super.onPress(mouse, left);
        }

        @Override
        public boolean onKey(int code) {
            if (code == GLFW.GLFW_KEY_ESCAPE) {
                Widget.page().overlay(null);
                return true;
            } else return super.onKey(code);
        }

        class Node extends WFocus.Impl {
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
        Cluster.Processor processor;
        int multiplier;
        int x;

        public Rail(Cluster.Processor p, int x, int m) {
            processor = p;
            multiplier = m;
            this.x = x;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);

            try (Painter.State ignore = p.color(multiplier * 0x444444)) {
                p.drawRect(0, 0, MARGIN, HEIGHT);
            }
            p.drawTextRight(processor.getName(), MARGIN - 4, 2 + Painter.fontAscent);
            p.drawRect(MARGIN + game.getCount() - 1, 0, 2, HEIGHT);
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            clear();
            game.getHistory().getRecord(processor).forEach((w, p) ->
                    put(new Node(w, p.y - p.x), p.x + MARGIN, 0));
            super.onRefresh(mouse);
        }

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left) {
            if (super.onClick(mouse, left)) return true;
            else if (mouse == null) return false;
            else if (!game.isRunning() && Quad.inside(mouse, x, WHistory.HEIGHT)) {
                List<Graph.Work> ws = new ArrayList<>();
                widgets.backward((w, v) -> {
                    if (w instanceof Node) {
                        Node n = (Node) w;
                        if (!n.onTest(mouse.sub(v, new Vector2i()))) return false;
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
                int color = work instanceof Graph.Task ? 0x88CC8888 : 0x888888CC;
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
