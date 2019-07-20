package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Resource;
import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Game;
import me.towdium.stask.utils.wrap.Trio;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 10/06/19
 */
@ParametersAreNonnullByDefault
public class WCluster extends WContainer {
    public static final int WIDTH = Node.WIDTH + 60;
    public static final int HEIGHT = 4 * (Node.HEIGHT + Node.SPACING) - Node.SPACING;
    Game game;
    Map<Cluster.Processor, Integer> processors = new HashMap<>();

    public WCluster(Game game) {
        this.game = game;
        List<Cluster.Processor> layout = game.getCluster().getLayout();
        for (int i = 0; i < layout.size(); i++) {
            Cluster.Processor p = layout.get(i);
            put(new Node(game.getProcessors().get(p)), 0, i * (Node.HEIGHT + Node.SPACING));
            processors.put(p, i);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        int s = Node.SPACING + Node.HEIGHT;
        try (Painter.State ignore = p.color(0x333333)) {
            for (int i = 0; i < 4; i++) p.drawRect(0, i * s, Node.WIDTH, Node.HEIGHT);
        }
        for (Cluster.Processor i : processors.keySet()) {
            Game.Status status = game.getProcessor(i);
            status.getComms().values().stream()
                    .filter(j -> j.b)
                    .forEach(j -> drawComm(p, j.c, i));
        }
        super.onDraw(p, mouse);
    }

    public void drawComm(Painter p, Cluster.Processor from, Cluster.Processor to) {
        int f = processors.get(from);
        int t = processors.get(to);
        int space = Node.HEIGHT + Node.SPACING;
        p.drawRect(Node.WIDTH, Node.HEIGHT / 2 - 1 + f * space, (f + 1) * 15 - 1, 2);
        p.drawRect(Node.WIDTH + (f + 1) * 15 - 1, Node.HEIGHT / 2 - 1, 2, (t - f) * space + 2);
        p.drawRect(Node.WIDTH, Node.HEIGHT / 2 - 1 + t * space, (f + 1) * 15 - 1, 2);
        try (Painter.SMatrix matrix = p.matrix()) {
            matrix.translate(Node.WIDTH, Node.HEIGHT / 2 - 1 + t * space);
            matrix.rotate((float) Math.PI / 4);
            p.drawRect(0, -1, 10, 2);
            matrix.rotate(-(float) Math.PI / 2);
            p.drawRect(0, -1, 10, 2);
        }
    }

    class Node implements Widget {
        public static final int WIDTH = 132;
        public static final int HEIGHT = 56;
        public static final int SPACING = 20;
        Game.Status status;

        public Node(Game.Status status) {
            this.status = status;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.State ignore = p.color(0x444444)) {
                p.drawRect(0, 0, WIDTH, HEIGHT);
            }
            p.drawTextRight(status.getProcessor().getName(), 78, Painter.fontAscent + 1);
            try (Painter.State ignore = p.color(0x44882200)) {
                p.drawRect(88, 0, 44, 28, 2);
                p.drawRect(88, 0, (int) (44 * status.getProgress()), 28);
            }
            try (Painter.State ignore = p.color(0x44226688)) {
                if (game.getCluster().getComm() == 0) {
                    p.drawRect(0, 28, 132, 28, 2);
                    try (Painter.SMatrix matrix = p.matrix()) {
                        matrix.translate(0, 28);
                        matrix.rotate((float) Math.atan(28 / 132f));
                        p.drawRect(1, 0, (int) Math.sqrt(132 * 132 + 28 * 28), 2);
                    }
                } else if (game.getCluster().getPolicy().multiple) {
                    Iterator<Trio<Float, Boolean, Cluster.Processor>> it = status.getComms().values().iterator();
                    for (int i = 0; i < 3; i++) {
                        p.drawRect(i * 44, 28, 44, 28, 2);
                        if (it.hasNext()) p.drawRect(i * 44, 28, (int) (44 * it.next().a), 28);
                    }
                } else {
                    Iterator<Trio<Float, Boolean, Cluster.Processor>> it = status.getComms().values().iterator();
                    p.drawRect(0, 28, 132, 28, 2);
                    if (it.hasNext()) p.drawRect(0, 28, (int) (132 * it.next().a), 28);
                }
            }
            p.drawResource(Resource.PROCESSOR, 0, 0);
        }
    }
}
