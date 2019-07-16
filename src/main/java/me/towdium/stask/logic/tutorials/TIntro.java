package me.towdium.stask.logic.tutorials;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.widgets.WGraph;
import me.towdium.stask.client.widgets.WTask;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Tutorial;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 11/07/19
 */
@ParametersAreNonnullByDefault
public class TIntro extends Tutorial.Impl {
    public TIntro(Game g) {
        super(g);
        initialize(new Allocate1(),
                new AllocateN("b", "a"),
                new AllocateN("c", "b"),
                new AllocateN("d", "b"),
                new Start());
    }

    static class Allocate1 extends AllocateN {
        static final String S1 = "Welcome! This game aims to teach the " +
                "concept of task graph scheduling, a problem in parallel " +
                "systems.\n\n" +
                "When dealing with big tasks, a common solution to parallelize " +
                "it is to split it into small ones with dependencies (a task " +
                "graph), then distribute them to several processors.";
        static final String S2 = "This is an example task. The first row shows " +
                "cost and type (we will talk about it later), and the second row " +
                "shows the name of the task.";
        static final String S3 = "This is a task graph. When you hover " +
                "over any task, all related communications will be highlighted " +
                "with size of data.";
        static final String S4 = "Inside one task graph, if there is any connection between " +
                "two tasks, there is dependency. In this case, top level task (predecessor) have to " +
                "be finished before the start of low level task (successor).";
        static final String S5 = "Communications happen between tasks if two related tasks " +
                "are executed on different processors. In this example, " +
                "communications are assumed to take no time (an ideal assumption), " +
                "so we will talk about it later.\n\n";
        static final String S6 = "On the left hand side you have several processors. " +
                "You can assign tasks to processors by dragging tasks to the task queue. " +
                "Let's get started by assigning task a to processor a.";

        public Allocate1() {
            super("a", "a");
        }

        @Override
        public void activate(WTutorial w) {
            w.update((p, m) -> p.drawTextWrapped(S1, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), true);

            Game g = new Game("tutorial");
            Graph r = g.getGraphs().iterator().next();
            w.update((p, m) -> {
                p.drawTextWrapped(S2, 10, 140 + Painter.fontAscent, WTutorial.WIDTH - 20);
                WTask.drawTask(p, r.getTask("a"), 200, 70);
            }, false);

            WGraph a = new WGraph(200, g, r) {
                @Override
                public void onDraw(Painter p, Vector2i mouse) {
                    super.onDraw(p, mouse);
                    for (Graph.Task i : tasks.keySet()) {
                        for (Map.Entry<Graph.Task, Graph.Comm> j : i.getSuccessor().entrySet()) {
                            drawConnection(p, i, j.getKey(), true);
                        }
                    }
                }
            };
            w.update((p, m) -> {
                try (Painter.SMatrix matrix = p.matrix()) {
                    matrix.translate(200 - a.getWidth() / 2, 5);
                    a.onDraw(p, m);
                }
                p.drawTextWrapped(S3, 10, 200 + Painter.fontAscent, WTutorial.WIDTH - 20);
            }, false);

            w.update((p, m) -> p.drawTextWrapped(S4, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), false);
            w.update((p, m) -> p.drawTextWrapped(S5, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), false);
            w.update((p, m) -> p.drawTextWrapped(S6, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), false);
        }
    }

    static class AllocateN implements Tutorial.Impl.Stage {
        boolean p;
        String task, processor;

        public AllocateN(String task, String processor) {
            this.task = task;
            this.processor = processor;
            BUS.subscribe(Event.ETask.Schedule.class, this, e -> {
                if (e.task.getName().equals(task) && e.processor.getName().equals(processor)) p = true;
            });
        }

        @Override
        public boolean pass() {
            return p;
        }

        @Override
        public boolean test(Event e) {
            if (e instanceof Event.ETask.Schedule) {
                Event.ETask.Schedule s = (Event.ETask.Schedule) e;
                return s.task.getName().equals(task) && s.processor.getName().equals(processor);
            } else return e instanceof Event.ETask.Pick;
        }

        @Override
        public void activate(WTutorial w) {
            String s = "Then allocate task " + task + " to processor " + processor + ".";
            w.update((p, m) -> p.drawTextWrapped(s, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), true);
        }
    }

    static class Start implements Tutorial.Impl.Stage {
        static final String S1 = "The appearance of tasks change according to the state. " +
                "At default, they are grey. When assigned, they turn blue. When being executed, " +
                "they turn yellow. When finished, they turn green. Therefore, one task graph is " +
                "finished when all the tasks inside turn green";

        static final String S2 = "The state of processors will be rendered in real time, " +
                "and also recorded inside the history bar when tasks are executed.\n\n" +
                "Now things are set up. Let's hit the start button to execute the schedule.";

        boolean p = false;

        public Start() {
            BUS.subscribe(Event.EGame.Start.class, this, e -> p = true);
        }

        @Override
        public boolean pass() {
            return p;
        }

        @Override
        public boolean test(Event e) {
            return e instanceof Event.EGame.Start;
        }

        @Override
        public void activate(WTutorial w) {
            w.update((p, m) -> p.drawTextWrapped(S1, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), true);
            w.update((p, m) -> p.drawTextWrapped(S2, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), true);
        }
    }
}
