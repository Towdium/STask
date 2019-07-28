package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Colour;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Event.ETask;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.logic.Schedule;
import me.towdium.stask.utils.Quad;
import me.towdium.stask.utils.wrap.Wrapper;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static me.towdium.stask.logic.Event.Bus.BUS;


/**
 * Author: Towdium
 * Date: 10/06/19
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("Duplicates")
public class WSchedule extends WContainer {  // TODO remove task
    Game game;
    Map<Processor, Rail> processors = new IdentityHashMap<>();
    public static final int HEIGHT = Rail.HEIGHT * 4;

    public WSchedule(int x, Game g) {
        game = g;
        List<Processor> ps = game.getCluster().getLayout();
        for (int i = 0; i < ps.size(); i++) {
            Processor p = ps.get(i);
            Rail r = new Rail(p, x, i % 2 == 0);
            put(r, 0, Rail.HEIGHT * i);
            processors.put(p, r);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(Colour.DISABLED)) {
            p.drawRect(0, 0, Rail.MARGIN, 4 * Rail.HEIGHT);
        }
        super.onDraw(p, mouse);
    }

    private void overlay(Rail.Node n, Vector2i m) {
        Schedule.Node d = game.getSchedule().getNode(n.task);
        Overlay o = new Overlay(Objects.requireNonNull(d, "Internal error"));
        if (d.getComms().size() <= 1) return;
        Page r = Widget.page();
        Vector2i v = r.mouse().sub(m).add(Rail.WIDTH / 2 - o.x / 2, -o.y);
        Page.Overlay s = new Page.Overlay();
        s.put(o, v);
        r.overlay(s);
    }

    class Rail extends WCompose {
        static final int WIDTH = 60;
        static final int HEIGHT = 30;
        static final int MARGIN = 70;

        Processor processor;
        int index = -1;
        boolean highlight;
        Node ghost = null;

        public Rail(Processor p, int x, boolean h) {
            compose(new WDrag.Impl(x, HEIGHT) {
                @Override
                public void onReceived(Object o) {
                    if (o instanceof Task) {
                        Task t = (Task) o;
                        BUS.post(new ETask.Schedule(t, processor));
                        game.getSchedule().allocate(t, processor, index);
                        sync();
                    }
                }

                @Override
                public boolean onAttempt(Object o, Vector2i mouse) {
                    return size() < 16 && o instanceof Task && BUS.attempt(new ETask.Schedule((Task) o, processor));
                }
            });
            processor = p;
            highlight = h;
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            if (!WDrag.isSending()) sync();
            super.onRefresh(mouse);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            try (Painter.State ignore = p.color(highlight ? Colour.ACTIVE2 : Colour.ACTIVE1)) {
                p.drawRect(0, 0, MARGIN, HEIGHT);
            }
            p.drawTextRight(processor.getName(), MARGIN - 4, 2 + Painter.fontAscent);

            if (!WDrag.isReceiving(this)) return;
            p.drawRect(MARGIN + getIndex(mouse) * WIDTH - 1, 0, 2, HEIGHT);
        }

        @Override
        public void onMove(Vector2i mouse) {
            super.onMove(mouse);
            index = getIndex(mouse);
        }

        private void sync() {
            clear();
            List<Schedule.Node> ts = game.getSchedule().getTasks(processor);
            Task t = game.getProcessor(processor).getWorking();
            if (t != null) put(new Node(t, -1), MARGIN, 0);
            for (int i = 0; i < ts.size(); i++)
                put(new Node(ts.get(i).getTask(), i), MARGIN + (t == null ? i : i + 1) * WIDTH, 0);
            if (ghost != null) put(ghost, 0, 0);
        }

        private int getIndex(Vector2i mouse) {
            int temp = getTemp();
            int total = game.getSchedule().getTasks(processor).size();
            int pos = (mouse.x - MARGIN + WIDTH / 2) / WIDTH;
            return Math.max(Math.min(total + temp, pos), temp);
        }

        // get amount of temp nodes showing executing tasks
        private int getTemp() {
            Wrapper<Integer> i = new Wrapper<>(0);
            widgets.forward((w, v) -> {
                if (w instanceof Node) {
                    Node n = (Node) w;
                    Processor p = game.getSchedule().getProcessor(n.task);
                    if (p == null && w != ghost) i.v++;
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
                        if (BUS.attempt(new ETask.Pick(task, WSchedule.this))) {
                            game.getSchedule().remove(processor, idx);
                            ghost = Node.this;
                            sync();
                            visible = false;
                            BUS.post(new ETask.Pick(task, WSchedule.this));
                            return task;
                        } else return null;
                    }

                    @Override
                    public void onRejected() {
                        game.getSchedule().allocate(task, processor, idx);
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
                    else if (game.getSchedule().allocated(task)) progress = 0;
                    else progress = 1;

                    try (Painter.State ignore = p.color(Colour.ACTIVE3)) {
                        p.drawRect(0, 0, (int) (WIDTH * progress), HEIGHT);
                    }

                    int color = WFocus.isFocused(task) ? 0xCCCCCC : Colour.ACTIVE3;
                    try (Painter.State ignore = p.color(color)) {
                        p.drawRect(0, 0, WIDTH, HEIGHT, 2);
                    }
                    p.drawTextRight(task.getName(), WIDTH - 6, Painter.fontAscent + 2);
                }
                if (WDrag.isSending(this) && !WDrag.isReceiving())
                    Widget.page().overlay(new Page.Once((a, m) -> WTask.drawTask(a, task, m.x, m.y)));
            }

            @Override
            public boolean onClick(@Nullable Vector2i mouse, boolean left) {
                Cluster c = game.getCluster();
                if (super.onClick(mouse, left)) return true;
                else if (mouse == null || c.getComm() == 0 || c.getPolicy().multiple) return false;
                else if (onTest(mouse)) {
                    overlay(this, mouse);
                    return true;
                } else return false;
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
        Schedule.Node node;
        List<Graph.Comm> comms;
        Node ghost;
        int x, y;
        int index = -1;

        public Overlay(Schedule.Node n) {
            node = n;
            comms = node.getComms();
            y = HEIGHT * comms.size() + 2 * MARGIN;
            x = WIDTH + 2 * MARGIN;
            compose(new WDrag() {
                @Override
                public boolean onTest(@Nullable Vector2i mouse) {
                    return true;
                }

                @Override
                public void onReceived(Object o) {
                    comms.add(index, (Graph.Comm) o);
                    BUS.post(new ETask.Comm(node.getTask(), comms));
                    sync();
                    node.setComms(comms);
                }

                @Override
                public boolean onAttempt(Object o, Vector2i mouse) {
                    if (!(o instanceof Graph.Comm)) return false;
                    List<Graph.Comm> l = new ArrayList<>(Overlay.this.comms);
                    l.add(index, (Graph.Comm) o);
                    return BUS.attempt(new ETask.Comm(node.getTask(), l));
                }
            });
            compose(new WOverlay(x, y));
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
        public void onMove(Vector2i mouse) {
            index = getIndex(mouse);
            super.onMove(mouse);
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            if (!WDrag.isSending()) sync();
            super.onRefresh(mouse);
        }

        private void sync() {
            clear();
            for (int i = 0; i < comms.size(); i++)
                put(new Node(comms.get(i)), MARGIN, MARGIN + i * HEIGHT);
            if (ghost != null) put(ghost, 0, 0);
        }

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left) {
            if (!super.onClick(mouse, left)) Widget.page().overlay(null);
            return true;
        }

        class Node extends WCompose {
            Graph.Comm comm;
            boolean visible = true;
            int restore = -1;

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
                        restore = comms.indexOf(comm);
                        comms.remove(restore);
                        sync();
                        return comm;
                    }

                    @Override
                    public void onSucceeded() {
                        super.onSucceeded();
                        restore = -1;
                        ghost = null;
                    }

                    @Override
                    public void onRejected() {
                        comms.add(restore, comm);
                        restore = -1;
                        ghost = null;
                        sync();
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
