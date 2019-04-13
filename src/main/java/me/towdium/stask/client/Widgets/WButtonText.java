package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public class WButtonText extends WButton {
    String s;

    public WButtonText(int x, int y, String s) {
        super(x, y);
        this.s = s;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        p.drawTextCut(s, 0, (y - Painter.fontHeight) / 2, x);
    }
}