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
public class TS3L1 extends Tutorial.Impl {
    static final String S1 = "This is one level for demonstration. You can use any strategy to " +
            "build the schedule to make it run faster, not matter the order of execution. " +
            "When all the graphs are completed, the system will evaluate it according to the performance.";

    public TS3L1(Game game) {
        super(game);
    }

    @Override
    public void activate() {
        super.activate();
        BUS.subscribe(Event.ETutorial.class, this, i -> widget.clear());
        BUS.subscribe(Event.EGame.Start.class, this, i -> widget.clear());
        widget.update(S1, true);
        widget.update("", false, "");
    }
}
