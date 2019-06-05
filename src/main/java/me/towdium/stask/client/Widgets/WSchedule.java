package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Schedule;
import me.towdium.stask.utils.Log;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Author: Towdium
 * Date: 29/05/19
 */
@ParametersAreNonnullByDefault
public class WSchedule extends WContainer {
    public static final int MARGIN = 30;
    public static final int HEIGHT = 20;
    public static final float MULTIPLIER = 0.05f;
    boolean debug;
    Schedule schedule;
    Cluster cluster;
    Map<Schedule.Assignment, Node> assignments = new IdentityHashMap<>();
    Map<Cluster.Processor, Rail> processors = new IdentityHashMap<>();

    public WSchedule(int x, int y, Schedule s, Cluster c) {
        schedule = s;
        cluster = c;
        setMask(MARGIN, 0, x - MARGIN, y);
        List<Cluster.Processor> ps = cluster.getLayout();

        // TODO load existing
        for (int i = 0; i < ps.size(); i++) {
            Cluster.Processor p = ps.get(i);
            Rail r = new Rail(p, x - MARGIN);
            put(r, MARGIN, i * HEIGHT);
            processors.put(p, r);
            //SortedMap<Float, Schedule.Assignment> as = schedule.getProcessors().get(p);
//            if (as == null) continue;
//            for (Map.Entry<Float, Schedule.Assignment> j : as.entrySet()) {
//                Node n = new Node(j.getValue());
//                assignments.put(j.getValue(), n);
//                r.put(n, (int) (j.getKey() * MULTIPLIER), 0);
//            }
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        List<Cluster.Processor> ps = cluster.getLayout();
        for (int i = 0; i < ps.size(); i++) {
            try (Painter.State ignore = p.color((i % 2 + 1) * 0x444444)) {
                p.drawRect(0, i * HEIGHT, MARGIN, HEIGHT);
            }
            p.drawTextRight(ps.get(i).getName(), MARGIN - 2, i * HEIGHT + 2 + Painter.fontAscent);
        }
        super.onDraw(p, mouse);
    }

    class Node extends WContainer {
        Schedule.Assignment asmt;
        Drag drag;
        Highlight highlight;
        boolean visible = true;
        boolean ghost = false;

        public Node(Schedule.Assignment a) {
            asmt = a;
            int left = (int) (MULTIPLIER * a.getStart());
            int right = (int) (MULTIPLIER * a.getEnd());
            drag = new Drag(right - left);
            highlight = new Highlight();
            put(drag, 0, 0);
            put(highlight, 0, 0);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            if (!visible) return;
            try (Painter.State ignore1 = p.color(ghost ? 0.5f : 0f);
                 Painter.State ignore2 = p.color(0x888888)) {
                p.drawRect(0, 0, drag.x - 2, 2);
                p.drawRect(0, 2, 2, drag.y - 2);
                p.drawRect(drag.x - 2, 0, 2, drag.y - 2);
                p.drawRect(2, drag.y - 2, drag.x - 2, 2);
            }
            super.onDraw(p, mouse);
        }

        class Drag extends WDrag {
            public Drag(int x) {
                super(x, HEIGHT);
            }

            @Override
            public void onReceived(Object o) {

            }

            @Override
            public void onRejected() {
                // TODO
            }

            @Override
            public void onSucceeded() {
                remove(this);
            }

            @Override
            public @Nullable
            Object onStarting() {  // TODO change to move
                return null;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                if (sender == this && receiver == null && asmt.getWork() instanceof Graph.Task) {
                    WGraph.drawTask(p, mouse.x, mouse.y, (Graph.Task) asmt.getWork());
                }
            }
        }

        class Highlight extends WHighlight {
            @Override
            public Graph.Work onHighlight(@Nullable Vector2i mouse) {
                return drag.onTest(mouse) ? asmt.getWork() : null;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
            }
        }
    }

    class Rail extends WContainer {
        Drag drag;
        Cluster.Processor processor;
        Graph.Task active;
        List<Node> ghost = null;

        public Rail(Cluster.Processor p, int x) {
            processor = p;
            put(drag = new Drag(x + MARGIN), -MARGIN, 0);
        }

        class Drag extends WDrag {
            public Drag(int x) {
                super(x, HEIGHT);
            }

            @Override
            public void onMove(Vector2i mouse) {
                if (ghost != null) {
                    if (debug) Log.client.info("move");
                    cancel();
                    assign(mouse);
                }
            }

            @Override
            public void onReceived(Object o) {
                super.onReceived(o);
                Objects.requireNonNull(ghost, "Internal error");
                for (Node i : ghost) {
                    assignments.put(i.asmt, i);
                    i.ghost = false;
                }
                ghost = null;
            }

            @Override
            public boolean onTest(Object o, Vector2i mouse) {
                return o instanceof Graph.Task;
            }

            @Override
            public void onEnter(Object o, Vector2i mouse) {
                if (debug) Log.client.info("enter");
                active = (Graph.Task) o;
                assign(mouse);
            }

            @Override
            public void onLeaving() {
                if (debug) Log.client.info("leave");
                active = null;
                cancel();
            }

            private void cancel() {
                if (ghost == null) return;
                for (Node n : ghost) {
                    Rail r = processors.get(n.asmt.getProcessor());
                    r.remove(n);
                    Log.client.debug("remove " + n.asmt.getProcessor());
                }
                ghost = null;
            }

            private void assign(Vector2i mouse) {
                int point = (int) ((mouse.x - MARGIN) / MULTIPLIER) - processor.cost(active) / 2;
                Schedule.TimeAxis ta = schedule.attempt(active, processor);
                Integer f = ta.earliest(point);
                if (f == null) {
                    if (debug) Log.client.debug("failed");
                    return;
                }
                ghost = new ArrayList<>();
                List<Schedule.Assignment> as = schedule.assign(active, processor, f);
                // TODO handle move
                if (as == null) throw new RuntimeException("Internal error");
                for (Schedule.Assignment i : as) {
                    Node n = new Node(i);
                    n.ghost = true;
                    ghost.add(n);
                    Rail r = processors.get(i.getProcessor());
                    int pos = (int) (i.getStart() * MULTIPLIER);
                    r.put(n, pos, 0);
                    Log.client.debug("add " + i.getProcessor() + " " + pos);
                }
            }
        }
    }
}
