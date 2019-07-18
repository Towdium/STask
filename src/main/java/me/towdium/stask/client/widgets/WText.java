package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 17/07/19
 */
@ParametersAreNonnullByDefault
public class WText implements Widget {
    public int color;
    public String str;

    public WText(int color, String str) {
        this.color = color;
        this.str = str;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(color)) {
            p.drawText(str, 0, 0);
        }
    }
}
