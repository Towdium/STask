package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
public class WGraph extends WHContainer {
    Map<Task, WTask> tasks = new IdentityHashMap<>();

    public WGraph(int x, int y, Graph graph) {
        List<List<Task>> layout = graph.getLayout();
        int ys = layout.size();
        int yd = 25;
        int yo = (y - ys * (WTask.HEIGHT + yd) + yd) / 2;
        for (int i = 0; i < ys; i++) {
            List<Task> row = layout.get(i);
            int xs = row.size();
            int xd = 15;
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
        for (Task i : tasks.keySet())
            for (Task j : i.getBefore().keySet())
                drawConnection(p, i, j);
        super.onDraw(p, mouse);
    }

    private void drawConnection(Painter p, Task a, Task b) {
        Vector2f start = new Vector2f(find(tasks.get(a))).add(WTask.WIDTH / 2f, WTask.HEIGHT).add(0, -1);
        Vector2f end = new Vector2f(find(tasks.get(b))).add(WTask.WIDTH / 2f, 0).add(0, 1);
        Vector2f diff = end.sub(start, new Vector2f());
        try (Painter.SMatrix s = p.matrix()) {
            s.translate(start.x, start.y);
            s.rotate((float) Math.atan2(diff.y, diff.x));
            p.drawRect(0, -1, (int) diff.length(), 2);
        }
    }

    static class WTask extends WArea implements WHighlight {
        public static final int WIDTH = 30;
        public static final int HEIGHT = 38;
        Task task;

        public WTask(Task task) {
            super(WIDTH, HEIGHT);
            this.task = task;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.State s = p.color(0x666666)) {
                p.drawRect(0, 0, WIDTH, HEIGHT);
            }
            try (Painter.State s = p.color(0x888888)) {
                p.drawRect(0, 0, WIDTH, 19);
            }
            p.drawTextRight(Integer.toString(task.getTime()), 26, Painter.fontAscent + 2);
            p.drawTextRight(task.getType(), 26, Painter.fontAscent + 19);
        }

        @Override
        public boolean onHighlight(Vector2i mouse) {
            //return inside(mouse) ? task : null;
            return false;
        }
    }
}
