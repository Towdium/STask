package me.towdium.stask.logic.tutorials;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 28/07/2019
 */
@SuppressWarnings("DuplicatedCode")
public class TS3L2 extends Tutorial.Impl {
    static final String S1 = "This level is the demonstration for dynamic levels. In this level, " +
            "task graphs will not be given at the beginning. Instead, after the start of game, " +
            "several task graphs will be given at certain time. Also, the timeline will keep " +
            "going when no task is being executed.";

    public TS3L2(Game game) {
        super(game);
    }

    @Override
    public void activate() {
        super.activate();
        BUS.subscribe(Event.ETutorial.class, this, i -> {
            widget.clear();
            game.start();
            BUS.post(new Event.EGame.Start());
        });
        BUS.subscribe(Event.EGame.Start.class, this, i -> widget.clear());
        widget.update(S1, true);
        widget.update("", false, "");
    }
}
