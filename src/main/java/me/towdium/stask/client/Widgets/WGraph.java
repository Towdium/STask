package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import org.joml.Vector2i;

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
        int yd = (y - ys * WTask.HEIGHT) / Math.max(ys - 1, 1);
        int yo = ys == 1 ? (y - WTask.HEIGHT) / 2 : 0;
        for (int i = 0; i < ys; i++) {
            List<Task> row = layout.get(i);
            int xs = row.size();
            int xd = (x - xs * WTask.WIDTH) / Math.max(xs - 1, 1);
            int xo = xs == 1 ? (x - WTask.WIDTH) / 2 : 0;
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
        for (Task i : tasks.keySet())
            for (Task j : i.getBefore().keySet())
                drawConnection(p, i, j);
        super.onDraw(p, mouse);
    }

    private void drawConnection(Painter p, Task a, Task b) {
        Vector2i start = new Vector2i(WTask.WIDTH / 2, WTask.HEIGHT).add(find(tasks.get(a)));
        Vector2i end = new Vector2i(WTask.WIDTH / 2, 0).add(find(tasks.get(b)));
        Vector2i diff = end.sub(start, new Vector2i());
        try (Painter.SMatrix s = p.matrix()) {
            s.translate(start.x, start.y);
            s.rotate((float) Math.atan2(diff.y, diff.x));
            p.drawRect(-1, -1, (int) diff.length(), 2);
        }
    }

    static class WTask extends WArea {
        public static final int WIDTH = 60;
        public static final int HEIGHT = 40;
        Task task;

        public WTask(Task task) {
            super(WIDTH, HEIGHT);
            this.task = task;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            p.drawRect(0, 0, WIDTH, HEIGHT);
        }
    }
}
