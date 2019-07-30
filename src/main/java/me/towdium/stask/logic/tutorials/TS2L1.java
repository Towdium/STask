package me.towdium.stask.logic.tutorials;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 28/07/2019
 */
public class TS2L1 extends Tutorial.Impl {
    static final String S1 = "This section we are going to talk about one " +
            "scheduling algorithm: HLFET (Highest Level First with Estimated Times)." +
            "You can find tons of algorithms in this area, but we will use this " +
            "one as an example, and to discuss the variants.";
    static final String S2 = "HLFET is a typical list scheduling algorithm. The idea " +
            "of list scheduling algorithms is to sort the tasks using priority, then " +
            "execute high priority tasks as early as possible. The main difference " +
            "between list scheduling algorithms is how they compute the priority.";
    static final String S3 = "In HLFET, the priority is defined as the length of longest " +
            "path to exit. Let's take this graph for example:\n\n" +
            "P(F) = 1\nP(D) = 1 + 3 = 4\nP(E) = 1 + 1 = 2\nP(A) = 1 + 3 + 2 = 6\n" +
            "P(B) = 1 + 3 + 3 = 7\nP(C) = 1 + 1 + 3 = 5";
    static final String S4 = "According to the priorities, we sort the tasks as: B A C D E F. " +
            "When there are tasks with same priority, some algorithms can have rules to break " +
            "the tie, but we just do it randomly in HLFET.";
    static final String S5 = "Now we need to schedule the tasks to processors. The top of " +
            "the list is task B, so we schedule it to processor A (actually no difference in this " +
            "stage between processor A and B).";
    static final String S6 = "Next task is A. To make it execute as early as possible, we schedule " +
            "it to processor B";
    static final String S7 = "Next task is C. Since task A takes less time, we know processor B " +
            "will be available earlier than processor A. So we schedule task C to processor B.";
    static final String S8 = "Now things are getting more complex. So we can simulate it to see " +
            "how it works. In addition to simulation, you can always estimate it by drawing the " +
            "timeline on paper.\n\n" +
            "As you can see, processor A is available earlier.";
    static final String S9 = "So we schedule next task in the list, task D to processor A. " +
            "Then we repeat the process to schedule task E to processor B.";
    static final String S10 = "When estimating the execution, one thing to notice is tasks will " +
            "always be delayed after all its dependencies. When we say \"execute as early as possible\" " +
            "in list scheduling, please always remember it means the time of actual execution, " +
            "rather than when the processor is available, because dependencies can easily block a task.";
    static final String S11 = "In this case, both processor looks same to execute task F. We choose " +
            "to schedule task F to processor A.";
    static final String S12 = "Up to now the schedule is complete. We can simulate it again to " +
            "see the result.\n\n" +
            "This tutorial is finished.";


    public TS2L1(Game game) {
        super(game);
    }

    @Override
    public void activate() {
        super.activate();

        BUS.gate(Event.class, this, i -> false);
        BUS.subscribe(Event.ETutorial.class, this, i -> {
            switch (i.id) {
                case "S5":
                    schedule("B", "A");
                    break;
                case "S6":
                    schedule("A", "B");
                    break;
                case "S7":
                    schedule("C", "B");
                    break;
                case "S8":
                case "S10":
                    game.start();
                    BUS.post(new Event.EGame.Start());
                    break;
                case "S9":
                    game.reset();
                    BUS.post(new Event.EGame.Reset());
                    schedule("B", "A");
                    schedule("A", "B");
                    schedule("C", "B");
                    schedule("D", "A");
                    schedule("E", "B");
                    break;
                case "S11":
                    game.reset();
                    BUS.post(new Event.EGame.Reset());
                    schedule("B", "A");
                    schedule("A", "B");
                    schedule("C", "B");
                    schedule("D", "A");
                    schedule("E", "B");
                    schedule("F", "A");
                    break;
                case "S12":
                    BUS.cancel(this);
                    game.start();
                    BUS.post(new Event.EGame.Start());
                    break;
            }
        });

        widget.update(S1, true);
        widget.update(S2, false);
        widget.update(S3, false);
        widget.update(S4, false);
        widget.update(S5, false, "S5");
        widget.update(S6, false, "S6");
        widget.update(S7, false, "S7");
        widget.update(S8, false, "S8");
        widget.update(S9, false, "S9");
        widget.update(S10, false, "S10");
        widget.update(S11, false, "S11");
        widget.update(S12, false, "S12");
    }
}
