package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Widget;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public abstract class WArea implements Widget {
    int x, y;

    public WArea(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected boolean onTest(@Nullable Vector2i mouse) {
        return mouse != null && mouse.x >= 0 && mouse.y >= 0 && mouse.x < x && mouse.y < y;
    }
}
