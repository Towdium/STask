package me.towdium.stask;

import me.towdium.stask.render.Painter;
import me.towdium.stask.render.Window;

import static org.lwjgl.opengl.GL11.*;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
public class STask {
    public static void main(String[] args) {
        new Window() {
            @Override
            public void loop() {
                glClear(GL_COLOR_BUFFER_BIT);
                glPushMatrix();
                //glTranslatef(200, 200, 0);
                Painter.drawChar('a', 400, 20);
                Painter.drawString("asdadrgr卧槽\n回复勾画出", 400, 60);
                Painter.drawTexture("pic.png", 300, 600);
                glPopMatrix();
            }
        }.run();
    }
}
