package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Work;
import me.towdium.stask.utils.wrap.Pair;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
@ParametersAreNonnullByDefault
public class Schedule {
    Map<Graph.Task, Assignment> tasks = new IdentityHashMap<>();
    Map<Graph.Comm, Pair<Assignment, Assignment>> comms = new IdentityHashMap<>();
    Map<Processor, TimeLine<Assignment>> processors = new IdentityHashMap<>();
    BiPredicate<Assignment, Assignment> separator = (a, b) ->
            a.work instanceof Graph.Comm && b.work instanceof Graph.Comm;

    public List<Assignment> assign(Graph.Task task, Processor processor) {
        return assign(task, processor, -Float.MAX_VALUE);
    }

    @Nullable
    public List<Assignment> assign(Graph.Task task, Processor processor, float time) {
        List<Assignment> ret = new ArrayList<>();

        // remove existing
        Assignment old = tasks.get(task);
        if (old != null) {
            tasks.remove(task);
            processors.get(old.processor).remove(old);
            Consumer<Map<Graph.Task, Graph.Comm>> c = i -> {
                for (Graph.Comm j : i.values()) {
                    Pair<Assignment, Assignment> as = comms.get(j);
                    if (as == null) continue;
                    comms.remove(j);
                    processors.get(as.a.processor).remove(as.a);
                    processors.get(as.b.processor).remove(as.b);
                }
            };
            c.accept(task.after);
            c.accept(task.before);
        }

        // check time
        TimeAxis ta = attempt(task, processor);
        if (time == -Float.MAX_VALUE) {
            Float t = ta.earliest();
            if (t == null) return null;
            else time = t;
        } else if (!ta.contains(time)) return null;

        // write task
        Function<Processor, TimeLine<Assignment>> f = j -> new TimeLine<>(separator);
        Assignment a = new Assignment(task, processor, time, time + processor.cost(task));
        boolean b = processors.computeIfAbsent(processor, f).put(a.start, a.end, a);
        if (!b) throw new RuntimeException("Internal error");
        tasks.put(task, a);
        ret.add(a);

        // write communications
        Predicate<Assignment> p = j -> j.work instanceof Graph.Comm;

        TimeAxis center = processors.get(processor).space(p);
        for (Graph.Comm i : task.getAfter().values()) {
            Assignment as = tasks.get(i.src);
            TimeAxis src = processors.get(as.processor).space(p);
            src.remove(0, as.end);
            TimeAxis merge = center.intercept(src);
            float com = as.processor.comm(i.size, processor);
            if (com == 0) continue;
            merge.shrink(com);
            Float start = merge.earliest();
            Objects.requireNonNull(start, "Internal error");
            float end = start + com;
            Assignment a1 = new Assignment(i, as.processor, start, end);
            Assignment a2 = new Assignment(i, processor, start, end);
            Pair<Assignment, Assignment> pair = new Pair<>(a1, a2);
            comms.put(i, pair);
            ret.add(a1);
            ret.add(a2);
            processors.computeIfAbsent(as.processor, f).put(start, end, a1);
            processors.computeIfAbsent(processor, f).put(start, end, a2);
        }

        return ret;
    }

    @Nullable
    public Assignment getAssignment(Graph.Task t) {
        return tasks.get(t);
    }

    @SuppressWarnings("Duplicates")
    public TimeAxis attempt(Graph.Task task, Processor p) {
        Map<Processor, TimeAxis> space = new IdentityHashMap<>();
        for (Map.Entry<Processor, TimeLine<Assignment>> i : processors.entrySet()) {
            space.put(i.getKey(), i.getValue().space(j -> {
                if (j.work instanceof Graph.Task) return task == j.work;
                else if (j.work instanceof Graph.Comm) {
                    Graph.Comm c = (Graph.Comm) j.work;
                    boolean b1 = task.after.containsKey(c.src) && task == c.dst;
                    boolean b2 = task.before.containsKey(c.dst) && task == c.src;
                    return b1 || b2;
                }
                throw new RuntimeException("Internal error");
            }));
        }
        Function<Processor, TimeAxis> f = i -> {
            TimeAxis ret = new TimeAxis();
            ret.add(0, Float.MAX_VALUE);
            return ret;
        };
        TimeAxis center = space.computeIfAbsent(p, f);
        float after = 0, before = Float.MAX_VALUE;
        for (Graph.Comm t : task.after.values()) {
            Assignment a = tasks.get(t.src);
            TimeAxis src = space.computeIfAbsent(a.processor, f);
            src.remove(0, a.end);
            TimeAxis merge = center.intercept(src);
            float com = a.processor.comm(t.size, p);
            merge.shrink(com);
            Float start = merge.earliest();
            if (start == null) return new TimeAxis();
            float finish = start + com;
            after = Math.max(after, finish);
        }
        for (Graph.Comm t : task.before.values()) {
            Assignment a = tasks.get(t.dst);
            if (a == null) continue;
            TimeAxis src = space.computeIfAbsent(a.processor, f);
            src.remove(a.start, Float.MAX_VALUE);
            TimeAxis merge = center.intercept(src);
            float com = a.processor.comm(t.size, p);
            merge.shrink(com);
            Float start = merge.latest();
            if (start == null) return new TimeAxis();
            before = Math.min(before, start);
        }
        center.remove(0, after);
        center.remove(before, Float.MAX_VALUE);
        return center;
    }

    public static class Assignment {
        Work work;
        Processor processor;
        float start, end;

        public Assignment(Work work, Processor processor, float start, float end) {
            this.work = work;
            this.processor = processor;
            this.start = start;
            this.end = end;
        }

        public Work getWork() {
            return work;
        }

        public Processor getProcessor() {
            return processor;
        }

        public float getStart() {
            return start;
        }

        public float getEnd() {
            return end;
        }

        public float getDuration() {
            return end - start;
        }
    }

    static class TimeLine<T> {
        SortedMap<Float, Section<T>> sections = new TreeMap<>();
        Map<T, Float> time = new IdentityHashMap<>();
        BiPredicate<T, T> separator;

        public TimeLine(BiPredicate<T, T> separator) {
            this.separator = separator;
        }

        public boolean attempt(float start, float end, T item) {
            SortedMap<Float, Section<T>> head = sections.headMap(start);
            float f = head.isEmpty() ? 0 : head.lastKey();
            SortedMap<Float, Section<T>> cut = sections.subMap(f, end);
            for (Section<T> s : cut.values()) {
                if (s.overlap(start, end)) {
                    for (T t : s.events) {
                        if (!separator.test(t, item)) return false;
                    }
                }
            }
            return true;
        }

        public boolean put(float start, float end, T item) {
            if (!attempt(start, end, item)) return false;
            SortedMap<Float, Section<T>> head = sections.headMap(start);
            float f = head.isEmpty() ? 0 : head.lastKey();
            SortedMap<Float, Section<T>> cut = sections.subMap(f, end);
            time.put(item, start);
            Consumer<Section<T>> c = s -> {
                if (s != null) sections.put(s.start, s);
            };
            Section<T> add = new Section<>(start, end, item);
            for (Section<T> s : cut.values()) {
                if (!s.overlap(start, end)) continue;
                float a = Math.max(start, s.start);
                float b = Math.min(end, s.end);
                Section<T> mid = null;
                if (start > s.start) mid = s.cutRight(a);
                else c.accept(add.cutLeft(a));
                if (end < s.end) {
                    Section<T> right = s.cutRight(b);
                    s.events.addAll(add.events);
                    c.accept(right);
                    break;
                } else {
                    s.events.addAll(add.events);
                    add.cutLeft(b);
                }
                if (start > s.start) c.accept(mid);
            }
            c.accept(add);
            return true;
        }

        public void remove(T item) {
            float t = Objects.requireNonNull(time.get(item), "No such element");
            Iterator<Section<T>> it = sections.tailMap(t).values().iterator();
            Section<T> a, b = null;
            while (it.hasNext()) {
                a = b;
                b = it.next();
                if (!b.events.contains(item)) break;
                b.events.remove(item);
                if (b.events.isEmpty()) {
                    it.remove();
                    continue;
                }
                if (a != null && a.events.containsAll(b.events) && b.events.containsAll(a.events)) {
                    it.remove();
                    a.end = b.end;
                }
            }
            time.remove(item);
        }

        public TimeAxis space(Predicate<T> ignore) {
            TimeAxis ret = new TimeAxis();
            float start = 0;
            for (Section<T> s : sections.values()) {
                boolean pass = true;
                for (T t : s.events)
                    if (!ignore.test(t)) pass = false;
                if (pass) continue;
                if (s.start > start) ret.sections.put(start, s.start);
                start = s.end;
            }
            if (start != Float.MAX_VALUE) ret.sections.put(start, Float.MAX_VALUE);
            return ret;
        }

        static class Section<T> {
            float start, end;
            Set<T> events = Collections.newSetFromMap(new IdentityHashMap<>());

            public Section(float start, float end, T item) {
                this.start = start;
                this.end = end;
                events.add(item);
            }

            public Section(float start, float end, Set<T> events) {
                this.start = start;
                this.end = end;
                this.events.addAll(events);
            }

            public boolean overlap(float start, float end) {
                return start < this.end && end > this.start;
            }

            public boolean contains(float t) {
                return start <= t && end > t;
            }

            public Section<T> cutLeft(float t) {
                if (start < t && end > t) {
                    Section<T> ret = new Section<>(start, t, events);
                    start = t;
                    return ret;
                } else return null;
            }

            public Section<T> cutRight(float t) {
                if (start < t && end > t) {
                    Section<T> ret = new Section<>(t, end, events);
                    end = t;
                    return ret;
                } else return null;
            }
        }
    }

    public static class TimeAxis {
        SortedMap<Float, Float> sections = new TreeMap<>();

        public void add(float start, float end) {
            SortedMap<Float, Float> head = sections.headMap(start);
            if (!head.isEmpty() && head.get(head.lastKey()) >= start) {
                start = head.lastKey();
                sections.remove(head.lastKey());
            }
            SortedMap<Float, Float> sub = sections.subMap(start, end);
            if (!sub.isEmpty()) {
                end = Math.max(end, sub.get(sub.lastKey()));
            }
            Iterator<Map.Entry<Float, Float>> it = sub.entrySet().iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
            sections.put(start, end);
        }

        public void remove(float start, float end) {
            SortedMap<Float, Float> head = sections.headMap(start);
            if (!head.isEmpty() && head.get(head.lastKey()) >= start) {
                sections.put(head.lastKey(), start);
            }
            SortedMap<Float, Float> sub = sections.subMap(start, end);
            if (!sub.isEmpty() && sub.get(sub.lastKey()) >= end) {
                sections.put(end, sub.get(sub.lastKey()));
            }
            Iterator<Map.Entry<Float, Float>> it = sub.entrySet().iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        public TimeAxis intercept(TimeAxis t) {
            Iterator<Map.Entry<Float, Float>> ita = sections.entrySet().iterator();
            Iterator<Map.Entry<Float, Float>> itb = t.sections.entrySet().iterator();
            TimeAxis ret = new TimeAxis();
            Map.Entry<Float, Float> ia = null, ib = null;
            while (true) {
                int next;
                if (ia == null) next = 0;
                else if (ib == null) next = 1;
                else next = ia.getValue() > ib.getValue() ? 1 : 0;
                Iterator<Map.Entry<Float, Float>> itn = (next == 0 ? ita : itb);
                if (!itn.hasNext()) return ret;
                Map.Entry<Float, Float> in = itn.next();
                if (next == 0) ia = in;
                else ib = in;
                if (ia == null || ib == null) continue;
                float start = Math.max(ia.getKey(), ib.getKey());
                float end = Math.min(ia.getValue(), ib.getValue());
                if (start < end) ret.sections.put(start, end);
            }
        }

        public void shrink(float f) {
            Iterator<Map.Entry<Float, Float>> it = sections.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Float, Float> i = it.next();
                if (i.getValue() - i.getKey() >= f) i.setValue(i.getValue() - f);
                else it.remove();
            }
        }

        public boolean contains(float f) {
            if (sections.containsKey(f)) return true;
            SortedMap<Float, Float> head = sections.headMap(f);
            return !head.isEmpty() && head.get(head.lastKey()) >= f;
        }

        @Nullable
        public Float earliest() {
            return earliest(0);
        }

        @Nullable
        public Float earliest(float f) {
            SortedMap<Float, Float> head = sections.headMap(f);
            if (!head.isEmpty() && head.get(head.lastKey()) > f) return f;
            SortedMap<Float, Float> tail = sections.tailMap(f);
            return tail.isEmpty() ? null : tail.firstKey();
        }

        @Nullable
        public Float latest() {
            return latest(Float.MAX_VALUE);
        }

        @Nullable
        public Float latest(float f) {
            SortedMap<Float, Float> head = sections.headMap(f);
            if (head.isEmpty()) return null;
            else return Math.min(head.get(head.lastKey()), f);
        }
    }
}
