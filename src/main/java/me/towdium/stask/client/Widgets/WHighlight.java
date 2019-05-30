package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Widget;
import me.towdium.stask.client.Window;
import me.towdium.stask.logic.Graph;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 28/05/19
 */
public abstract class WHighlight implements Widget {
    static Graph.Task focus;
    static WHighlight owner;

    @Override
    public boolean onMouse(Vector2i mouse, Window.Mouse button, boolean state) {
        if (button == Window.Mouse.MOVE) {
            Graph.Task t = onHighlight(mouse);
            if (t != null) {
                owner = this;
                focus = t;
            } else if (owner == this) {
                owner = null;
                focus = null;
            }
        }
        return false;
    }

    public abstract Graph.Task onHighlight(Vector2i mouse);
}
