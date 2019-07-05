package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Game;
import me.towdium.stask.utils.wrap.Trio;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;

/**
 * Author: Towdium
 * Date: 10/06/19
 */
@ParametersAreNonnullByDefault
public class WCluster extends WContainer {
    Game game;

    public WCluster(Game game) {
        this.game = game;
        int start = 0;
        for (Cluster.Processor i : game.getCluster().getLayout()) {
            put(new Node(game.getProcessors().get(i)), 0, start);
            start += 100;
        }
    }

    static class Node implements Widget {
        public static final int WIDTH = 84;
        public static final int HEIGHT = 84;
        Game.Status status;

        public Node(Game.Status status) {
            this.status = status;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.State ignore = p.color(0x444444)) {
                p.drawRect(0, 0, WIDTH, HEIGHT);
            }
            p.drawText(status.getProcessor().getName(), 4, Painter.fontAscent + 1);
            try (Painter.State ignore = p.color(0x222222)) {
                p.drawRect(0, HEIGHT / 3, WIDTH, HEIGHT / 3);
            }
            try (Painter.State ignore = p.color(0xAA2222)) {
                p.drawRect(0, HEIGHT / 3, (int) (WIDTH * status.getProgress()), HEIGHT / 3);
            }
            Iterator<Trio<Float, Boolean, Cluster.Processor>> it = status.getComms().values().iterator();
            for (int i = 0; i < 2; i++) {
                if (it.hasNext()) {
                    Trio<Float, Boolean, Cluster.Processor> e = it.next();
                    try (Painter.State ignore = p.color(0x2222AA)) {
                        p.drawRect(0, HEIGHT / 3 * 2 + i * HEIGHT / 6, (int) (WIDTH * e.a), HEIGHT / 3);
                    }
                }
            }
        }
    }
}