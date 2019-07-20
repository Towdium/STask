package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widget;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 19/07/19
 */
@ParametersAreNonnullByDefault
public class WStatus implements Widget {
    public static String text = null;

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        if (text != null) p.drawText(text, 0, Painter.fontAscent);
    }
}
