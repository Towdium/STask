package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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
    Game game;
    Map<Processor, Rail> processors = new IdentityHashMap<>();

    public WAllocation(int x, int y, Game g) {
        game = g;
        List<Processor> ps = game.getCluster().getLayout();
        for (int i = 0; i < ps.size(); i++) {
            Processor p = ps.get(i);
            Rail r = new Rail(p, x, i % 2 + 1);
            put(r, 0, HEIGHT * i);
            processors.put(p, r);
        }
    }

    class Rail extends WDragFocus {
        Processor processor;
        int index = -1;
        int multiplier;

        public Rail(Processor p, int x, int m) {
            super(x, HEIGHT);
            processor = p;
            multiplier = m;
        }

        @Override
        public void onRefresh() {
            super.onRefresh();
            clear();
            List<Task> ts = game.getAllocation().getTasks(processor);
            Task t = game.getProcessor(processor).getWorking();
            if (t != null) put(new Node(t, -1), MARGIN, 0);
            for (int i = 0; i < ts.size(); i++)
                put(new Node(ts.get(i), i), MARGIN + (t == null ? i : i + 1) * WIDTH, 0);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);

            try (Painter.State ignore = p.color(multiplier * 0x444444)) {
                p.drawRect(0, 0, MARGIN, HEIGHT);
            }
            p.drawTextRight(processor.getName(), MARGIN - 2, 2 + Painter.fontAscent);

            if (WDrag.receiver != drag) return;
            p.drawRect(MARGIN + getIndex(mouse) * WIDTH - 1, 0, 2, HEIGHT);
        }

        @Override
        public void onMove(Vector2i mouse) {
            super.onMove(mouse);
            index = getIndex(mouse);
        }

        @Override
        public void onReceived(Object o) {
            if (o instanceof Task) {
                game.getAllocation().allocate((Task) o, processor, index);
                onRefresh();
            }
        }

        @Override
        public boolean onTest(Object o, Vector2i mouse) {
            return o instanceof Task;
        }

        @Override
        public void onEnter(Object o, Vector2i mouse) {
        }

        @Override
        public void onLeaving() {
        }

        private int getIndex(Vector2i mouse) {
            int total = game.getAllocation().getTasks(processor).size();
            int pos = (mouse.x - MARGIN + WIDTH / 2) / WIDTH;
            return Math.min(total, pos);
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

                    float progress;
                    Game.Status s = game.getProcessor(processor);
                    if (task == s.getWorking()) progress = s.getProgress();
                    else if (game.getAllocation().allocated(task)) progress = 0;
                    else progress = 1;

                    try (Painter.State ignore = p.color(0x888888)) {
                        p.drawRect(0, 0, (int) (WIDTH * progress), HEIGHT);
                    }

                    int color = WFocus.focus == task ? 0xCCCCCC : 0x888888;
                    try (Painter.State ignore = p.color(color)) {
                        p.drawRect(0, 0, WIDTH, HEIGHT, 2);
                    }
                }

                if (WDrag.sender == drag && WDrag.receiver == null)
                    WTask.drawTask(p, task, mouse.x - WTask.WIDTH / 2,
                            mouse.y - WTask.HEIGHT / 2);
            }

            @Nullable
            @Override
            public Object onStarting() {
                game.getAllocation().remove(processor, idx);
                Rail.this.onRefresh();
                Rail.this.put(this, 0, 0);
                visible = false;
                return task;
            }

            @Override
            public void onRejected() {
                game.getAllocation().allocate(task, processor, idx);
                Rail.this.onRefresh();
            }

            @Override
            public void onSucceeded() {
                remove(this);
            }
        }
    }
}
