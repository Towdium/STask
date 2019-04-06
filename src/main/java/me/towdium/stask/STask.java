package me.towdium.stask;

import me.towdium.stask.gui.IWidget;
import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.States;
import me.towdium.stask.gui.States.SMatrix;
import me.towdium.stask.gui.States.State;
import me.towdium.stask.gui.Widgets.WContainer;
import me.towdium.stask.gui.Widgets.WDrag;
import me.towdium.stask.gui.Window;
import me.towdium.stask.network.Discover;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.IdentityHashMap;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class STask {
    static volatile boolean alive = true;

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Discover.setup(() -> true);
        Discover.activate(System.out::println);




        WContainer root = new WContainer();
        root.add(0, 0, i -> {
            try (SMatrix mat = States.matrix();
                 State color1 = States.color(0xFFAA00)) {
                mat.translate(50, 50);
                Painter.drawText("Here is some test text. 这是一段测试文本。", 440, Painter.fontAscent, 250);
                Painter.drawTexture("pic.png", 0, 0, 100, 100, 0, 0);
                Painter.drawTexture("pic.png", 110, 0, 100, 100, 305, 0, 10, 10, 2);
                try (State mask1 = States.mask(330, 0, 99, 100);
                     State mask2 = States.mask(330, 0, 100, 99)) {
                    Painter.drawTexture("pic.png", 330, 0, 100, 100, 305, 0, 10, 10, 2);
                }
                Painter.drawRect(220, 0, 100, 100);
                try (State color2 = States.color(0xAAFF0000)) {
                    Painter.drawRect(220, 0, 50, 100);
                }

            }
        });

        root.add(50, 200, new WDTestA(true));
        root.add(110, 200, new WDTestA(false));
        root.add(170, 200, new WDTestA(false));
        root.add(280, 200, new WDTestB());

        Window.run(root, () -> {
        });

        Discover.deactivate();
        Discover.shutdown();
    }

    static class WDTestA extends WDrag {
        boolean hold;

        public WDTestA(boolean hold) {
            this.hold = hold;
        }

        @Override
        @SuppressWarnings("unused")
        public void onDraw(Vector2i mouse) {
            super.onDraw(mouse);
            try (State color = States.color(0x111111)) {
                Painter.drawRect(0, 0, 50, 50);
            }

            if (hold) {
                try (State prior = States.priority(true);
                     SMatrix mat = States.matrix()) {
                    if (WDrag.sender == this) mat.translate(mouse.x - 25, mouse.y - 25);
                    try (State color = States.color(0x888888)) {
                        Painter.drawRect(0, 0, 50, 50);
                    }
                    try (State color = States.color(0x777777)) {
                        Painter.drawRect(10, 10, 30, 30);
                    }
                }
            } else {
                if (sender != null && inside(mouse) && canReceive(sender.canSend())) {
                    try (State color = States.color(0x444444)) {
                        Painter.drawRect(0, 0, 50, 50);
                    }
                }
            }
        }

        @Override
        public boolean canReceive(Object o) {
            return !hold && o instanceof Integer && (Integer) o == 1;
        }

        @Override
        public @Nullable Object canSend() {
            return hold ? 1 : null;
        }

        @Override
        public void onReceived() {
            hold = true;
        }

        @Override
        public void onSent() {
            hold = false;
        }

        @Override
        protected boolean inside(Vector2i mouse) {
            return IWidget.inside(mouse, 50, 50);
        }
    }

    static class WDTestB extends WContainer {
        IdentityHashMap<WDrag, Vector2i> pos = new IdentityHashMap<>();
        WDrag a, b;

        public WDTestB() {
            add(0, 0, true);
            add(60, 0, false);
            add(120, 0, false);
        }

        public void add(int x, int y, boolean hold) {
            Drag d = new Drag(hold);
            add(x, y, d);
            pos.put(d, new Vector2i(x, y));
        }

        @Override
        public void onDraw(Vector2i mouse) {
            super.onDraw(mouse);
            if (a != null && b != null) {
                int x1 = pos.get(a).x;
                int x2 = pos.get(b).x;
                Painter.drawRect(Math.min(x1, x2) + 25, 58, Math.abs(x2 - x1), 2);
                Painter.drawRect(x1 + 24, 50, 2, 10);
                Painter.drawRect(x2 + 24, 50, 2, 10);
                a = b = null;
            }
        }

        class Drag extends WDTestA {
            public Drag(boolean hold) {
                super(hold);
            }

            @Override
            @SuppressWarnings("unused")
            public void onDraw(Vector2i mouse) {
                super.onDraw(mouse);
                if (sender == this && receiver != null && receiver.canReceive(canSend())) {
                    WDTestB.this.a = this;
                    WDTestB.this.b = receiver;
                }
            }

            @Override
            public boolean canReceive(Object o) {
                return !hold && o instanceof Integer && (Integer) o == 2;
            }

            @Override
            public @Nullable Object canSend() {
                return hold ? 2 : null;
            }

            @Override
            protected boolean inside(Vector2i mouse) {
                return IWidget.inside(mouse, 50, 50);
            }
        }
    }
}
