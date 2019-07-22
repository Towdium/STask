package me.towdium.stask.logic.tutorials;

import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

/**
 * Author: Towdium
 * Date: 22/07/19
 */
public class TS1L2 extends Tutorial.Impl {
    static final String S1 = "In this level we will talk about another cluster model, where there " +
            "is no magic: communication will take time to execute. However, it's still a simple one.\n\n" +
            "Each processor is allowed to send to and receive from many other processors at the same time. " +
            "The only limit is it cannot have multiple channels to the same processor.";

    public TS1L2(Game g) {
        super(g);
    }

    @Override
    public void activate() {
        super.activate();
        start();
    }

    private void start() {
        widget.update(S1, true);
    }
}
