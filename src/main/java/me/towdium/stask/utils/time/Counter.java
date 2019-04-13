package me.towdium.stask.utils.time;

import me.towdium.stask.utils.Tickable;

import java.util.function.IntConsumer;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
public class Counter implements Tickable {
    Timer timer;
    int stored = 0;
    int count = 0;
    IntConsumer callback;

    public Counter(double seconds, IntConsumer callback) {
        this.callback = callback;
        timer = new Timer(seconds, this::update);
    }

    public Counter(long nanos, IntConsumer callback) {
        this.callback = callback;
        timer = new Timer(nanos, this::update);
    }

    public void setListener(IntConsumer c) {
        this.callback = c;
    }

    @Override
    public void tick() {
        timer.tick();
        count++;
    }

    public int stored() {
        return stored;
    }

    private void update(int skipped) {
        stored = skipped == 0 ? count : 0;
        count = 0;
        callback.accept(stored);
    }
}
