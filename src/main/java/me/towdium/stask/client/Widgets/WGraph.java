package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Allocation;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.wrap.Pair;
import org.joml.Vector2f;
import org.joml.Vector2i;

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
    Allocation allocation;
    Game game;
    Map<Graph.Task, WTask> tasks = new IdentityHashMap<>();

    public WGraph(int x, int y, Graph g, Allocation a, Game m) {
        game = m;
        allocation = a;
        List<List<Task>> layout = g.getLayout();
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
                WTask w = new WTask(t, allocation, game);
                tasks.put(t, w);
                put(w, xo + j * (xd + WTask.WIDTH), yo + i * (yd + WTask.HEIGHT));
            }
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        List<Pair<Graph.Task, Graph.Task>> late = new ArrayList<>();
        Graph.Work focus = WFocus.focus;
        for (Graph.Task i : tasks.keySet()) {
            for (Map.Entry<Task, Graph.Comm> j : i.getBefore().entrySet()) {
                if (focus == i || focus == j.getKey())
                    late.add(new Pair<>(i, j.getKey()));
                else drawConnection(p, i, j.getKey(), focus == j.getValue());
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
}
