package me.towdium.stask.client.Widgets;

import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 10/03/19
 */
@ParametersAreNonnullByDefault
public abstract class WDrag extends WArea {
    public static WDrag sender, receiver;
    static Object parcel;

    public WDrag(int x, int y) {
        super(x, y);
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left, boolean state) {
        if (left) {
            if (state) {
                if (onTest(mouse)) {
                    Object o = onStarting();
                    if (o != null) {
                        sender = this;
                        parcel = o;
                    }
                }
            } else if (sender == this) {
                if (receiver != null && parcel != null) {
                    onSucceeded();
                    receiver.onReceived(parcel);
                    receiver = null;
                } else onRejected();
                sender = null;
                parcel = null;
            }
        }
        return false;
    }

    @Override
    public void onMove(Vector2i mouse) {
        update(mouse);
    }

    private void update(@Nullable Vector2i mouse) {
        if (receiver == this) {
            if (!onTest(mouse)) {
                onLeaving();
                receiver = null;
            }
        } else if (sender != null && parcel != null && mouse != null
                && onTest(mouse) && onTest(parcel, mouse)) {
            if (receiver != null) receiver.onLeaving();
            onEnter(parcel, mouse);
            receiver = this;
        }
    }

    // receiver side
    public void onReceived(Object o) {
    }


    // receiver side
    public boolean onTest(Object o, Vector2i mouse) {
        return false;
    }

    // receiver side
    public void onEnter(Object o, Vector2i mouse) {
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
