package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widgets.WContainer;
import me.towdium.stask.client.Widgets.WDrag;
import me.towdium.stask.client.Window;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.IdentityHashMap;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public class Render {
    public static void main(String[] args) {
        Page.Simple root = new Page.Simple();
        root.add(0, 0, (p, m) -> {
            try (Painter.SMatrix mat = p.matrix();
                 Painter.State col = p.color(0xFFAA00)) {
                mat.translate(50, 50);
                p.drawTextWrapped("Here is some test text. 这是一段测试文本。", 440, 0, 250);
                p.drawTexture("pic.png", 0, 0, 100, 100, 0, 0);
                p.drawTexture("pic.png", 110, 0, 100, 100, 306, 1, 8, 8, 1);
                try (Painter.State mask1 = p.mask(330, 0, 99, 100);
                     Painter.State mask2 = p.mask(330, 0, 100, 99)) {
                    p.drawTexture("pic.png", 330, 0, 100, 100, 306, 1, 8, 8, 1);
                }
                p.drawRect(220, 0, 100, 100);
                try (Painter.State color2 = p.color(0xAAFF0000)) {
                    p.drawRect(220, 0, 50, 100);
                }
            }
        });

        root.add(50, 200, new WDTestA(true));
        root.add(110, 200, new WDTestA(false));
        root.add(170, 200, new WDTestA(false));
        root.add(280, 200, new WDTestB());


        try (Window w = new Window("Render", root)) {
            w.display();
            while (!w.isFinished()) w.tick();
        }

    }

    static class WDTestA extends WDrag {
        boolean hold;

        public WDTestA(boolean hold) {
            super(50, 50);
            this.hold = hold;
        }

        @Override
        @SuppressWarnings("unused")
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            try (Painter.State color = p.color(0x111111)) {
                p.drawRect(0, 0, 50, 50);
            }

            if (hold) {
                if (isSending()) {
                    try (Painter.State prior = p.priority(true);
                         Painter.SMatrix mat = p.matrix()) {
                        mat.translate(mouse.x - 25, mouse.y - 25);
                        try (Painter.State color = p.color(0x888888)) {
                            p.drawRect(0, 0, 50, 50);
                        }
                        try (Painter.State color = p.color(0x777777)) {
                            p.drawRect(10, 10, 30, 30);
                        }
                    }
                } else {
                    try (Painter.State color = p.color(0x888888)) {
                        p.drawRect(0, 0, 50, 50);
                    }
                    try (Painter.State color = p.color(0x777777)) {
                        p.drawRect(10, 10, 30, 30);
                    }
                }
            } else {
                if (isReceiving()) {
                    try (Painter.State color = p.color(0x444444)) {
                        p.drawRect(0, 0, 50, 50);
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
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            if (pos.containsKey(WDrag.sender) && pos.containsKey(WDrag.receiver)) {
                int x1 = pos.get(WDrag.sender).x;
                int x2 = pos.get(WDrag.receiver).x;
                p.drawRect(Math.min(x1, x2) + 25, 58, Math.abs(x2 - x1), 2);
                p.drawRect(x1 + 24, 50, 2, 10);
                p.drawRect(x2 + 24, 50, 2, 10);
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
