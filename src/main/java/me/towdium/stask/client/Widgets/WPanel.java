package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Author: Towdium
 * Date: 18/06/19
 */
@ParametersAreNonnullByDefault
public class WPanel extends WArea {
    public WPanel(int xs, int ys) {
        super(xs, ys);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(0x444444)) {
            p.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
        return true;
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left, boolean state) {
        return onTest(mouse);
    }
}
