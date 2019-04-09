package me.towdium.stask.utils;

import java.util.function.IntConsumer;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
public class Counter {
    int stored = 0;
    int count = 0;
    long step;
    long next = Long.MIN_VALUE;
    IntConsumer callback;

    public Counter(double seconds) {
        this(seconds, null);
    }

    public Counter(double seconds, IntConsumer callback) {
        this((int) (seconds * 1000000000), callback);
    }

    public Counter(long nanos) {
        this(nanos, null);
    }

    public Counter(long nanos, IntConsumer callback) {
        this.step = nanos;
        this.callback = callback;
    }

    public void count() {
        count(1);
    }

    public int stored() {
        return stored;
    }

    public void count(int i) {
        if (next == Long.MIN_VALUE) next = System.nanoTime() + step;

        long now = System.nanoTime();
        if (now > next) {
            stored = now - next > step ? 0 : count;
            count = i;
            next += (int) ((now - next) / step) * step + step;
            if (callback != null) callback.accept(stored);
        } else count += i;
    }
}
