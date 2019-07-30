package me.towdium.stask.logic.tutorials;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 28/07/2019
 */
public class TS2L2 extends Tutorial.Impl {
    static final String S1 = "In this level we are going to talk about a variant of HLFET " +
            "to consider communication. We will give tasks same priority and still try to " +
            "execute high priority tasks as early as possible. However, when estimating " +
            "start time of tasks, we consider the time delayed by communication.";
    static final String S2 = "Let's first take an example. Now we make the same schedule as " +
            "last level for task A, B, C and D.";
    static final String S3 = "Then we simulate it. You can find task D is terribly delayed " +
            "due to the big data size between task A and D. When task A is executed on processor " +
            "B while task D is scheduled to processor A, the system will first transfer " +
            "the data between processors, then execute the task.";
    static final String S4 = "That is why we use the start time delayed by communication. " +
            "In this case, although processor A is available earlier, scheduling task D to " +
            "processor B will make it start earlier by avoiding big communications.";
    static final String S5 = "Then we repeat the process for remaining tasks. We schedule " +
            "both task E and F to processor A. Now the schedule is finished.";
    static final String S6 = "Now we can simulate it to check the result. As you can see " +
            "from the timeline, this schedule can be further improved, but don't worry. " +
            "The idea is scheduling algorithms do not always provide the optimal schedule, " +
            "but helps to avoid worst ones.\n\n" +
            "This tutorial is finished.";


    public TS2L2(Game game) {
        super(game);
    }

    @Override
    public void activate() {
        super.activate();
        BUS.gate(Event.class, this, i -> false);
        BUS.subscribe(Event.ETutorial.class, this, i -> {
            switch (i.id) {
                case "S2":
                    schedule("B", "A");
                    schedule("A", "B");
                    schedule("C", "B");
                    schedule("D", "A");
                    break;
                case "S3":
                    game.start();
                    BUS.post(new Event.EGame.Start());
                    break;
                case "S4":
                    game.reset();
                    BUS.post(new Event.EGame.Reset());
                    schedule("B", "A");
                    schedule("A", "B");
                    schedule("C", "B");
                    schedule("D", "B");
                    break;
                case "S5":
                    schedule("E", "A");
                    schedule("F", "B");
                    break;
                case "S6":
                    BUS.cancel(this);
                    game.start();
                    BUS.post(new Event.EGame.Start());
            }
        });

        widget.update(S1, true);
        widget.update(S2, false, "S2");
        widget.update(S3, false, "S3");
        widget.update(S4, false, "S4");
        widget.update(S5, false, "S5");
        widget.update(S6, false, "S6");
    }
}
