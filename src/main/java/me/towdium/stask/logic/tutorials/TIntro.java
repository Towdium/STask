package me.towdium.stask.logic.tutorials;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.WContainer;
import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 11/07/19
 */
@ParametersAreNonnullByDefault
public class TIntro extends Tutorial {
    WContainer content = new WContainer();
    Event.Filter filter = new Event.Filter(this);
    Game game;

    public TIntro(Game g) {
        game = g;
        content.put((p, mouse) -> p.drawText("Hello!", 10, 10 + Painter.fontAscent), 0, 0);
        filter.update(i -> false);
    }

    @Override
    public Widget widget() {
        return frame(content);
    }
}
