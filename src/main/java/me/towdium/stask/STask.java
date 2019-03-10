package me.towdium.stask;

import me.towdium.stask.gui.IWidget;
import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.Widgets.WContainer;
import me.towdium.stask.gui.Widgets.WDrag;
import me.towdium.stask.gui.Window;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class STask {
    public static void main(String[] args) {
        WContainer root = new WContainer();
        root.add(0, 0, i -> {
            Painter.matPush();
            Painter.matTranslate(50, 50, 0);
            Painter.colorPush(0xFFAA00);
            Painter.drawText("一段\n测试\n文本", 330, Painter.fontAscent);
            Painter.drawTexture("pic.png", 0, 0, 100, 100, 0, 0);
            Painter.colorPop();
            Painter.drawTexture("pic.png", 110, 0, 100, 100, 305, 0, 10, 10, 2);
            Painter.maskPush(220, 0, 90, 90);
            Painter.maskPush(240, 20, 200, 200);
            Painter.drawTexture("pic.png", 220, 0, 120, 120, 305, 0, 10, 10, 2);
            Painter.maskPop();
            Painter.maskPop();
            Painter.matPop();
            Painter.drawRect(500, 0, 100, 100);
        });

        root.add(200, 200, new WDTest(true));
        root.add(300, 200, new WDTest(false));
        root.add(400, 200, new WDTest(false));

        Window.run(root, () -> {
        });
    }

    static class WDTest extends WDrag {
        boolean hold;

        public WDTest(boolean hold) {
            this.hold = hold;
        }

        @Override
        public void onDraw(Vector2i mouse) {
            super.onDraw(mouse);
            Painter.colorPush(0x111111);
            Painter.drawRect(0, 0, 50, 50);
            Painter.colorPop();

            if (hold) {
                Painter.Priority.PRIORITIZED.apply();
                Painter.colorPush(0x888888);
                if (WDrag.sender == this) Painter.drawRect(mouse.x - 25, mouse.y - 25, 50, 50);
                else Painter.drawRect(0, 0, 50, 50);
                Painter.colorPop();
                Painter.Priority.NORMAL.apply();
            } else {
                if (inside(mouse) && sender != null) {
                    Painter.colorPush(0x444444);
                    Painter.drawRect(0, 0, 50, 50);
                    Painter.colorPop();
                }
            }
        }

        @Override
        public boolean canReceive(Object o) {
            return !hold;
        }

        @Override
        public @Nullable Object canSend() {
            return hold ? new Object() : null;
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
}
