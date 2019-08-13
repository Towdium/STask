package me.towdium.stask.logic.tutorials;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 22/07/19
 */
@SuppressWarnings("DuplicatedCode")  // I want to keep tutorial code straight forward
public class TS1L3 extends Tutorial.Impl {
    static final String S1 = "This level we are going to talk about another model: " +
            "single background communication. I know what you think: why the tutorial is so " +
            "long? Yes, that's the complexity of task scheduling. Please keep a little patient. " +
            "We still have several things to introduce.";
    static final String S2 = "You can see the difference from progress bar: one processor can " +
            "execute tasks and communicate at the same time, but with only one processor, and in one " +
            "direction. This makes the execution of schedules harder to estimate. Also, to have " +
            "better performance, you need to be more careful with communications.";
    static final String S3 = "Here's the schedule for demonstration. Let's run it.";
    static final String S4 = "As you can see, processor B and C both requires data from processor " +
            "A to execute the scheduled task, while processor A have only one channel. " +
            "In this case, the system will pick one communication to start first, and another blocked.\n\n" +
            "Now click on the start button to continue the execution.";
    static final String S5 = "Although we are working on the same task graph, you can see the result " +
            "changes a lot for different communication models. This is why scheduling algorithms can only " +
            "apply to certain models. Therefore, when learning these algorithms, it's important to " +
            "distinguish the applicable models.\n\n" +
            "This level is finished.";

    public TS1L3(Game g) {
        super(g);
    }

    @Override
    public void activate() {
        super.activate();
        schedule();
    }

    private void schedule() {
        BUS.gate(Event.class, this, i -> false);
        BUS.subscribe(Event.ETutorial.class, this, i -> {
            if (i.id.equals("S3")) {
                BUS.cancel(this);
                schedule("A", "A");
                schedule("B", "A");
                schedule("C", "B");
                schedule("D", "C");
                start();
            }
        });
        widget.update(S1, true);
        widget.update(S2, false);
        widget.update(S3, false, "S3");
    }

    private void start() {
        BUS.gate(Event.class, this, i -> i instanceof Event.EGame.Start);
        BUS.subscribe(Event.EGame.Tick.class, this, e -> {
            if (e.count == 120) {
                widget.update(S4, true);
                game.pause();
                BUS.post(new Event.EGame.Pause());
                BUS.cancel(this);
                resume();
            }
        });
    }

    private void resume() {
        BUS.gate(Event.class, this, e -> e instanceof Event.EGame.Start);
        BUS.subscribe(Event.EGame.Finish.class, this, e -> {
            widget.update(S5, true);
            BUS.cancel(this);
        });
    }
}
