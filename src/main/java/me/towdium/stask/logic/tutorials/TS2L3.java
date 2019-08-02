package me.towdium.stask.logic.tutorials;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Event.EGame;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 28/07/2019
 */
public class TS2L3 extends Tutorial.Impl {
    static final String S1 = "In this level we are going to talk about another variant of HLFET " +
            "for heterogeneous clusters. The idea is simple: instead of focusing on execute " +
            "as early as possible, we will try to make tasks finish as early as possible.";
    static final String S2 = "To understand it better, we are always using heuristics in list " +
            "scheduling algorithms. Therefore, an easy way to make the algorithm suitable for " +
            "a certain cluster is to add features into the heuristic. By changing start time to " +
            "finish time, it actually reflects different performance of processors.";
    static final String S3 = "For task A and B, we follow the same procedure as previous levels " +
            "to schedule them to processor A and B.";
    static final String S4 = "For task C, since processor A has high speedup for type \u03b2, " +
            "it actually finishes earlier on processor A compared to processor B, although processor B" +
            "is available earlier. So we schedule task C to processor A";
    static final String S5 = "Then we can finish remaining schedule by repeating the process.";
    static final String S6 = "Now the schedule is finished, let's run it to see the result.\n\n" +
            "This tutorial is finished.";

    public TS2L3(Game game) {
        super(game);
    }

    @Override
    public void activate() {
        super.activate();
        BUS.gate(Event.class, this, i -> false);
        BUS.subscribe(Event.ETutorial.class, this, i -> {
            switch (i.id) {
                case "S3":
                    schedule("B", "A");
                    schedule("A", "B");
                    break;
                case "S4":
                    schedule("C", "A");
                    break;
                case "S5":
                    schedule("E", "A");
                    schedule("D", "B");
                    schedule("F", "B");
                    break;
                case "S6":
                    BUS.cancel(this);
                    game.start();
                    BUS.post(new EGame.Start());
                    break;
            }
        });

        widget.update(S1, true);
        widget.update(S2, false);
        widget.update(S3, false, "S3");
        widget.update(S4, false, "S4");
        widget.update(S5, false, "S5");
        widget.update(S6, false, "S6");
    }
}
