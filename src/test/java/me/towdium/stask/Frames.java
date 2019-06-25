package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Widgets.WButtonText;
import me.towdium.stask.client.Window;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.Utilities;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
@ParametersAreNonnullByDefault
public class Frames {
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Log.client.setLevel(Log.Priority.TRACE);
        Page.Impl root = new Page.Impl();
        root.put(new WButtonText(100, 50, "5").setListener(i -> Utilities.sleep(5)), 10, 10);
        root.put(new WButtonText(100, 50, "10").setListener(i -> Utilities.sleep(10)), 10, 120);
        root.put(new WButtonText(100, 50, "20").setListener(i -> Utilities.sleep(20)), 10, 230);
        root.put(new WButtonText(100, 50, "40").setListener(i -> Utilities.sleep(40)), 10, 340);


        try (Window window = new Window("Frames", root)) {
            window.display();
            window.setDebug(true);
            while (!window.isFinished()) window.tick();
        }
    }
}
