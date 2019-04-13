package me.towdium.stask.utils.time;

import me.towdium.stask.utils.Tickable;

import java.util.function.IntConsumer;

/**
 * Author: Towdium
 * Date: 13/04/19
 */
public class Timer implements Tickable {
    long step;
    long next = Long.MIN_VALUE;
    IntConsumer callback;

    public Timer(double seconds, IntConsumer callback) {
        this((int) (seconds * 1000000000), callback);
    }

    public Timer(long nanos, IntConsumer callback) {
        this.step = nanos;
        this.callback = callback;
    }

    @Override
    public void tick() {
        if (next == Long.MIN_VALUE) next = System.nanoTime() + step;

        long now = System.nanoTime();
        if (now > next) {
            long skipped = (now - next) / step;
            next += skipped * step + step;
            callback.accept((int) skipped);
        }
    }
}

