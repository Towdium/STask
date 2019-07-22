package me.towdium.stask.logic.tutorials;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 22/07/19
 */
public class TS1L2 extends Tutorial.Impl {
    public TS1L2(Game g) {
        super(g);
        initialize(new Allocate());
    }

    class Allocate extends SAllocate {
        static final String S1 = "In this level we will talk about another cluster model, where there " +
                "is no magic: communication will take time to execute. However, it's still a simple one.\n\n" +
                "Each processor is allowed to send to and receive from many other processors at the same time. " +
                "The only limit is it cannot have multiple channels to the same processor.";

        public Allocate() {
            super("A", "A", TS1L2.this);
        }

        @Override
        public void activate() {
            widget.update((p, m) -> p.drawTextWrapped(S1, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20), true);
        }

        @Override
        public void deactivate() {
            BUS.cancel(this);
        }
    }
}
