package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Allocation;
import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Quad;
import me.towdium.stask.utils.wrap.Wrapper;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Author: Towdium
 * Date: 10/06/19
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("Duplicates")
public class WAllocation extends WContainer {

    Game game;
    Overlay overlay = null;
    Map<Processor, Rail> processors = new IdentityHashMap<>();

    public WAllocation(int x, int y, Game g) {
        game = g;
        List<Processor> ps = game.getCluster().getLayout();
        for (int i = 0; i < ps.size(); i++) {
            Processor p = ps.get(i);
            Rail r = new Rail(p, x, i % 2 + 1);
            put(r, 0, Rail.HEIGHT * i);
            processors.put(p, r);
        }
    }

    private boolean overlay(@Nullable Rail.Node n) {
        if (n == null) {
            if (overlay != null) {
                remove(overlay);
                overlay = null;
            }
            return false;
        }
        Processor p = game.getAllocation().getProcessor(n.task);
        if (p == null || n.task.getAfter().isEmpty()) return false;
        Rail r = processors.get(p);
        Vector2i v1 = find(r);
        Vector2i v2 = r.find(n);
        if (overlay != null) {
            remove(overlay);
            overlay = null;
        }
        Allocation.Node an = game.getAllocation().getNode(n.task);
        Vector2i v = v1.add(v2, new Vector2i());
        overlay = new Overlay(Objects.requireNonNull(an, "Internal error"));
        put(overlay, v.x + Rail.WIDTH / 2 - overlay.x / 2, v.y - overlay.y);
        return true;
    }

    class Rail extends WCompose {
        static final int WIDTH = 80;
        static final int HEIGHT = 30;
        static final int MARGIN = 48;

        Processor processor;
        int index = -1;
        int multiplier;
        Node ghost = null;

        public Rail(Processor p, int x, int m) {
            compose(new WDrag.Impl(x, HEIGHT) {
                @Override
                public void onReceived(Object o) {
                    if (o instanceof Task) {
                        game.getAllocation().allocate((Task) o, processor, index);
                        sync();
                    }
                }

                @Override
                public boolean onAttempt(Object o, Vector2i mouse) {
                    return o instanceof Task;
                }
            });
            processor = p;
            multiplier = m;
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            if (!WDrag.isSending()) sync();
            super.onRefresh(mouse);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            try (Painter.State ignore = p.color(multiplier * 0x444444)) {
                p.drawRect(0, 0, MARGIN, HEIGHT);
            }
            p.drawTextRight(processor.getName(), MARGIN - 4, 2 + Painter.fontAscent);

            if (!WDrag.isReceiving(this)) return;
            p.drawRect(MARGIN + getIndex(mouse) * WIDTH - 1, 0, 2, HEIGHT);
        }

        @Override
        public void onMove(Vector2i mouse) {
            super.onMove(mouse);
            int temp = getTemp();
            int total = game.getAllocation().getTasks(processor).size();
            int pos = (mouse.x - MARGIN + WIDTH / 2) / WIDTH;
            index = Math.max(Math.min(total, pos), temp) - temp;
        }

        private void sync() {
            clear();
            List<Allocation.Node> ts = game.getAllocation().getTasks(processor);
            Task t = game.getProcessor(processor).getWorking();
            if (t != null) put(new Node(t, -1), MARGIN, 0);
            for (int i = 0; i < ts.size(); i++)
                put(new Node(ts.get(i).getTask(), i), MARGIN + (t == null ? i : i + 1) * WIDTH, 0);
            if (ghost != null) put(ghost, 0, 0);
        }

        private int getIndex(Vector2i mouse) {
            int temp = getTemp();
            int total = game.getAllocation().getTasks(processor).size();
            int pos = (mouse.x - MARGIN + WIDTH / 2) / WIDTH;
            return Math.max(Math.min(total + temp, pos), temp);
        }

        private int getTemp() {
            Wrapper<Integer> i = new Wrapper<>(0);
            widgets.forward((w, v) -> {
                if (w instanceof Node) {
                    Node n = (Node) w;
                    Processor p = game.getAllocation().getProcessor(n.task);
                    if (p == null) i.v++;
                }
                return false;
            });
            return i.v;
        }

        class Node extends WCompose implements WArea {
            Task task;
            int idx;
            boolean visible = true;

            public Node(Task t, int i) {
                compose(new WDrag.Impl(WIDTH, HEIGHT) {
                    @Nullable
                    @Override
                    public Object onStarting() {
                        overlay(null);
                        game.getAllocation().remove(processor, idx);
                        ghost = Node.this;
                        sync();
                        visible = false;
                        return task;
                    }

                    @Override
                    public void onRejected() {
                        game.getAllocation().allocate(task, processor, idx);
                        ghost = null;
                        sync();
                    }

                    @Override
                    public void onSucceeded() {
                        ghost = null;
                        sync();
                    }
                });
                compose(new WFocus.Impl(WIDTH, HEIGHT) {
                    @Nullable
                    @Override
                    public Graph.Work onFocus() {
                        return task;
                    }

                    @Override
                    public boolean onTest(@Nullable Vector2i mouse) {
                        return WDrag.isSending(Node.this) || super.onTest(mouse);
                    }
                });
                task = t;
                idx = i;
            }

            @Override
            public void onMove(Vector2i mouse) {
                super.onMove(mouse);
                index = getIndex(mouse);
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                super.onDraw(p, mouse);
                if (visible) {

                    float progress;
                    Game.Status s = game.getProcessor(processor);
                    if (task == s.getWorking()) progress = s.getProgress();
                    else if (game.getAllocation().allocated(task)) progress = 0;
                    else progress = 1;

                    try (Painter.State ignore = p.color(0x888888)) {
                        p.drawRect(0, 0, (int) (WIDTH * progress), HEIGHT);
                    }

                    int color = WFocus.isFocused(task) ? 0xCCCCCC : 0x888888;
                    try (Painter.State ignore = p.color(color)) {
                        p.drawRect(0, 0, WIDTH, HEIGHT, 2);
                    }
                    p.drawTextRight(task.getName(), WIDTH - 6, Painter.fontAscent + 2);
                }

                if (WDrag.isSending(this) && !WDrag.isReceiving())
                    WTask.drawTask(p, task, mouse.x - WTask.WIDTH / 2,
                            mouse.y - WTask.HEIGHT / 2);
            }

            @Override
            public boolean onClick(@Nullable Vector2i mouse, boolean left) {
                return super.onClick(mouse, left) || (!WDrag.isSending() && onTest(mouse) && overlay(this));
            }

            @Override
            public boolean onTest(@Nullable Vector2i mouse) {
                return Quad.inside(mouse, WIDTH, HEIGHT);
            }
        }
    }

    class Overlay extends WCompose {
        static final int WIDTH = 60;
        static final int HEIGHT = 30;
        static final int MARGIN = 10;
        Allocation.Node node;
        WPanel panel;
        List<Graph.Comm> comms;
        Node ghost;
        int x, y;
        int index = -1;

        public Overlay(Allocation.Node n) {
            compose(new WDrag() {
                @Override
                public boolean onTest(@Nullable Vector2i mouse) {
                    return true;
                }

                @Override
                public void onReceived(Object o) {
                    Overlay.this.comms.add(index, (Graph.Comm) o);
                    sync();
                    node.setComms(comms);
                }

                @Override
                public boolean onAttempt(Object o, Vector2i mouse) {
                    return o instanceof Graph.Comm;
                }
            });
            node = n;
            comms = node.getComms();
            y = HEIGHT * comms.size() + 2 * MARGIN;
            x = WIDTH + 2 * MARGIN;
            panel = new WPanel(x, y);
        }

        @Override
        public boolean onPress(@Nullable Vector2i mouse, boolean left) {
            if (!panel.onTest(mouse)) overlay(null);
            return super.onPress(mouse, left);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            if (!WDrag.isReceiving(this)) return;
            p.drawRect(MARGIN, MARGIN + getIndex(mouse) * HEIGHT - 1, WIDTH, 2);
        }

        private int getIndex(Vector2i mouse) {
            int total = comms.size();
            int pos = (mouse.y - MARGIN + HEIGHT / 2) / HEIGHT;
            return Math.max(Math.min(total, pos), 0);
        }

        @Override
        public boolean onKey(int code) {
            if (code == GLFW.GLFW_KEY_ESCAPE) {
                overlay(null);
                return true;
            } else return super.onKey(code);
        }

        @Override
        public void onMove(Vector2i mouse) {
            super.onMove(mouse);
            index = getIndex(mouse);
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            if (!WDrag.isSending()) sync();
            super.onRefresh(mouse);
        }

        private void sync() {
            clear();
            put(panel, 0, 0);
            for (int i = 0; i < comms.size(); i++)
                put(new Node(comms.get(i)), MARGIN, MARGIN + i * HEIGHT);
            if (ghost != null) put(ghost, 0, 0);
        }

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left) {
            if (!super.onClick(mouse, left)) overlay(null);
            return true;
        }

        class Node extends WCompose {
            Graph.Comm comm;
            boolean visible = true;

            public Node(Graph.Comm c) {
                compose(new WDrag.Impl(WIDTH, HEIGHT) {
                    @Override
                    public boolean onTest(@Nullable Vector2i mouse) {
                        return visible && super.onTest(mouse);
                    }

                    @Nullable
                    @Override
                    public Object onStarting() {
                        visible = false;
                        ghost = Node.this;
                        comms.remove(comm);
                        sync();
                        return comm;
                    }

                    @Override
                    public void onSucceeded() {
                        super.onSucceeded();
                    }

                    @Override
                    public void onRejected() {
                        throw new RuntimeException("Internal error");
                    }
                });
                compose(new WFocus.Impl(WIDTH, HEIGHT) {
                    @Nullable
                    @Override
                    public Graph.Work onFocus() {
                        return comm;
                    }

                    @Override
                    public boolean onTest(@Nullable Vector2i mouse) {
                        return WDrag.isSending(Node.this) || super.onTest(mouse);
                    }
                });
                comm = c;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                super.onDraw(p, mouse);
                if (!visible) return;
                try (Painter.State ignore = p.color(0x666666)) {
                    p.drawRect(0, 0, WIDTH, HEIGHT);
                }
                p.drawTextRight(comm.getSrc().getName(), WIDTH - 4, 2 + Painter.fontAscent);
                if (WFocus.isFocused(comm)) {
                    try (Painter.State ignore = p.color(0xAAFFFFFF)) {
                        p.drawRect(0, 0, WIDTH, HEIGHT);
                    }
                }
            }
        }
    }
}
