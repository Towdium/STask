package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Graph;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 28/05/19
 */
@ParametersAreNonnullByDefault
public abstract class WHighlight implements Widget {
    static Graph.Task focus;
    static WHighlight owner;

    @Override
    public void onMove(Vector2i mouse) {
        Graph.Task t = onHighlight(mouse);
        if (t != null) {
            owner = this;
            focus = t;
        } else if (owner == this) {
            owner = null;
            focus = null;
        }
    }

    @Nullable
    public abstract Graph.Task onHighlight(@Nullable Vector2i mouse);
}
