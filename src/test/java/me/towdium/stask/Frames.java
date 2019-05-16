package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Widgets.WButtonText;
import me.towdium.stask.client.Window;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.Utilities;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
public class Frames {
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Log.client.setLevel(Log.Priority.TRACE);
        Page.Simple root = new Page.Simple();
        root.add(10, 10, new WButtonText(100, 50, "5").setListener(i -> Utilities.sleep(5)));
        root.add(120, 10, new WButtonText(100, 50, "10").setListener(i -> Utilities.sleep(10)));
        root.add(230, 10, new WButtonText(100, 50, "20").setListener(i -> Utilities.sleep(20)));
        root.add(340, 10, new WButtonText(100, 50, "40").setListener(i -> Utilities.sleep(40)));


        try (Window window = new Window("Frames", root)) {
            window.display();
            window.setDebug(true);
            while (!window.isFinished()) window.tick();
        }
    }
}
