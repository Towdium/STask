package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Speaker;
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
        try (Window w = new Window("Audio", new Page.Simple());
             Speaker s = new Speaker()) {
            Log.client.setLevel(Log.Priority.TRACE);
            Speaker.Source a = s.source();
            Speaker.Source b = s.source();
            w.display();
            long start = System.currentTimeMillis();
            a.play("example.ogg");
            Thread.sleep(200);
            b.play("example.ogg");
            boolean cleaned = false;
            while (!w.isFinished()) {
                if (!cleaned && System.currentTimeMillis() - start > 20000) {
                    Log.client.debug("Reference cleaned");
                    cleaned = true;
                    //noinspection UnusedAssignment
                    a = b = null;
                    System.gc();
                }
                w.tick();
                s.tick();
                ticker.sync();
            }
        }
    }
}
