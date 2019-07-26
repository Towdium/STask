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
public class TS1L4 extends Tutorial.Impl {
    static final String S1 = "This level we are going to talk about the last communication " +
            "model: blocking communication. In this model, one processor can either process " +
            "tasks or communicate, but not together, as indicated by the progress bar. " +
            "It might sound crude, but it's the typical model when using blocking " +
            "communication libraries (like Java IO).";
    static final String S2 = "Let's run it to see what will happen using the same schedule as before.";
    static final String S3 = "As you can see, processor stops processing to send data to processor B. " +
            "In this application, when there is conflict between processing and communication, " +
            "communication will always be executed first.\n\n" +
            "Now click on the start button to continue the execution.";
    static final String S4 = "In this model, don't be surprised parallel execution is not faster than " +
            "serial execution because the communication model is very strict. In real life, it's almost the same: " +
            "parallel algorithms sometimes have poor scalability due to poor communication performance.";
    static final String S5 = "Another thing to be noticed is communication is handled by the system " +
            "automatically. But in real life, execution of communication is more flexible and difference " +
            "can actually lead to different performance. We will talk about it later.\n\n" +
            "This tutorial is finished.";

    public TS1L4(Game g) {
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
            if (i.id.equals("S2")) {
                BUS.cancel(this);
                schedule("A", "A");
                schedule("B", "A");
                schedule("C", "B");
                schedule("D", "C");
                start();
            }
        });
        widget.update(S1, true);
        widget.update(S2, false, "S2");
    }

    private void start() {
        BUS.gate(Event.class, this, i -> i instanceof Event.EGame.Start);
        BUS.subscribe(Event.EGame.Tick.class, this, e -> {
            if (e.count == 120) {
                widget.update(S3, true);
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
            widget.update(S4, true);
            widget.update(S5, false);
            BUS.cancel(this);
        });
    }
}
