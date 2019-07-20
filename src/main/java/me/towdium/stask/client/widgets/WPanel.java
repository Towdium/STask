package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Colour;
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
public class WPanel extends WArea.Impl {
    public WPanel(int xs, int ys) {
        super(xs, ys);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(Colour.PANEL)) {
            p.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
        return true;
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        if (onTest(mouse)) WFocus.clear();
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left) {
        return onTest(mouse);
    }
}
