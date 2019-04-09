package me.towdium.stask.gui.Widgets;

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
    public void onDraw(Vector2i mouse) {
        if (sender == this) {
            if (canSend() == null) sender = receiver = null;
        } else if (receiver == this) {
            if (!inside(mouse)) receiver = null;
        } else {
            if (sender != null && inside(mouse) && canReceive(sender.canSend())) receiver = this;
        }
    }

    @Override
    public boolean onMouse(Vector2i mouse, int button, boolean state) {
        if (state) {
            if (inside(mouse) && canSend() != null)
                sender = this;
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

    public abstract void onReceived();

    public abstract boolean canReceive(Object o);

    public abstract void onSent();

    @Nullable
    public abstract Object canSend();

    protected boolean isSending() {
        return sender == this;
    }

    protected boolean isReceiving() {
        return receiver == this;
    }
}
