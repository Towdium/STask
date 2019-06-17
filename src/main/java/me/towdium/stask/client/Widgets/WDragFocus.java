package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Graph;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 10/06/19
 *
 * Container + Drag + Focus
 */
@ParametersAreNonnullByDefault
public abstract class WDragFocus extends WContainer {
    Drag drag;
    Focus focus;

    public WDragFocus(int x, int y) {
        drag = new Drag(x, y);
        focus = new Focus(x, y);
        put(drag, 0, 0);
        put(focus, 0, 0);
    }

    protected Graph.Work onFocus() {
        return null;
    }

    protected void onReceived(Object o) {
    }

    protected boolean onTest(Object o, Vector2i mouse) {
        return false;
    }

    protected void onEnter(Object o, Vector2i mouse) {
    }

    protected void onLeaving() {
    }

    @Nullable
    protected Object onStarting() {
        return null;
    }

    protected void onSucceeded() {
    }

    protected void onRejected() {
    }

    @Override
    public WContainer clear() {
        super.clear();
        put(drag, 0, 0);
        put(focus, 0, 0);
        return this;
    }

    class Drag extends WDrag {
        public Drag(int x, int y) {
            super(x, y);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
        }

        @Override
        public void onReceived(Object o) {
            WDragFocus.this.onReceived(o);
        }

        @Override
        public boolean onTest(Object o, Vector2i mouse) {
            return WDragFocus.this.onTest(o, mouse);
        }

        @Override
        public void onEnter(Object o, Vector2i mouse) {
            WDragFocus.this.onEnter(o, mouse);
        }

        @Override
        public void onLeaving() {
            WDragFocus.this.onLeaving();
        }

        @Nullable
        @Override
        public Object onStarting() {
            return WDragFocus.this.onStarting();
        }

        @Override
        public void onSucceeded() {
            WDragFocus.this.onSucceeded();
        }

        @Override
        public void onRejected() {
            WDragFocus.this.onRejected();
        }
    }

    class Focus extends WFocus {
        public Focus(int x, int y) {
            super(x, y);
        }

        @Nullable
        @Override
        public Graph.Work onFocus() {
            return WDragFocus.this.onFocus();
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
        }

        @Override
        protected boolean onTest(@Nullable Vector2i mouse) {
            // grab focus at drag n drop
            return super.onTest(mouse) || WDrag.sender == drag;
        }
    }
}
