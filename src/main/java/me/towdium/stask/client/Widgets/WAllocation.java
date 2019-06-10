package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Allocation;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Author: Towdium
 * Date: 10/06/19
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("Duplicates")
public class WAllocation extends WContainer {
    static final int WIDTH = 20;
    static final int HEIGHT = 20;
    static final int MARGIN = 30;
    Allocation allocation;
    Cluster cluster;

    public WAllocation(int x, int y, Allocation a, Cluster c) {
        allocation = a;
        cluster = c;
        List<Processor> ps = cluster.getLayout();
        for (int i = 0; i < ps.size(); i++) {
            put(new Rail(ps.get(i), x), 0, HEIGHT * i);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        List<Processor> ps = cluster.getLayout();
        for (int i = 0; i < ps.size(); i++) {
            try (Painter.State ignore = p.color((i % 2 + 1) * 0x444444)) {
                p.drawRect(0, i * HEIGHT, MARGIN, HEIGHT);
            }
            p.drawTextRight(ps.get(i).getName(), MARGIN - 2, i * HEIGHT + 2 + Painter.fontAscent);
        }
        super.onDraw(p, mouse);
    }

    class Rail extends WDragFocus {
        Processor processor;
        int index = -1;

        public Rail(Processor p, int x) {
            super(x, HEIGHT);
            processor = p;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            if (WDrag.receiver != drag) return;
            p.drawRect(MARGIN + getIndex(mouse) * WIDTH - 1, 0, 2, HEIGHT);
        }

        @Override
        protected Graph.Work onFocus() {
            return null;
        }

        @Override
        public void onReceived(Object o) {
            if (o instanceof Task) {
                allocation.allocate((Task) o, processor, index);
                sync();
            }
        }

        @Override
        public boolean onTest(Object o, Vector2i mouse) {
            if (o instanceof Task) {
                index = getIndex(mouse);
                return true;
            } else return false;
        }

        @Override
        public void onEnter(Object o, Vector2i mouse) {
        }

        @Override
        public void onLeaving() {
        }

        private int getIndex(Vector2i mouse) {
            int total = allocation.getTasks(processor).size();
            int pos = (mouse.x - MARGIN + WIDTH / 2) / WIDTH;
            return Math.min(total, pos);
        }

        private void sync() {
            clear();
            List<Task> ts = allocation.getTasks(processor);
            for (int i = 0; i < ts.size(); i++)
                put(new Node(ts.get(i), i), MARGIN + i * WIDTH, 0);
        }

        class Node extends WDragFocus {
            Task task;
            int idx;
            boolean visible = true;

            public Node(Task t, int i) {
                super(WIDTH, HEIGHT);
                task = t;
                idx = i;
            }

            @Override
            protected Graph.Work onFocus() {
                return task;
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                super.onDraw(p, mouse);
                if (visible) {
                    try (Painter.State ignore = p.color(0xAAAAAA)) {
                        p.drawRect(0, 0, WIDTH - 2, 2);
                        p.drawRect(0, 2, 2, HEIGHT - 2);
                        p.drawRect(WIDTH - 2, 0, 2, HEIGHT - 2);
                        p.drawRect(2, HEIGHT - 2, WIDTH - 2, 2);
                    }
                }

                if (WDrag.sender == drag && WDrag.receiver == null)
                    WGraph.drawTask(p, mouse.x - WGraph.Node.WIDTH / 2,
                            mouse.y - WGraph.Node.HEIGHT / 2, task, false);
            }

            @Nullable
            @Override
            public Object onStarting() {
                allocation.remove(processor, idx);
                sync();
                Rail.this.put(this, 0, 0);
                visible = false;
                return task;
            }

            @Override
            public void onRejected() {
                allocation.allocate(task, processor, idx);
                sync();
            }

            @Override
            public void onSucceeded() {
                remove(this);
            }
        }
    }
}
