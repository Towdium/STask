package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
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
    Game game;
    Graph graph;
    Map<Graph.Task, WTask> tasks = new IdentityHashMap<>();
    int width;

    public WGraph(int y, Game g, Graph r) {
        game = g;
        graph = r;
        List<List<Task>> layout = r.getLayout();
        int max = layout.stream().mapToInt(List::size).max().orElse(0);
        width = max * (WTask.WIDTH + 24) + 24;
        int ys = layout.size();
        int yd = 36;
        int yo = (y - ys * (WTask.HEIGHT + yd) + yd) / 2;
        for (int i = 0; i < ys; i++) {
            List<Task> row = layout.get(i);
            int xs = row.size();
            int xd = 24;
            int xo = (width - xs * (WTask.WIDTH + xd) + xd) / 2;
            for (int j = 0; j < xs; j++) {
                Task t = row.get(j);
                if (t == null) continue;
                WTask w = new WTask(t, game);
                tasks.put(t, w);
                put(w, xo + j * (xd + WTask.WIDTH), yo + i * (yd + WTask.HEIGHT));
            }
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        List<Pair<Graph.Task, Graph.Task>> late = new ArrayList<>();
        for (Graph.Task i : tasks.keySet()) {
            for (Map.Entry<Task, Graph.Comm> j : i.getBefore().entrySet()) {
                if (WFocus.isFocused(i) || WFocus.isFocused(j.getKey()) || WFocus.isFocused(j.getValue()))
                    late.add(new Pair<>(i, j.getKey()));
                else drawConnection(p, i, j.getKey(), false);
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
            Vector2f center = start.add(end, new Vector2f()).mul(0.5f).add(-12, -12);
            try (Painter.SMatrix s = p.matrix()) {
                s.translate((int) center.x, (int) center.y);
                try (Painter.State ignore = p.color(0x888888)) {
                    p.drawRect(0, 0, 24, 24);
                }
                p.drawTextCenter(Integer.toString(a.getBefore().get(b).getSize()), 12, 1 + Painter.fontAscent);
            }
        }
    }

    public int getWidth() {
        return width;
    }
}
