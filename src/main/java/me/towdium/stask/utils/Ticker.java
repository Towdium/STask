package me.towdium.stask.utils;

import java.util.function.IntConsumer;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
public class Ticker {
    final long step;
    long next = Long.MIN_VALUE;
    IntConsumer callback;

    public Ticker(double seconds) {
        this(seconds, null);
    }

    public Ticker(double seconds, IntConsumer callback) {
        this((long) (seconds * 1000000000), callback);
    }

    public Ticker(long nanos) {
        this(nanos, null);
    }

    public Ticker(long nanos, IntConsumer callback) {
        this.callback = callback;
        this.step = nanos;
    }

    public void sync() {
        if (next == Long.MIN_VALUE) next = System.nanoTime();

        long time = System.nanoTime();
        if (time >= next) {
            int skipped = (int) ((time - next) / step);
            if (skipped > 0) callback.accept(skipped);
            next += skipped * step + step;
        } else {
            Utilities.sleep((next - time) / 1000000, (int) ((next - time) % 100000));
            next += step;
        }
    }
}
