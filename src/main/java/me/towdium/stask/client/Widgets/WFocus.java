package me.towdium.stask.client.Widgets;

import me.towdium.stask.logic.Graph;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 28/05/19
 */
@ParametersAreNonnullByDefault
public abstract class WFocus extends WArea {
    static Graph.Work focus;
    static WFocus owner;

    public WFocus(int x, int y) {
        super(x, y);
    }

    @Override
    public void onMove(Vector2i mouse) {
        Graph.Work t = onTest(mouse) ? onFocus() : null;
        if (t != null) {
            owner = this;
            focus = t;
        } else if (owner == this) {
            owner = null;
            focus = null;
        }
    }

    @Override
    public void onRemove() {
        if (owner != this) return;
        owner = null;
        focus = null;
    }

    @Nullable
    public abstract Graph.Work onFocus();
}
