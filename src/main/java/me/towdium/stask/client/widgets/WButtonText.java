package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public class WButtonText extends WButton {
    String s;

    public WButtonText(int x, int y, String s) {
        this(x, y, s, null);
    }

    public WButtonText(int x, int y, String s, @Nullable Supplier<Boolean> gate) {
        super(x, y, gate);
        this.s = s;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        p.drawTextCut(s, 0, (y - Painter.fontHeight) / 2 + Painter.fontAscent, x);
    }
}
