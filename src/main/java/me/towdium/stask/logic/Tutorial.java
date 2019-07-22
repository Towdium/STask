package me.towdium.stask.logic;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.WTask;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.tutorials.TS1L1;
import me.towdium.stask.logic.tutorials.TS1L2;
import me.towdium.stask.utils.Toggleable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public interface Tutorial extends Toggleable {
    static Tutorial get(String id, Game g) {
        return Registry.loaders.get(id).apply(g);
    }

    Widget widget();

    class Registry {
        static HashMap<String, Function<Game, Tutorial>> loaders = new HashMap<>();

        static {
            loaders.put("1-1", TS1L1::new);
            loaders.put("1-2", TS1L2::new);
        }
    }

    class Impl implements Tutorial {
        protected Game game;
        List<Stage> stages;
        protected WTutorial widget = new WTutorial();
        int index = -1;

        public Impl(Game g) {
            game = g;
        }

        @Override
        public void activate() {
            update(0);
        }

        public void deactivate() {
            BUS.cancel(this);
            if (index != -1) stages.get(index).deactivate();
        }

        public void initialize(Stage... ss) {
            stages = Arrays.asList(ss);
        }

        private void update(int i) {
            if (index != -1) stages.get(index).deactivate();
            stages.get(i).activate();
            index = i;
        }

        @Override
        public Widget widget() {
            return widget;
        }

        protected void pass() {
            int next = index + 1;
            if (next < stages.size()) update(next);
            else {
                if (index != -1) stages.get(index).deactivate();
                index = -1;
                widget.clear();
            }
        }

        public interface Stage extends Toggleable {
        }

        public static class SAllocate implements Tutorial.Impl.Stage {
            String task, processor;
            Impl impl;

            public SAllocate(String task, String processor, Impl impl) {
                this.task = task;
                this.processor = processor;
                this.impl = impl;
            }

            @Override
            public void activate() {
                BUS.subscribe(Event.ETask.Schedule.class, this, e -> {
                    if (e.task.getName().equals(task) && e.processor.getName().equals(processor)) impl.pass();
                });
                BUS.gate(Event.class, this, e -> {
                    if (e instanceof Event.ETask.Schedule) {
                        Event.ETask.Schedule s = (Event.ETask.Schedule) e;
                        return s.task.getName().equals(task) && s.processor.getName().equals(processor);
                    } else if (e instanceof Event.ETask.Pick) {
                        Event.ETask.Pick p = (Event.ETask.Pick) e;
                        return p.source instanceof WTask;
                    } else return false;
                });
                add();
            }

            public void deactivate() {
                BUS.cancel(this);
            }

            protected void add() {
                String s = "Then allocate task " + task + " to processor " + processor + ".";
                impl.widget.update((p, m) -> p.drawTextWrapped(s, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), true);
            }
        }
    }
}
