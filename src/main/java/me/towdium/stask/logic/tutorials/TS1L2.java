package me.towdium.stask.logic.tutorials;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 22/07/19
 */
public class TS1L2 extends Tutorial.Impl {
    static final String S1 = "In this level we will talk about another cluster model, where there " +
            "is no magic: communication will take time to execute. However, it's still a simple one.\n\n" +
            "Each processor is allowed to send to and receive from many other processors at the same time. " +
            "The only limit is it cannot have multiple channels to the same processor.";
    static final String S2 = "I have made a schedule for you to demonstrate it. Now hit the start button " +
            "to execute it.";
    static final String S3 = "Now communication starts, as indicated by the progress bars. The engine will " +
            "execute related communications automatically when attempting to execute any task.\n\n" +
            "This model accepts multiple communication at background. Therefore, processor " +
            "A is sending data to processor B and C at the same time, while it is still processing.";
    static final String S4 = "Let me emphasize it: there is no need for communication when predecessor (A) " +
            "and successor (B) are executed on the same processor (A). So task B can start immediately " +
            "when task A finishes. This is very important when optimizing communication intensive problems.\n\n" +
            "Now click on the start button to continue the execution.";
    static final String S5 = "You can always refer to the history bar to check the execution results. " +
            "When considering communications, there might be overlapping areas in the timeline. Clicking " +
            "on such area, they will be separated for you.\n\n" +
            "This tutorial is finished.";

    public TS1L2(Game g) {
        super(g);
    }

    @Override
    public void activate() {
        super.activate();
        schedule();
    }

    private void schedule() {
        widget.update(S1, true);
        widget.update(S2, false, "S2");
        BUS.gate(Event.class, this, e -> false);
        BUS.subscribe(Event.ETutorial.class, this, e -> {
            if (e.id.equals("S2")) {
                schedule("A", "A");
                schedule("B", "A");
                schedule("C", "B");
                schedule("D", "C");
                BUS.cancel(this);
                start();
            }
        });
    }

    private void start() {
        BUS.gate(Event.class, this, e -> e instanceof Event.EGame.Start);
        BUS.subscribe(Event.EGame.Start.class, this, e -> {
            BUS.cancel(this);
            describe();
        });
    }

    private void describe() {
        BUS.gate(Event.class, this, e -> false);
        BUS.subscribe(Event.EGame.Tick.class, this, e -> {
            if (e.count == 120) {
                widget.update(S3, true);
                widget.update(S4, false);
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
