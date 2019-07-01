package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 10/03/19
 */
@ParametersAreNonnullByDefault
public abstract class WDrag implements WArea, WOwner {
    private static WDrag sender, receiver;
    private static Object parcel;
    WOwner owner = this;

    public WDrag() {
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
    }

    @Override
    public boolean onDrag(@Nullable Vector2i mouse, boolean left) {
        if (onTest(mouse)) {
            Object o = onStarting();
            if (o != null) {
                sender = this;
                parcel = o;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDrop(boolean left) {
        if (sender == this) {
            if (receiver != null && parcel != null) {
                onSucceeded();
                receiver.onReceived(parcel);
                receiver = null;
            } else onRejected();
            sender = null;
            parcel = null;
            return true;
        } else return false;
    }

    @Override
    public void onMove(Vector2i mouse) {
        if (receiver == this) {
            if (!onTest(mouse)) {
                onLeaving();
                receiver = null;
            }
        } else if (sender != null && parcel != null
                && onTest(mouse) && onAttempt(parcel, mouse)) {
            if (receiver != null) receiver.onLeaving();
            onEnter(parcel, mouse);
            receiver = this;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSending() {
        return sender != null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isReceiving() {
        return receiver != null;
    }

    @Nullable
    public static WOwner getSender() {
        return sender == null ? null : sender.owner;
    }

    @Nullable
    public static WOwner getReceiver() {
        return receiver == null ? null : receiver.owner;
    }

    public static boolean isSending(WOwner w) {
        return sender != null && sender.owner == w;
    }

    public static boolean isReceiving(WOwner w) {
        return receiver != null && receiver.owner == w;
    }

    @Override
    public void onTransfer(WOwner to) {
        owner = to;
    }

    // receiver side
    public void onReceived(Object o) {
    }

    // receiver side
    public boolean onAttempt(Object o, Vector2i mouse) {
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

    public static class Impl extends WDrag {
        int x, y;

        public Impl(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean onTest(@Nullable Vector2i mouse) {
            return Quad.inside(mouse, x, y);
        }
    }
}
