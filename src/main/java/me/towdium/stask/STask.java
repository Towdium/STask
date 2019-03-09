package me.towdium.stask;

import me.towdium.stask.gui.IWidget;
import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.Window;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class STask {
    static int shader;

    public static void main(String[] args) {

        Window.run(new IWidget() {
            @Override
            public void onDraw() {
                Painter.drawText("一段\n测试\n文本", 330, Painter.fontAscent);
                Painter.drawTexture("pic.png", 0, 0, 100, 100, 0, 0);
                Painter.drawTexture("pic.png", 110, 0, 100, 100, 305, 0, 10, 10, 2);
                Painter.clipSet(220, 0, 100, 100);
                Painter.drawTexture("pic.png", 220, 0, 120, 120, 305, 0, 10, 10, 2);
                Painter.clipRemove();
            }
        }, () -> {
        });
    }
}
