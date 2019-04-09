package me.towdium.stask.gui.Widgets;

import me.towdium.stask.gui.Painter;
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
    public void onDraw(Vector2i mouse) {
        super.onDraw(mouse);
        Painter.drawTextCut(s, 0, (y - Painter.fontHeight) / 2, x);
    }
}
