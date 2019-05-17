package me.towdium.stask.client;

import me.towdium.stask.utils.Tickable;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Author: Towdium
 * Date: 16/05/19
 */
public class Animator implements Tickable {
    private List<Entry> entries = new LinkedList<>();

    @Override
    public void tick() {
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry i = it.next();
            i.tick();
            if (i.finished) it.remove();
        }
    }

    public Entry add(float x1, float x2, long mills,
                     Function<Float, Float> func, Consumer<Float> clbk, Runnable fin) {
        Entry ret = new Entry(x1, x2, mills, func, clbk, fin);
        entries.add(ret);
        return ret;
    }

    public Entry add(float x1, float x2, long mills,
                     Function<Float, Float> func, Consumer<Float> clbk) {
        return add(x1, x2, mills, func, clbk, null);
    }

    public static class Entry implements Tickable {
        float x1, x2;
        long start, duration;
        boolean finished;
        Function<Float, Float> function;
        Consumer<Float> callback;
        Runnable finish;

        public Entry(float x1, float x2, long duration, Function<Float, Float> function,
                     Consumer<Float> callback, @Nullable Runnable finish) {
            this.x1 = x1;
            this.x2 = x2;
            this.start = System.currentTimeMillis();
            this.duration = duration;
            this.function = function;
            this.callback = callback;
            this.finish = finish;
        }

        @Override
        public void tick() {
            if (finished) return;
            long diff = System.currentTimeMillis() - start;
            if (diff > duration) {
                finished = true;
                callback.accept(x2);
                if (finish != null) finish.run();
            } else {
                float progress = function.apply(diff / (float) duration);
                callback.accept(x1 * (1 - progress) + x2 * progress);
            }
        }
    }

    public static class FBezier implements Function<Float, Float> {
        float k1, k2;

        public FBezier(float k1, float k2) {
            this.k1 = 1 - k1;
            this.k2 = 1 - k2;
        }

        @Override
        public Float apply(Float x1) {
            float x2 = 1 - x1;
            return 3 * k1 * x1 * x2 * x2 + 3 * k2 * x1 * x1 * x2 + x1 * x1 * x1;
        }
    }

    public static class FLinear implements Function<Float, Float> {
        @Override
        public Float apply(Float f) {
            return f;
        }
    }
}
