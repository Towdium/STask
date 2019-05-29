package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.wrap.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
public class WGraph extends WContainer {
    Map<Task, WTask> tasks = new IdentityHashMap<>();

    public WGraph(int x, int y, Graph graph) {
        List<List<Task>> layout = graph.getLayout();
        int ys = layout.size();
        int yd = 28;
        int yo = (y - ys * (WTask.HEIGHT + yd) + yd) / 2;
        for (int i = 0; i < ys; i++) {
            List<Task> row = layout.get(i);
            int xs = row.size();
            int xd = 18;
            int xo = (x - xs * (WTask.WIDTH + xd) + xd) / 2;
            for (int j = 0; j < xs; j++) {
                Task t = row.get(j);
                if (t == null) continue;
                WTask w = new WTask(t);
                tasks.put(t, w);
                put(w, xo + j * (xd + WTask.WIDTH), yo + i * (yd + WTask.HEIGHT));
            }
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        List<Pair<Task, Task>> late = new ArrayList<>();

        for (Task i : tasks.keySet()) {
            for (Task j : i.getBefore().keySet()) {
                Task focus = WHighlight.focus == null ? null : WHighlight.focus.getTask();
                if (focus == i || focus == j)
                    late.add(new Pair<>(i, j));
                else drawConnection(p, i, j, false);
            }
        }
        for (Pair<Task, Task> i : late) drawConnection(p, i.a, i.b, true);
        super.onDraw(p, mouse);
    }

    private void drawConnection(Painter p, Task a, Task b, boolean highlight) {
        Vector2f start = new Vector2f(find(tasks.get(a))).add(WTask.WIDTH / 2f, WTask.HEIGHT).add(0, -1);
        Vector2f end = new Vector2f(find(tasks.get(b))).add(WTask.WIDTH / 2f, 0).add(0, 1);
        Vector2f diff = end.sub(start, new Vector2f());
        try (Painter.SMatrix s = p.matrix()) {
            s.translate(start.x, start.y);
            s.rotate((float) Math.atan2(diff.y, diff.x));
            try (Painter.State c = p.color(highlight ? 0xAAAAAA : 0x444444)) {
                p.drawRect(0, -1, (int) diff.length(), 2);
            }
        }
        if (highlight) {
            Vector2f center = start.add(end, new Vector2f()).mul(0.5f).add(-9, -9);
            try (Painter.SMatrix s = p.matrix()) {
                s.translate((int) center.x, (int) center.y);
                try (Painter.State c = p.color(0x888888)) {
                    p.drawRect(0, 0, 18, 18);
                }
                p.drawTextCenter(a.getBefore().get(b).toString(), 9, 1 + Painter.fontAscent);
            }
        }
    }

    static class WTask extends WContainer {
        public static final int WIDTH = 30;
        public static final int HEIGHT = 38;
        Task task;
        Drag drag = new Drag();
        Highlight highlight = new Highlight();

        public WTask(Task task) {
            this.task = task;
            put(drag, 0, 0);
            put(highlight, 0, 0);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            try (Painter.State s = p.color(0x666666)) {
                p.drawRect(0, 0, WIDTH, HEIGHT);
            }
            try (Painter.State s = p.color(0x888888)) {
                p.drawRect(0, 0, WIDTH, 19);
            }
            p.drawTextRight(Integer.toString(task.getTime()), 26, Painter.fontAscent + 2);
            p.drawTextRight(task.getType(), 26, Painter.fontAscent + 19);
        }

        class Highlight extends WHighlight {
            @Override
            public Task onHighlight(Vector2i mouse) {
                return drag.onTest(mouse) ? task : null;
            }

            @Override
            public Task getTask() {
                return task;
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
            public void onReceived(Object o) {

            }

            @Override
            public boolean onReceiving(Object o) {
                return false;
            }

            @Override
            public void onSent() {

            }

            @Override
            public @Nullable Object onSending() {
                return null;
            }
        }
    }
}
