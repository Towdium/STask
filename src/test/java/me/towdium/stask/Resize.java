package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Window;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 14/05/19
 */
@ParametersAreNonnullByDefault
public class Resize {
    public static void main(String[] args) {
        Impl impl = new Impl();

        try (Window w = new Window("Render", impl)) {
            w.display();
            while (!w.isFinished()) w.tick();
        }
    }

    static class Impl extends Page.Simple {
        @Override
        protected void onLayout(int x, int y) {
            clear();
            put((p, mouse) -> p.drawRect(0, 0, x, 10), 0, y - 10);
        }
    }
}