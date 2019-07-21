package me.towdium.stask.client.widgets;

import me.towdium.stask.client.*;
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
        try (Painter.State ignore = p.color(Colour.DISABLED)) {
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
        p.drawRect(Node.WIDTH + (f + 1) * 15 - 1, Node.HEIGHT / 2 - 1 + Math.min(f, t) * space, 2, Math.abs(t - f) * space + 2);
        p.drawRect(Node.WIDTH, Node.HEIGHT / 2 - 1 + t * space, (f + 1) * 15 - 1, 2);
        try (Painter.SMatrix matrix = p.matrix()) {
            matrix.translate(Node.WIDTH, Node.HEIGHT / 2 - 1 + t * space);
            matrix.rotate((float) Math.PI / 4);
            p.drawRect(0, -1, 10, 2);
            matrix.rotate(-(float) Math.PI / 2);
            p.drawRect(0, -1, 10, 2);
        }
    }

    class Node extends WArea.Impl {
        public static final int WIDTH = 133;
        public static final int HEIGHT = 56;
        public static final int SPACING = 20;
        Game.Status status;

        public Node(Game.Status status) {
            super(WIDTH, HEIGHT);
            this.status = status;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            Iterator<Trio<Float, Boolean, Cluster.Processor>> it = status.getComms().values().iterator();
            try (Painter.State ignore = p.color(Colour.ACTIVE2)) {
                p.drawRect(0, 0, 50, 28);
            }
            try (Painter.State ignore = p.color(Colour.ACTIVE3)) {
                p.drawRect(50, 0, 83, 28);
            }
            try (Painter.State ignore = p.color(Colour.ACTIVE1)) {
                p.drawRect(0, 28, 133, 28);
            }
            p.drawTextRight(status.getProcessor().getName(), 44, Painter.fontAscent + 1);
            p.drawTextRight(Float.toString(status.getProcessor().getSpeed()), 127, Painter.fontAscent + 1);
            if (game.getCluster().getComm() == 0) {
                try (Painter.State ignore = p.color(0xDD4444)) {
                    p.drawRect(0, 28, 133, 28, 2);
                    p.drawRect(0, 28, (int) (133 * status.getProgress()), 28);
                }
            } else if (game.getCluster().getPolicy().background) {
                try (Painter.State ignore = p.color(0xDD4444)) {
                    p.drawRect(0, 28, 49, 28, 2);
                    p.drawRect(0, 28, (int) (50 * status.getProgress()), 28);
                }
                try (Painter.State ignore = p.color(0x4499DD)) {
                    if (game.getCluster().getPolicy().multiple) {
                        for (int i = 0; i < 6; i++) {
                            p.drawRect(i * 14 + 51, 28, 12, 28, 2);
                            if (it.hasNext()) p.drawRect(i * 14 + 51, 55, 12, -(int) (28 * it.next().a));
                        }
                    } else {
                        p.drawRect(51, 28, 82, 28, 2);
                        if (it.hasNext()) p.drawRect(51, 28, (int) (82 * it.next().a), 28);
                    }
                }
            } else {
                if (status.getWorking() == null) {
                    if (it.hasNext()) {
                        try (Painter.State ignore = p.color(0x4499DD)) {
                            p.drawRect(0, 28, (int) (133 * it.next().a), 28);
                        }
                    }
                } else {
                    try (Painter.State ignore = p.color(0xDD4444)) {
                        p.drawRect(0, 28, (int) (133 * status.getProgress()), 28);
                    }
                }
                try (Painter.State ignore = p.color(0xDD4444)) {
                    p.drawRect(0, 28, 2, 14);
                    p.drawRect(2, 28, 131, 2);
                    p.drawRect(131, 30, 2, 12);
                }
                try (Painter.State ignore = p.color(0x4499DD)) {
                    p.drawRect(0, 42, 2, 14);
                    p.drawRect(2, 54, 131, 2);
                    p.drawRect(131, 42, 2, 12);
                }
            }
            p.drawResource(Resource.PROCESSOR, 0, 0);
            p.drawResource(Resource.SPEED, 50, 0);
            Map<String, Float> su = status.getProcessor().getSpeedup();
            Vector2i global = Widget.page().mouse();
            Vector2i pos = global.sub(mouse, new Vector2i()).add(WIDTH + 10, 0);
            if (!su.isEmpty() && onTest(mouse) && Widget.page().overlay() == null) {
                Widget.page().overlay(new Page.Once((a, m) -> {
                    int i = 0;
                    try (Painter.SMatrix matrix = p.matrix()) {
                        matrix.translate(pos.x, pos.y);
                        for (Map.Entry<String, Float> e : su.entrySet()) {
                            int y = i * 28;
                            int c1 = i % 2 == 0 ? Colour.ACTIVE1 : Colour.ACTIVE2;
                            int c2 = i % 2 == 0 ? Colour.ACTIVE2 : Colour.ACTIVE3;
                            try (Painter.State ignore = a.color(c1)) {
                                p.drawRect(0, y, 50, 28);
                            }
                            try (Painter.State ignore = a.color(c2)) {
                                p.drawRect(50, y, 96, 28);
                            }
                            p.drawResource(Resource.CLASS, 1, y);
                            p.drawTextRight(e.getKey(), 46, y + 1 + Painter.fontAscent);
                            p.drawResource(Resource.SPEED, 51, y);
                            p.drawTextRight("x" + e.getValue(), 142, y + 1 + Painter.fontAscent);
                            i++;
                        }
                    }
                }));
            }
        }
    }
}
