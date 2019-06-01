package me.towdium.stask.utils.time;

import me.towdium.stask.utils.Tickable;
import me.towdium.stask.utils.Utilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.IntConsumer;

/**
 * Author: Towdium
 * Date: 13/04/19
 *
 * Call callback with given period
 */
@ParametersAreNonnullByDefault
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

    public Timer(double seconds) {
        //noinspection ConstantConditions
        this(seconds, null);
    }

    public Timer(long nanos) {
        //noinspection ConstantConditions
        this(nanos, null);
    }

    @Override
    public void tick() {
        invoke();
    }

    public void sync() {
        if (!invoke()) {
            long time = System.nanoTime();
            Utilities.sleep((next - time) / 1000000, (int) ((next - time) % 100000));
            if (callback != null) callback.accept(0);
            next += step;
        }
    }

    private boolean invoke() {
        if (next == Long.MIN_VALUE) next = System.nanoTime() + step;

        long now = System.nanoTime();
        if (now > next) {
            long skipped = (now - next) / step;
            next += skipped * step + step;
            if (callback != null) callback.accept((int) skipped);
            return true;
        } else return false;
    }
}

