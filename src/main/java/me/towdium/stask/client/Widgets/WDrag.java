package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Window.Mouse;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 10/03/19
 */
public abstract class WDrag extends WArea {
    public static WDrag sender, receiver;

    public WDrag(int x, int y) {
        super(x, y);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        if (sender == this) {
            if (onSending() == null) sender = receiver = null;
        } else if (receiver == this) {
            if (!onTest(mouse)) receiver = null;
        } else {
            if (sender != null && onTest(mouse) && onReceiving(sender.onSending())) receiver = this;
        }
    }

    @Override
    public boolean onMouse(Vector2i mouse, Mouse button, boolean state) {
        if (button != Mouse.LEFT) return false;
        if (state) {
            if (onTest(mouse) && onSending() != null)
                sender = this;
        } else if (sender == this) {
            Object o = sender.onSending();
            if (receiver != null) {
                if (o != null) {
                    receiver.onReceived(o);
                    onSent();
                }
                receiver = null;
            }
            sender = null;
        }
        return false;
    }

    public abstract void onReceived(Object o);

    public abstract boolean onReceiving(Object o);

    public abstract void onSent();

    @Nullable
    public abstract Object onSending();
}
