package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Widget;
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

    protected boolean onTest(Vector2i mouse) {
        return mouse != null && mouse.x >= 0 && mouse.y >= 0 && mouse.x < x && mouse.y < y;
    }
}
