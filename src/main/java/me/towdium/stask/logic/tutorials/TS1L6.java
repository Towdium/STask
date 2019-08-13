package me.towdium.stask.logic.tutorials;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.widgets.WCluster;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 28/07/2019
 */
public class TS1L6 extends Tutorial.Impl {
    static final String S1 = "You might be confused why every task is labeled as type \"\u03b1\". " +
            "Now we have the second type. In real world, different operations can have different " +
            "speed. For example, GPU can process graphics much faster than CPU. In this game, " +
            "we use speedup to represent such behavior.";
    static final String S2 = "Here is one processor. The special speed icon means it has speedup for " +
            "certain types of tasks. When you hover over it, the details will be displayed.";
    static final String S3 = "When processing tasks in certain type, the actual speed is " +
            "base speed x speedup.\n\n" +
            "I have made one schedule for you to make use of high speedup of " +
            "processor B for type \u03b2. Let's run it.";
    static final String S4 = "In this level, processor A has base speed 2. " +
            "Therefore, when processing task A, it takes 2/2=1 second.\n\n" +
            "Processor B has base speed 1 and x8 speedup for type \u03b2. " +
            "Therefore, when processing task B, it takes 8/1/8=1 second.";
    static final String S5 = "When processors inside a cluster are different, it is called a heterogeneous " +
            "cluster, which is another tough topic for scheduling algorithms.\n\n" +
            "This tutorial is finished.";


    public TS1L6(Game game) {
        super(game);
    }

    @Override
    public void activate() {
        super.activate();
        intro();
    }

    private void intro() {
        BUS.gate(Event.class, this, i -> false);
        BUS.subscribe(Event.ETutorial.class, this, i -> {
            if (i.id.equals("S3")) {
                BUS.cancel(this);
                schedule("A", "A");
                schedule("B", "B");
                schedule("C", "A");
                schedule("D", "A");
                start();
            }
        });

        widget.update(S1, true);

        Game game = new Game("1-6");
        Cluster cluster = game.getCluster();
        Cluster.Processor processor = cluster.getProcessor("B");
        widget.update((p, m) -> {
            try (Painter.SMatrix matrix = p.matrix()) {
                matrix.translate(54, 40);
                WCluster.drawProcessor(p, game, processor);
                matrix.translate(144, 0);
                WCluster.drawSpeedup(p, processor);
            }
            p.drawTextWrapped(S2, 10, 140 + Painter.fontAscent, WTutorial.WIDTH - 20);
        }, false);

        widget.update(S3, false, "S3");
    }

    private void start() {
        BUS.gate(Event.class, this, i -> i instanceof Event.EGame.Start);
        BUS.subscribe(Event.EGame.Finish.class, this, e -> {
            BUS.cancel(this);
            widget.update(S4, true);
            widget.update(S5, false);
        });
    }
}
