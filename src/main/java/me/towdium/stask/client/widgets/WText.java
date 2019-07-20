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
    public int width;

    public WText(int color, String str) {
        this(color, str, -1);
    }

    public WText(int color, String str, int w) {
        this.color = color;
        this.str = str;
        this.width = w;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(color)) {
            if (width == -1) p.drawText(str, 0, Painter.fontAscent);
            else p.drawTextWrapped(str, 0, Painter.fontAscent, width);
        }
    }
}
