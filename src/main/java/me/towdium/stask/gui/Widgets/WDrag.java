package me.towdium.stask.gui.Widgets;

import me.towdium.stask.gui.IWidget;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 10/03/19
 */
public abstract class WDrag implements IWidget {
    protected static WDrag sender, receiver;

    @Override
    public void onDraw(Vector2i mouse) {
        boolean inside = inside(mouse);
        if (sender == this) {
            if (canSend() == null) sender = receiver = null;
        } else if (receiver == this) {
            if (!inside) receiver = null;
        } else {
            if (inside && sender != null && canReceive(sender.canSend())) receiver = this;
        }
    }

    @Override
    public boolean onMouse(Vector2i mouse, int button, boolean state) {
        if (state) {
            if (inside(mouse) && canSend() != null) sender = this;
        } else if (sender == this) {
            Object o = sender.canSend();
            if (receiver != null) {
                if (o != null) {
                    receiver.onReceived();
                    onSent();
                }
                receiver = null;
            }
            sender = null;
        }
        return false;
    }

    public void onReceived() {

    }

    public abstract boolean canReceive(Object o);

    public void onSent() {

    }

    @Nullable
    public abstract Object canSend();

    protected abstract boolean inside(Vector2i mouse);
}
