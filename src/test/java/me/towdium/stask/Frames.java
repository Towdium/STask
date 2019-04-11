package me.towdium.stask;

import me.towdium.stask.gui.Widgets.WButtonText;
import me.towdium.stask.gui.Widgets.WContainer;
import me.towdium.stask.gui.Window;
import me.towdium.stask.utils.Counter;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.Ticker;
import me.towdium.stask.utils.Utilities;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
public class Frames {
    static Ticker ticker = new Ticker(1 / 200f, i -> Log.client.debug("Dropping " + i + " frame(s)"));
    static Counter counter = new Counter(1f, i -> Log.client.debug("FPS: " + i));

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Log.client.setLevel(Log.Priority.DEBUG);
        WContainer root = new WContainer();
        Window window = new Window("Frames", 800, 600, root);
        root.add(10, 10, new WButtonText(100, 50, "5").setListener(i -> Utilities.sleep(5)));
        root.add(120, 10, new WButtonText(100, 50, "10").setListener(i -> Utilities.sleep(10)));
        root.add(230, 10, new WButtonText(100, 50, "20").setListener(i -> Utilities.sleep(20)));
        root.add(340, 10, new WButtonText(100, 50, "40").setListener(i -> Utilities.sleep(40)));
        window.display();
        while (!window.isClosed()) {
            window.tick();
            counter.count();
            ticker.sync();
        }
    }
}
