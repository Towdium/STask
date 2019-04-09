package me.towdium.stask.gui.Widgets;

import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.States;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public abstract class WButton extends WArea {
    protected ListenerAction<? super WButton> listener;

    public WButton(int x, int y) {
        super(x, y);
    }

    public WButton setListener(ListenerAction<? super WButton> r) {
        listener = r;
        return this;
    }

    @Override
    public void onDraw(Vector2i mouse) {
        try (States.State s = States.color(inside(mouse) ? 0xCCCCCC : 0xAAAAAA)) {
            Painter.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onMouse(Vector2i mouse, int button, boolean state) {
        if (inside(mouse) && button == 0 && listener != null) {
            listener.invoke(this);
            return true;
        } else return false;
    }
}
