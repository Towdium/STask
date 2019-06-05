package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.logic.Schedule;
import me.towdium.stask.utils.wrap.Pair;
import org.joml.Vector2f;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
@ParametersAreNonnullByDefault
public class WGraph extends WContainer {
    Schedule schedule;
    Map<Graph.Task, Node> tasks = new IdentityHashMap<>();

    public WGraph(int x, int y, Graph g, Schedule s) {
        schedule = s;
        List<List<Task>> layout = g.getLayout();
        int ys = layout.size();
        int yd = 28;
        int yo = (y - ys * (Node.HEIGHT + yd) + yd) / 2;
        for (int i = 0; i < ys; i++) {
            List<Task> row = layout.get(i);
            int xs = row.size();
            int xd = 18;
            int xo = (x - xs * (Node.WIDTH + xd) + xd) / 2;
            for (int j = 0; j < xs; j++) {
                Task t = row.get(j);
                if (t == null) continue;
                Node w = new Node(t);
                tasks.put(t, w);
                put(w, xo + j * (xd + Node.WIDTH), yo + i * (yd + Node.HEIGHT));
            }
        }
    }

    public static void drawTask(Painter p, int x, int y, Graph.Task t) {
        try (Painter.SMatrix m = p.matrix()) {
            m.translate(x, y);
            try (Painter.State ignore = p.color(0x666666)) {
                p.drawRect(0, 0, Node.WIDTH, Node.HEIGHT);
            }
            try (Painter.State ignore = p.color(0x888888)) {
                p.drawRect(0, 0, Node.WIDTH, 19);
            }
            p.drawTextRight(Integer.toString(t.getTime()), 26, Painter.fontAscent + 2);
            p.drawTextRight(t.getType(), 26, Painter.fontAscent + 19);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        List<Pair<Graph.Task, Graph.Task>> late = new ArrayList<>();
        Task focus = WHighlight.focus instanceof Task ? (Task) WHighlight.focus : null;
        for (Graph.Task i : tasks.keySet()) {
            for (Task j : i.getBefore().keySet()) {
                if (focus == i || focus == j)
                    late.add(new Pair<>(i, j));
                else drawConnection(p, i, j, false);
            }
        }
        for (Pair<Task, Task> i : late) drawConnection(p, i.a, i.b, true);
        super.onDraw(p, mouse);
    }

    private void drawConnection(Painter p, Task a, Task b, boolean highlight) {
        Vector2f start = new Vector2f(find(tasks.get(a))).add(Node.WIDTH / 2f, Node.HEIGHT).add(0, -1);
        Vector2f end = new Vector2f(find(tasks.get(b))).add(Node.WIDTH / 2f, 0).add(0, 1);
        Vector2f diff = end.sub(start, new Vector2f());
        try (Painter.SMatrix s = p.matrix()) {
            s.translate(start.x, start.y);
            s.rotate((float) Math.atan2(diff.y, diff.x));
            try (Painter.State ignore = p.color(highlight ? 0xAAAAAA : 0x444444)) {
                p.drawRect(0, -1, (int) diff.length(), 2);
            }
        }
        if (highlight) {
            Vector2f center = start.add(end, new Vector2f()).mul(0.5f).add(-9, -9);
            try (Painter.SMatrix s = p.matrix()) {
                s.translate((int) center.x, (int) center.y);
                try (Painter.State ignore = p.color(0x888888)) {
                    p.drawRect(0, 0, 18, 18);
                }
                p.drawTextCenter(Integer.toString(a.getBefore().get(b).getSize()), 9, 1 + Painter.fontAscent);
            }
        }
    }

    class Node extends WContainer {
        public static final int WIDTH = 30;
        public static final int HEIGHT = 38;
        Task task;
        Drag drag = new Drag();
        Highlight highlight = new Highlight();

        public Node(Task task) {
            this.task = task;
            put(drag, 0, 0);
            put(highlight, 0, 0);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            drawTask(p, 0, 0, task);
        }

        class Highlight extends WHighlight {
            @Override
            public Task onHighlight(@Nullable Vector2i mouse) {
                return drag.onTest(mouse) ? task : null;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
            }
        }

        class Drag extends WDrag {
            public Drag() {
                super(WIDTH, HEIGHT);
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                if (sender == this && receiver == null) {
                    try (Painter.State ignore = p.priority(true)) {
                        drawTask(p, mouse.x - WIDTH / 2, mouse.y - HEIGHT / 2, task);
                    }
                }
            }

            @Override
            public void onReceived(Object o) {
            }

            @Override
            public void onSucceeded() {
            }

            @Override
            public @Nullable Object onStarting() {
                for (Task t : task.getAfter().keySet())
                    if (schedule.getAssignment(t) == null) return null;
                if (schedule.getAssignment(task) != null) return null;
                return task;
            }
        }
    }
}
