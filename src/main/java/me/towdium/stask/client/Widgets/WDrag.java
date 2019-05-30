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
    static Object parcel;

    public WDrag(int x, int y) {
        super(x, y);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        update(mouse);
    }

    @Override
    public boolean onMouse(Vector2i mouse, Mouse button, boolean state) {
        if (button == Mouse.LEFT) {
            if (state) {
                if (onTest(mouse)) {
                    Object o = onStarting();
                    if (o != null) {
                        sender = this;
                        parcel = o;
                    }
                }
            } else if (sender == this) {
                if (receiver != null) {
                    onSucceeded();
                    receiver.onReceived(parcel);
                    receiver = null;
                } else onRejected();
                sender = null;
                parcel = null;
            }
        } else if (button == Mouse.MOVE) update(mouse);
        return false;
    }

    private void update(Vector2i mouse) {
        if (receiver == this) {
            if (!onTest(mouse)) {
                onLeaving();
                receiver = null;
            }
        } else if (sender != null && onTest(mouse) && onEntering(parcel, mouse)) {
            if (receiver != null) receiver.onLeaving();
            receiver = this;
        }
    }

    // receiver side
    public void onReceived(Object o) {
    }


    // receiver side
    public boolean onEntering(Object o, Vector2i mouse) {
        return false;
    }

    // receiver side
    public void onLeaving() {

    }

    // sender side
    @Nullable
    public Object onStarting() {
        return null;
    }

    // sender side
    public void onSucceeded() {
    }

    // sender side
    public void onRejected() {
    }
}
