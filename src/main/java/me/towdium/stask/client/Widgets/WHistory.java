package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 17/06/19
 */
@ParametersAreNonnullByDefault
public class WHistory extends WContainer {
    static final int HEIGHT = 30;
    static final int MARGIN = 48;
    Game game;
    Map<Cluster.Processor, Rail> processors = new HashMap<>();

    public WHistory(int x, int y, Game g) {
        game = g;
        List<Cluster.Processor> ps = game.getCluster().getLayout();
        for (int i = 0; i < ps.size(); i++) {
            Cluster.Processor p = ps.get(i);
            Rail r = new Rail(p, x, i % 2 + 1);
            put(r, 0, HEIGHT * i);
            processors.put(p, r);
        }
    }

    class Rail extends WContainer {
        Cluster.Processor processor;
        int multiplier;

        public Rail(Cluster.Processor p, int x, int m) {
            processor = p;
            multiplier = m;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);

            try (Painter.State ignore = p.color(multiplier * 0x444444)) {
                p.drawRect(0, 0, MARGIN, HEIGHT);
            }
            p.drawTextRight(processor.getName(), MARGIN - 4, 2 + Painter.fontAscent);
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            super.onRefresh(mouse);
            clear();
            game.getHistory().getRecord(processor).forEach((w, p) ->
                    put(new Node(w, p.y - p.x), p.x + MARGIN, 0));
        }

        class Node extends WFocus {
            Graph.Work work;
            int width;

            public Node(Graph.Work w, int i) {
                super(i, HEIGHT);
                work = w;
                width = i;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                int color = work instanceof Graph.Task ? 0x888888CC : 0x88CC8888;
                try (Painter.State ignore = p.color(color)) {
                    p.drawRect(0, 0, width, HEIGHT);
                }
                if (WFocus.focus == work) {
                    try (Painter.State ignore = p.color(0xCCFFFFFF)) {
                        p.drawRect(0, 0, width, HEIGHT);
                    }
                }
            }

            @Nullable
            @Override
            public Graph.Work onFocus() {
                return work;
            }
        }
    }
}
