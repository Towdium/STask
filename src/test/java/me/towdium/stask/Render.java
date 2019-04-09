package me.towdium.stask;

import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.States;
import me.towdium.stask.gui.Widgets.WContainer;
import me.towdium.stask.gui.Widgets.WDrag;
import me.towdium.stask.gui.Window;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.IdentityHashMap;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public class Render {
    public static void main(String[] args) {
        WContainer root = new WContainer();
        root.add(0, 0, i -> {
            try (States.SMatrix mat = States.matrix();
                 States.State col = States.color(0xFFAA00)) {
                mat.translate(50, 50);
                Painter.drawTextWrapped("Here is some test text. 这是一段测试文本。", 440, 0, 250);
                Painter.drawTexture("pic.png", 0, 0, 100, 100, 0, 0);
                Painter.drawTexture("pic.png", 110, 0, 100, 100, 305, 0, 10, 10, 2);
                try (States.State mask1 = States.mask(330, 0, 99, 100);
                     States.State mask2 = States.mask(330, 0, 100, 99)) {
                    Painter.drawTexture("pic.png", 330, 0, 100, 100, 305, 0, 10, 10, 2);
                }
                Painter.drawRect(220, 0, 100, 100);
                try (States.State color2 = States.color(0xAAFF0000)) {
                    Painter.drawRect(220, 0, 50, 100);
                }
            }
        });

        root.add(50, 200, new WDTestA(true));
        root.add(110, 200, new WDTestA(false));
        root.add(170, 200, new WDTestA(false));
        root.add(280, 200, new WDTestB());
        Window.display(root);
        while (!Window.finished()) Window.tick();
        Window.destroy();
    }

    static class WDTestA extends WDrag {
        boolean hold;

        public WDTestA(boolean hold) {
            super(50, 50);
            this.hold = hold;
        }

        @Override
        @SuppressWarnings("unused")
        public void onDraw(Vector2i mouse) {
            super.onDraw(mouse);
            try (States.State color = States.color(0x111111)) {
                Painter.drawRect(0, 0, 50, 50);
            }

            if (hold) {
                try (States.State prior = States.priority(true);
                     States.SMatrix mat = States.matrix()) {
                    if (isSending()) mat.translate(mouse.x - 25, mouse.y - 25);
                    try (States.State color = States.color(0x888888)) {
                        Painter.drawRect(0, 0, 50, 50);
                    }
                    try (States.State color = States.color(0x777777)) {
                        Painter.drawRect(10, 10, 30, 30);
                    }
                }
            } else {
                if (isReceiving()) {
                    try (States.State color = States.color(0x444444)) {
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
    }

    static class WDTestB extends WContainer {
        IdentityHashMap<WDrag, Vector2i> pos = new IdentityHashMap<>();

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
            if (pos.containsKey(WDrag.sender) && pos.containsKey(WDrag.receiver)) {
                int x1 = pos.get(WDrag.sender).x;
                int x2 = pos.get(WDrag.receiver).x;
                Painter.drawRect(Math.min(x1, x2) + 25, 58, Math.abs(x2 - x1), 2);
                Painter.drawRect(x1 + 24, 50, 2, 10);
                Painter.drawRect(x2 + 24, 50, 2, 10);
            }
        }

        class Drag extends WDTestA {
            public Drag(boolean hold) {
                super(hold);
            }

            @Override
            public boolean canReceive(Object o) {
                return !hold && o instanceof Integer && (Integer) o == 2;
            }

            @Override
            public @Nullable Object canSend() {
                return hold ? 2 : null;
            }
        }
    }
}
