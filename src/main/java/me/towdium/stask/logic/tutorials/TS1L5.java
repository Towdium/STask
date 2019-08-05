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
public class TS1L5 extends Tutorial.Impl {
    static final String S1 = "The system handles communication for you automatically " +
            "and it produces almost no delay. However, it is very stupid at deciding " +
            "the order. This is not a problem in multiple communication models, but it " +
            "can be a disaster in limited communication models.";
    static final String S2 = "In multiple communication models, it will fetch data whenever " +
            "possible because the resource is unlimited. However in limited communication " +
            "modes, it will strictly follow the order. When the first communication is not " +
            "finished, the second one will always be blocked to avoid competition.";
    static final String S3 = "I've made a schedule for you. Now run it to see what happens.";
    static final String S4 = "To finish this graph faster, processor C can receive data from " +
            "processor B once task B is finished. But to follow the order, it is delayed " +
            "until communication from processor A is finished.";
    static final String S5 = "I have reset the schedule for you. Now let's change the order " +
            "to make it run faster. First click on task C in the schedule, then drag the " +
            "buttons in the pop up menu to put task B at the top of the queue.";
    static final String S6 = "Well done. Now let's run it again.";
    static final String S7 = "Of course there are better schedules for this task graph." +
            "This is just to demonstrate how it works. When playing with single communication " +
            "models, it's always good to check the order of input.";
    static final String S8 = "Another thing to be noticed is global communication speed is " +
            "changed to 2 in this level (see upper left corner). Therefore, every communication " +
            "takes 2/2=1 seconds to finish.";
    static final String S9 = "In this game, communication is simulated in a simplified model." +
            "In real life, it is more flexible, and  brings more difficulties " +
            "to scheduling algorithms. For example, adding extra delays to communication can " +
            "affect the performance in some cases, but we will not talk about it.\n\n" +
            "This tutorial is finished.";

    public TS1L5(Game game) {
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
                schedule("C", "C");
                start();
            }
        });

        widget.update(S1, true);
        widget.update(S2, false);
        widget.update(S3, false, "S3");
    }

    private void start() {
        BUS.gate(Event.class, this, i -> i instanceof EGame.Start);
        BUS.subscribe(EGame.Finish.class, this, e -> {
            BUS.cancel(this);
            reset();
        });
    }

    private void reset() {
        BUS.gate(Event.class, this, i -> false);
        BUS.subscribe(Event.ETutorial.class, this, e -> {
            if (e.id.equals("S5")) {
                game.reset();
                BUS.post(new EGame.Reset());
                schedule("A", "A");
                schedule("B", "B");
                schedule("C", "C");
                BUS.cancel(this);
                order();
            }
        });
        widget.update(S4, true);
        widget.update(S5, false, "S5");
    }

    private void order() {
        BUS.gate(Event.class, this, i -> {
            if (!(i instanceof Event.ETask.Comm)) return false;
            Event.ETask.Comm c = (Event.ETask.Comm) i;
            return c.order.get(0).getSrc().getName().equals("B")
                    && c.order.get(1).getSrc().getName().equals("A");
        });
        BUS.subscribe(Event.ETask.Comm.class, this, e -> {
            BUS.cancel(this);
            restart();
        });
    }

    private void restart() {
        BUS.gate(Event.class, this, i -> i instanceof EGame.Start);
        BUS.subscribe(EGame.Finish.class, this, e -> {
            BUS.cancel(this);
            widget.update(S7, true);
            widget.update(S8, false);
            widget.update(S9, false);
        });
        widget.update(S6, true);
    }
}
