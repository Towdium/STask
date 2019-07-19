package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 18/07/19
 */
@ParametersAreNonnullByDefault
public class WRectangle implements Widget {
    int x, y, color;

    public WRectangle(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(color)) {
            p.drawRect(0, 0, x, y);
        }
    }
}
