package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public class WButtonText extends WButton {
    String s;

    public WButtonText(int x, int y, String s) {
        super(x, y);
        this.s = s;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        p.drawTextCut(s, 0, (y - Painter.fontHeight) / 2 + Painter.fontAscent, x);
    }
}
