package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Window;
import me.towdium.stask.client.widgets.WContainer;
import me.towdium.stask.client.widgets.WDrag;
import me.towdium.stask.client.widgets.WOwner;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.IdentityHashMap;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public class Render {
    public static void main(String[] args) {
        Page.Impl root = new Page.Impl();
        root.put((p, m) -> {
            try (Painter.SMatrix matrix = p.matrix();
                 Painter.State ignore = p.color(0xFFAA00)) {
                matrix.translate(50, 50);
                p.drawTextWrapped("Here is some test text. 这是一段测试文本。", 440, 0, 250);
                p.drawTexture("pic.png", 0, 0, 100, 100, 0, 0);
                p.drawTexture("pic.png", 110, 0, 100, 100, 306, 1, 8, 8, 1);
                try (Painter.State ignore1 = p.mask(330, 0, 99, 100);
                     Painter.State ignore2 = p.mask(330, 0, 100, 99)) {
                    p.drawTexture("pic.png", 330, 0, 100, 100, 306, 1, 8, 8, 1);
                }
                p.drawRect(220, 0, 100, 100);
                try (Painter.State ignore1 = p.color(0xAAFF0000)) {
                    p.drawRect(220, 0, 50, 100);
                }
            }
        }, 0, 0);

        root.put(new WDTestA(true), 50, 200);
        root.put(new WDTestA(false), 110, 200);
        root.put(new WDTestA(false), 170, 200);
        root.put(new WDTestB(), 280, 200);


        try (Window w = new Window("Render", root)) {
            w.display();
            while (!w.isFinished()) w.tick();
        }

    }

    static class WDTestA extends WDrag.Impl {
        boolean hold;

        public WDTestA(boolean hold) {
            super(50, 50);
            this.hold = hold;
        }

        @Override
        @SuppressWarnings("unused")
        public void onDraw(Painter p, Vector2i mouse) {
            try (Painter.State color = p.color(0x111111)) {
                p.drawRect(0, 0, 50, 50);
            }

            if (hold) {
                if (isSending(this)) {
                    try (//Painter.State prior = p.priority(true);
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
                if (isReceiving(this)) {
                    try (Painter.State color = p.color(0x444444)) {
                        p.drawRect(0, 0, 50, 50);
                    }
                }
            }
        }

        @Override
        public boolean onAttempt(Object o, Vector2i mouse) {
            return !hold && o instanceof Integer && (Integer) o == 1;
        }

        @Override
        public @Nullable Object onStarting() {
            return hold ? 1 : null;
        }

        @Override
        public void onReceived(Object o) {
            hold = true;
        }

        @Override
        public void onSucceeded() {
            hold = false;
        }
    }

    static class WDTestB extends WContainer {
        IdentityHashMap<WOwner, Vector2i> pos = new IdentityHashMap<>();

        public WDTestB() {
            add(0, 0, true);
            add(60, 0, false);
            add(120, 0, false);
        }

        public void add(int x, int y, boolean hold) {
            Drag d = new Drag(hold);
            put(d, x, y);
            pos.put(d, new Vector2i(x, y));
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            if (pos.containsKey(WDrag.getSender()) && pos.containsKey(WDrag.getReceiver())) {
                int x1 = pos.get(WDrag.getSender()).x;
                int x2 = pos.get(WDrag.getReceiver()).x;
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
            public boolean onAttempt(Object o, Vector2i mouse) {
                return !hold && o instanceof Integer && (Integer) o == 2;
            }

            @Override
            public @Nullable Object onStarting() {
                return hold ? 2 : null;
            }
        }
    }
}
