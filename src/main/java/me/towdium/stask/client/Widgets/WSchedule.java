package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Window;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Schedule;
import me.towdium.stask.utils.Log;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Author: Towdium
 * Date: 29/05/19
 */
public class WSchedule extends WContainer {
    public static final int MARGIN = 30;
    public static final int HEIGHT = 20;
    public static final int MULTIPLIER = 10;
    Schedule schedule;
    Cluster cluster;
    Map<Schedule.Assignment, Node> assignments = new IdentityHashMap<>();
    Map<Cluster.Processor, Rail> processors = new IdentityHashMap<>();

    public WSchedule(int x, int y, Schedule s, Cluster c) {
        schedule = s;
        cluster = c;
        setMask(MARGIN, 0, x - MARGIN, y);
        List<Cluster.Processor> ps = cluster.getLayout();

        for (int i = 0; i < ps.size(); i++) {
            Cluster.Processor p = ps.get(i);
            Rail r = new Rail(p, x - MARGIN);
            put(r, MARGIN, i * HEIGHT);
            processors.put(p, r);
            SortedMap<Integer, Schedule.Assignment> as = schedule.getProcessors().get(p);
            if (as == null) continue;
            for (Map.Entry<Integer, Schedule.Assignment> j : as.entrySet()) {
                Node n = new Node(j.getValue());
                assignments.put(j.getValue(), n);
                r.put(n, j.getKey() * MULTIPLIER, 0);
            }
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        List<Cluster.Processor> ps = cluster.getLayout();
        for (int i = 0; i < ps.size(); i++) {
            try (Painter.State s = p.color((i % 2 + 1) * 0x444444)) {
                p.drawRect(0, i * HEIGHT, MARGIN, HEIGHT);
            }
            p.drawTextRight(ps.get(i).getName(), MARGIN - 2, i * HEIGHT + 2 + Painter.fontAscent);
        }
        super.onDraw(p, mouse);
    }

    static class Node extends WContainer {
        Schedule.Assignment assignment;
        Drag drag;
        Highlight highlight;
        boolean visible = true;
        boolean ghost = false;

        public Node(Schedule.Assignment a) {
            assignment = a;
            drag = new Drag();
            highlight = new Highlight();
            put(drag, 0, 0);
            put(highlight, 0, 0);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            if (!visible) return;
            try (Painter.State a = p.color(ghost ? 0.5f : 0f);
                 Painter.State b = p.color(0x888888)) {
                p.drawRect(0, 0, drag.x - 2, 2);
                p.drawRect(0, 2, 2, drag.y - 2);
                p.drawRect(drag.x - 2, 2, 2, drag.y - 2);
                p.drawRect(2, drag.y - 2, drag.x - 2, 2);
            }
        }

        class Drag extends WDrag {
            public Drag() {
                super(MULTIPLIER * (assignment.getEnd() - assignment.getStart()), HEIGHT);
            }

            @Override
            public void onReceived(Object o) {

            }

            @Override
            public boolean onEntering(Object o, Vector2i mouse) {
                return false;
            }

            @Override
            public void onSucceeded() {

            }

            @Override
            public @Nullable Object onStarting() {
                return assignment.getTask();
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {

            }
        }

        class Highlight extends WHighlight {
            @Override
            public Graph.Task onHighlight(Vector2i mouse) {
                return drag.onTest(mouse) ? assignment.getTask() : null;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
            }
        }
    }

    class Rail extends WContainer {
        Node ghost = null;
        Drag drag;
        Cluster.Processor processor;

        public Rail(Cluster.Processor p, int x) {
            processor = p;
            put(drag = new Drag(x), 0, 0);
        }

        class Drag extends WDrag {
            public Drag(int x) {
                super(x, HEIGHT);
            }

            @Override
            public boolean onMouse(Vector2i mouse, Window.Mouse button, boolean state) {
                if (ghost != null && button == Window.Mouse.MOVE) {
                    Log.client.info("move");
                    assign(mouse, cancel());
                    return true;
                } else return super.onMouse(mouse, button, state);
            }

            @Override
            public void onReceived(Object o) {
                super.onReceived(o);
                assignments.put(ghost.assignment, ghost);
                ghost.ghost = true;
                ghost = null;
            }

            @Override
            public boolean onEntering(Object o, Vector2i mouse) {
                if (o instanceof Graph.Task) {
                    Log.client.info("enter");
                    assign(mouse, (Graph.Task) o);
                    return true;
                } else return false;
            }

            @Override
            public void onLeaving() {
                Log.client.info("leave");
                cancel();
            }

            private Graph.Task cancel() {
                Graph.Task ret = ghost.assignment.getTask();
                schedule.cancel(ghost.assignment);
                remove(ghost);
                ghost = null;
                return ret;
            }

            private void assign(Vector2i mouse, Graph.Task t) {
                int time = Math.max(schedule.attempt(t, processor), mouse.x / MULTIPLIER);
                Schedule.Assignment a = schedule.assign(t, processor, time);
                ghost = new Node(a);
                put(ghost, time * MULTIPLIER, 0);
            }
        }
    }
}
