package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 18/07/19
 */
@ParametersAreNonnullByDefault
public class WButtonIcon extends WButton {
    Painter.Resource res;

    public WButtonIcon(int x, int y, Painter.Resource r) {
        super(x, y);
        res = r;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        p.drawResource(Painter.Resource.START, (int) (x - res.xs * res.mul) / 2, (int) (y - res.ys * res.mul) / 2);
    }
}
