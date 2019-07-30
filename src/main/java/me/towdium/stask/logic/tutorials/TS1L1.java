package me.towdium.stask.logic.tutorials;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.widgets.WGraph;
import me.towdium.stask.client.widgets.WTask;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.*;
import me.towdium.stask.logic.Event.EGame;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Map;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 22/07/19
 */
@ParametersAreNonnullByDefault
public class TS1L1 extends Tutorial.Impl {
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
            "two tasks, there is dependency. When dependency occurs, top level task " +
            "(predecessor) and related communication have to be finished before " +
            "the start of low level task (successor).\n\n" +
            "In this case, execution of B will be postponed until A is finished.";
    static final String S5 = "In the top left corner, it shows the game timer, counting the " +
            "total time cost of one schedule. The second row shows the communication speed. " +
            "In this case, the speed is infinity, meaning communication will take no time.";
    static final String S6 = "On the left hand side it shows several processors. " +
            "You can assign tasks to processors by dragging tasks to the task queue " +
            "At the bottom of the interface.\n\n" +
            "For each processor, it shows the name, speed and progress bars of execution " +
            "and communication.";
    static final String S7 = "Let's get started by assigning task a to processor a.";
    static final String S8 = "The appearance of tasks change according to the state. " +
            "At default, they are grey. When assigned, they turn blue. When being executed, " +
            "they turn yellow. When finished, they turn green.";
    static final String S9 = "I have scheduled remaining tasks for you. " +
            "Now the schedule is ready to execute. Let's hit the start button to execute the schedule.";
    static final String S10 = "Now the execution finishes. You can check the timeline " +
            "at the bottom of the interface. When you hover over any record, the " +
            "corresponding task will be highlighted.\n\n" +
            "In this level, both processors have speed of 1 and all tasks have duration " +
            "of 1, so the cost for each task is 1/1=1 second.";
    static final String S11 = "You can reset the game using the control panel in the lower right corner. " +
            "In future games, using the control panel, you can also adjust speed of simulation, " +
            "or pause it. \n\n" +
            "This tutorial is finished.";

    public TS1L1(Game game) {
        super(game);
    }

    @Override
    public void activate() {
        super.activate();
        allocate();
    }

    @SuppressWarnings("Duplicates")
    private void allocate() {
        BUS.subscribe(Event.ETask.Schedule.class, this, e -> {
            if (e.task.getName().equals("A") && e.processor.getName().equals("A")) {
                BUS.cancel(this);
                start();
            }
        });
        BUS.gate(Event.class, this, e -> {
            if (e instanceof Event.ETask.Schedule) {
                Event.ETask.Schedule s = (Event.ETask.Schedule) e;
                return s.task.getName().equals("A") && s.processor.getName().equals("A");
            } else if (e instanceof Event.ETask.Pick) {
                Event.ETask.Pick p = (Event.ETask.Pick) e;
                return p.source instanceof WTask;
            } else return false;
        });

        widget.update(S1, true);

        Graph r = new Graph(".1-1");
        Game g = new Game(new Cluster("1-1"), Collections.singletonList(r));
        widget.update((p, m) -> {
            p.drawTextWrapped(S2, 10, 140 + Painter.fontAscent, WTutorial.WIDTH - 20);
            WTask.drawTask(p, r.getTask("A"), 200, 70);
        }, false);

        WGraph a = new WGraph(200, g, r) {
            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                super.onDraw(p, mouse);
                try (Painter.SMatrix matrix = p.matrix()) {
                    matrix.translate(0, offset);
                    for (Graph.Task i : tasks.keySet()) {
                        for (Map.Entry<Graph.Task, Graph.Comm> j : i.getSuccessor().entrySet()) {
                            drawConnection(p, i, j.getKey(), true);
                        }
                    }
                }
            }
        };
        widget.update((p, m) -> {
            try (Painter.SMatrix matrix = p.matrix()) {
                matrix.translate(200 - a.getWidth() / 2, 5);
                a.onDraw(p, m);
            }
            p.drawTextWrapped(S3, 10, 200 + Painter.fontAscent, WTutorial.WIDTH - 20);
        }, false);

        widget.update(S4, false);
        widget.update(S5, false);
        widget.update(S6, false);
        widget.update(S7, false);
    }

    private void start() {
        BUS.gate(Event.class, this, e -> false);
        BUS.subscribe(Event.ETutorial.class, this, e -> {
            if (e.id.equals("S2S2")) {
                Schedule schedule = game.getSchedule();
                Graph graph = game.getGraphs().get(0);
                Cluster cluster = game.getCluster();
                schedule.allocate(graph.getTask("B"), cluster.getProcessor("A"));
                schedule.allocate(graph.getTask("C"), cluster.getProcessor("B"));
                schedule.allocate(graph.getTask("D"), cluster.getProcessor("B"));
                BUS.cancel(this);
                finish();
            }
        });

        widget.update(S8, true);
        widget.update(S9, false, "S2S2");
    }

    private void finish() {
        BUS.gate(Event.class, this, v -> v instanceof EGame.Start);
        BUS.subscribe(EGame.Finish.class, this, v -> {
            BUS.cancel(this);
            widget.update(S10, true);
            widget.update(S11, false);
        });
    }
}
