package me.towdium.stask;

import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.Widget;
import me.towdium.stask.gui.Widgets.WContainer;
import me.towdium.stask.gui.Widgets.WDrag;
import me.towdium.stask.gui.Window;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.Ticker;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Author: Towdium
 * Date: 11/04/19
 */
public class Windows {
    static Ticker ticker = new Ticker(1 / 200f);

    public static void main(String[] args) {
        Log.client.setLevel(Log.Priority.DEBUG);
        Set<Window> windows = new HashSet<>();
        windows.add(new Window("Window A", 230, 90, create()));
        windows.add(new Window("Window B", 230, 90, create()));

        for (Window w : windows) w.display();
        while (!windows.isEmpty()) {
            for (Iterator<Window> i = windows.iterator(); i.hasNext(); ) {
                Window window = i.next();
                if (window.isFinished()) {
                    window.close();
                    i.remove();
                } else window.tick();
            }
            ticker.sync();
        }
    }

    public static Widget create() {
        return new WContainer()
                .add(20, 20, new Drag(false))
                .add(90, 20, new Drag(true))
                .add(160, 20, new Drag(false))
                .add(0, 0, new Paint());
    }

    static class Drag extends WDrag {
        boolean hold;

        public Drag(boolean hold) {
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
                try (Painter.State prior = p.priority(true);
                     Painter.SMatrix mat = p.matrix()) {
                    if (!isSending()) {
                        try (Painter.State color = p.color(0x888888)) {
                            p.drawRect(0, 0, 50, 50);
                        }
                        try (Painter.State color = p.color(0x777777)) {
                            p.drawRect(10, 10, 30, 30);
                        }
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

    static class Paint extends WDrag {
        public Paint() {
            super(0, 0);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            if (sender instanceof Drag) {
                try (Painter.SMatrix mat = p.matrix()) {
                    mat.translate(mouse.x - 25, mouse.y - 25);
                    try (Painter.State color = p.color(0x888888)) {
                        p.drawRect(0, 0, 50, 50);
                    }
                    try (Painter.State color = p.color(0x777777)) {
                        p.drawRect(10, 10, 30, 30);
                    }
                }
            }
        }

        @Override
        public void onReceived() {
        }

        @Override
        public boolean canReceive(Object o) {
            return false;
        }

        @Override
        public void onSent() {
        }

        @Override
        public @Nullable Object canSend() {
            return null;
        }
    }
}
