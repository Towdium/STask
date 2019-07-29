package me.towdium.stask.client;

import me.towdium.stask.client.widgets.WContainer;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Author: Towdium
 * Date: 14/05/19
 */
@ParametersAreNonnullByDefault
public interface Page extends Widget {
    default void onResize(int x, int y) {
    }

    default void overlay(@Nullable Page p) {
        throw new UnsupportedOperationException();
    }

    default Page overlay() {
        throw new UnsupportedOperationException();
    }

    default Vector2i mouse() {
        throw new UnsupportedOperationException();
    }

    class Impl extends WContainer implements Page {
        int multiplier = 1;
        Page overlay = null;
        Vector2i mouse = new Vector2i();
        static final int WIDTH = 1280;
        static final int HEIGHT = 720;

        @Override
        public boolean onKey(int code) {
            Ref.page = this;
            return (overlay != null && overlay.onKey(code)) || super.onKey(code);
        }

        @Override
        @Nullable
        public Vector2i mouse() {
            return mouse;
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            super.onRefresh(m);
            if (overlay != null) overlay.onRefresh(m);
        }

        @Override
        public boolean onDrag(@Nullable Vector2i mouse, boolean left) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            return (overlay != null && overlay.onDrag(m, left)) || super.onDrag(m, left);
        }

        @Override
        public boolean onPress(@Nullable Vector2i mouse, boolean left) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            return (overlay != null && overlay.onPress(m, left)) || super.onPress(m, left);
        }

        @Override
        public boolean onDrop(boolean left) {
            if (update()) Ref.page = this;
            return (overlay != null && overlay.onDrop(left)) || super.onDrop(left);
        }

        @Override
        public void onRemove() {
            if (update()) Ref.page = this;
            super.onRemove();
            if (overlay != null) overlay.onRemove();
        }

        @Override
        public final void onResize(int x, int y) {
            if (update()) Ref.page = this;
            multiplier = Math.max(Math.min((y + HEIGHT / 2 - 1) / HEIGHT, (x + WIDTH / 2 - 1) / WIDTH), 1);
            onLayout((x + multiplier - 1) / multiplier, (y + multiplier - 1) / multiplier);
            if (overlay != null) overlay.onResize(x, y);
        }

        @Override
        public void overlay(@Nullable Page p) {
            if (overlay != null) overlay.onRemove();
            overlay = p;
            Window window = Widget.window();
            Vector2i size = window.getSize();
            if (p != null) p.onResize(size.x, size.y);
        }

        protected boolean update() {
            return true;
        }

        public Page overlay() {
            return overlay;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            try (Painter.SMatrix s = p.matrix()) {
                s.scale(multiplier, multiplier, 1);
                super.onDraw(p, m);
            }
            if (overlay != null) overlay.onDraw(p, m);
        }

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            return (overlay != null && overlay.onClick(m, left)) || super.onClick(m, left);
        }

        @Override
        public boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            return (overlay != null && overlay.onTooltip(m, tooltip)) || super.onTooltip(m, tooltip);
        }

        @Override
        public void onMove(Vector2i mouse) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            super.onMove(m);
            if (overlay != null) overlay.onMove(m);
        }

        @Override
        public boolean onScroll(@Nullable Vector2i mouse, int diff) {
            this.mouse = mouse;
            if (update()) Ref.page = this;
            Vector2i m = convert(mouse);
            return (overlay != null && overlay.onScroll(m, diff)) || super.onScroll(m, diff);
        }

        private Vector2i convert(@Nullable Vector2i in) {
            return in == null ? null : new Vector2i(in.x / multiplier, in.y / multiplier);
        }

        protected void onLayout(int x, int y) {
        }
    }

    class Once extends Impl {
        public Once(BiConsumer<Painter, Vector2i> c) {
            this(c, 0, 0);
        }

        public Once(BiConsumer<Painter, Vector2i> c, int x, int y) {
            put(c::accept, x, y);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            Ref.page.overlay(null);
        }

        @Override
        protected boolean update() {
            return false;
        }
    }

    class Overlay extends Impl {
        @Override
        protected boolean update() {
            return false;
        }
    }

    class Center extends Impl {
        Widget widget;
        int xs, ys;

        public Center(Widget w, int xs, int ys) {
            widget = w;
            this.xs = xs;
            this.ys = ys;
        }

        @Override
        protected void onLayout(int x, int y) {
            super.onLayout(x, y);
            put(widget, (x - xs) / 2, (y - ys) / 2);
        }

        @Override
        protected boolean update() {
            return false;
        }
    }

    class Ref {
        static Page page;

        public static Page getPage() {
            return page;
        }
    }
}
