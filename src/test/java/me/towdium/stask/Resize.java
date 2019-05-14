package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Window;
import me.towdium.stask.utils.time.Ticker;

/**
 * Author: Towdium
 * Date: 14/05/19
 */
public class Resize {
    public static void main(String[] args) {
        Impl impl = new Impl();

        Ticker ticker = new Ticker(1 / 200f);
        try (Window w = new Window("Render", impl)) {
            w.display();
            while (!w.isFinished()) {
                w.tick();
                ticker.sync();
            }
        }
    }

    static class Impl extends Page.Simple {
        @Override
        protected void onLayout(int x, int y) {
            clear();
            add(0, y - 10, (p, mouse) -> p.drawRect(0, 0, x, 10));
        }
    }
}
