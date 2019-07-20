package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Resource;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 18/07/19
 */
@ParametersAreNonnullByDefault
public class WButtonIcon extends WButton {
    Resource res;

    public WButtonIcon(int x, int y, Resource r) {
        this(x, y, r, null);
    }

    public WButtonIcon(int x, int y, Resource r, @Nullable String text) {
        super(x, y, text);
        res = r;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        p.drawResource(res, (int) (x - res.xs * res.mul) / 2, (int) (y - res.ys * res.mul) / 2);
    }
}
