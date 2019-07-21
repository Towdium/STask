package me.towdium.stask.client;

import me.towdium.stask.utils.Tickable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Author: Towdium
 * Date: 16/05/19
 */
@ParametersAreNonnullByDefault
public class Animator implements Tickable {
    private Set<Entry> entries = new HashSet<>();
    private List<Runnable> pending = new ArrayList<>();

    @Override
    public void tick() {
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry i = it.next();
            i.tick();
            if (i.finished) it.remove();
        }
        for (Runnable i : pending) i.run();
        pending.clear();
    }

    public Entry addFloat(float x1, float x2, long mills,
                          Function<Float, Float> func, Consumer<Float> clbk, Runnable fin) {
        Entry ret = new EFloat(x1, x2, mills, func, clbk, fin);
        entries.add(ret);
        return ret;
    }

    public Entry addFloat(float x1, float x2, long mills,
                          Function<Float, Float> func, Consumer<Float> clbk) {
        //noinspection ConstantConditions
        return addFloat(x1, x2, mills, func, clbk, null);
    }

    public Entry addColor(int c1, int c2, long mills,
                          Function<Float, Float> func, Consumer<Integer> clbk, Runnable fin) {
        Entry ret = new EColor(c1, c2, mills, func, clbk, fin);
        entries.add(ret);
        return ret;
    }

    public Entry addColor(int c1, int c2, long mills,
                          Function<Float, Float> func, Consumer<Integer> clbk) {
        //noinspection ConstantConditions
        return addColor(c1, c2, mills, func, clbk, null);
    }

    public static class FQuadratic implements Function<Float, Float> {
        boolean inverse;

        public FQuadratic(boolean inverse) {
            this.inverse = inverse;
        }

        public static int unify(float reference, float duration, float distance) {
            float ratio = distance / reference;
            return (int) (duration * ratio * ratio);
        }

        @Override
        public Float apply(Float f) {
            float x = inverse ? 1 - f : f;
            float y = x * x;
            return inverse ? 1 - y : y;
        }
    }

    public static class FPeak implements Function<Float, Float> {
        float peak;

        public FPeak(float peak) {
            this.peak = peak;
        }

        @Override
        public Float apply(Float f) {
            float x = 2 * (f - 0.5f);
            return (1 - x * x) * peak;
        }
    }

    public abstract class Entry<T> implements Tickable {
        long start, duration;
        boolean finished;
        Function<Float, Float> function;
        Consumer<T> callback;
        Runnable finish;

        public Entry(long duration, Function<Float, Float> function,
                     Consumer<T> callback, @Nullable Runnable finish) {
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
                call(1);
                if (finish != null) pending.add(finish);
            } else {
                float progress = function.apply(diff / (float) duration);
                call(progress);
            }
        }

        public boolean finished() {
            return finished;
        }

        public void cancel() {
            entries.remove(this);
        }

        protected abstract void call(float progress);
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

    class EFloat extends Entry<Float> {
        float x1, x2;

        public EFloat(float x1, float x2, long duration, Function<Float, Float> function,
                      Consumer<Float> callback, @Nullable Runnable finish) {
            super(duration, function, callback, finish);
            this.x1 = x1;
            this.x2 = x2;
        }

        @Override
        protected void call(float progress) {
            callback.accept(x1 * (1 - progress) + x2 * progress);
        }
    }

    class EColor extends Entry<Integer> {
        int a1, a2, r1, r2, g1, g2, b1, b2;

        public EColor(int c1, int c2, long duration, Function<Float, Float> function,
                      Consumer<Integer> callback, @Nullable Runnable finish) {
            super(duration, function, callback, finish);
            a1 = c1 >> 24 & 0xFF;
            a2 = c2 >> 24 & 0xFF;
            r1 = c1 >> 16 & 0xFF;
            r2 = c2 >> 16 & 0xFF;
            g1 = c1 >> 8 & 0xFF;
            g2 = c2 >> 8 & 0xFF;
            b1 = c1 & 0xFF;
            b2 = c2 & 0xFF;
        }

        @Override
        protected void call(float progress) {
            int a = (int) (a1 * (1 - progress) + a2 * progress);
            int r = (int) (r1 * (1 - progress) + r2 * progress);
            int g = (int) (g1 * (1 - progress) + g2 * progress);
            int b = (int) (b1 * (1 - progress) + b2 * progress);
            callback.accept((a << 24) + (r << 16) + (g << 8) + b);
        }
    }
}
