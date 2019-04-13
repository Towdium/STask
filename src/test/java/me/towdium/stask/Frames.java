package me.towdium.stask;

import me.towdium.stask.client.Widgets.WButtonText;
import me.towdium.stask.client.Widgets.WContainer;
import me.towdium.stask.client.Window;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.Utilities;
import me.towdium.stask.utils.time.Counter;
import me.towdium.stask.utils.time.Ticker;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
public class Frames {
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Log.client.setLevel(Log.Priority.DEBUG);
        WContainer root = new WContainer();
        root.add(10, 10, new WButtonText(100, 50, "5").setListener(i -> Utilities.sleep(5)));
        root.add(120, 10, new WButtonText(100, 50, "10").setListener(i -> Utilities.sleep(10)));
        root.add(230, 10, new WButtonText(100, 50, "20").setListener(i -> Utilities.sleep(20)));
        root.add(340, 10, new WButtonText(100, 50, "40").setListener(i -> Utilities.sleep(40)));

        Ticker ticker = new Ticker(1 / 200f, i -> Log.client.debug("Dropping " + i + " frame(s)"));
        Counter counter = new Counter(1f, i -> Log.client.debug("FPS: " + i));
        try (Window window = new Window("Frames", 800, 600, root)) {
            window.display();
            while (!window.isFinished()) {
                window.tick();
                counter.tick();
                ticker.sync();
            }
        }
    }
}
