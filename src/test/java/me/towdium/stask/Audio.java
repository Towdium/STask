package me.towdium.stask;

import me.towdium.stask.client.Speaker;
import me.towdium.stask.client.Widgets.WContainer;
import me.towdium.stask.client.Window;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.time.Ticker;

/**
 * Author: Towdium
 * Date: 12/04/19
 */


public class Audio {
    public static void main(String[] args) throws InterruptedException {
        Ticker ticker = new Ticker(1 / 200f, i -> Log.client.debug("Dropping " + i + " frame(s)"));
        try (Window w = new Window("Audio", 200, 100, new WContainer());
             Speaker s = new Speaker()) {
            Log.client.setLevel(Log.Priority.TRACE);
            w.display();
            for (int i = 0; i < 2; i++) {
                s.play("example.ogg");
                Thread.sleep(200);
            }
            while (!w.isFinished()) {
                w.tick();
                s.tick();
                ticker.sync();
            }
        }
    }
}
