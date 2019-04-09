package me.towdium.stask.gui.Widgets;

import me.towdium.stask.gui.Widget;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public abstract class WArea implements Widget {
    int x, y;

    public WArea(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected boolean inside(Vector2i mouse) {
        return mouse.x >= 0 && mouse.y >= 0 && mouse.x < x && mouse.y < y;
    }
}
