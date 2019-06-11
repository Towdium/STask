package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Game;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 10/06/19
 */
@ParametersAreNonnullByDefault
public class WGame extends WContainer {
    Game game;

    public WGame(Game game) {
        this.game = game;
        int start = 0;
        for (Map.Entry<Cluster.Processor, Game.Status> i : game.getProcessors().entrySet()) {
            put(new Node(i.getValue()), 0, start);
            start += 80;
        }
    }

    static class Node implements Widget {
        public static final int WIDTH = 60;
        public static final int HEIGHT = 60;
        Game.Status status;

        public Node(Game.Status status) {
            this.status = status;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.State ignore = p.color(0x444444)) {
                p.drawRect(0, 0, WIDTH, HEIGHT);
            }
            p.drawText(status.getProcessor().getName(), 4, 4 + Painter.fontAscent);
            try (Painter.State ignore = p.color(0x222222)) {
                p.drawRect(0, 20, WIDTH, 20);
            }
            try (Painter.State ignore = p.color(0xAA2222)) {
                p.drawRect(0, 20, (int) (WIDTH * status.getProgress()), 20);
            }
            Iterator<Float> it = status.getComms().values().iterator();
            for (int i = 0; i < 2; i++) {
                if (it.hasNext()) {
                    Float e = it.next();
                    try (Painter.State ignore = p.color(0x2222AA)) {
                        p.drawRect(0, 40 + i * 10, (int) (WIDTH * e), 10);
                    }
                }
            }
        }
    }
}
