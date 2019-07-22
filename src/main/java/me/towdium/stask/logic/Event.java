package me.towdium.stask.logic;

import me.towdium.stask.client.Widget;
import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.wrap.Pair;
import me.towdium.stask.utils.wrap.Trio;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Author: Towdium
 * Date: 26/06/19
 */
public class Event {
    public static class Filter implements Predicate<Event> {
        Predicate<Event> predicate;

        public Filter(Object owner) {
            Event.Bus.BUS.gate(Event.class, owner, this);
        }

        @Override
        public boolean test(Event event) {
            return predicate == null || predicate.test(event);
        }

        public void update(Predicate<Event> p) {
            predicate = p;
        }
    }

    @SuppressWarnings("unchecked")
    public static class Bus {
        public static final Bus BUS = new Bus();
        Cache<Class, HashMap<Object, List<Consumer>>> subs = new Cache<>(i -> new HashMap<>());
        Cache<Class, HashMap<Object, List<Predicate>>> gates = new Cache<>(i -> new HashMap<>());
        HashMap<Object, Set<Class>> record = new HashMap<>();
        boolean active = false;
        List<Pair<Inst, Trio<Class, Object, Object>>> cached = new ArrayList<>();

        private void clean() {
            for (Pair<Inst, Trio<Class, Object, Object>> i : cached) {
                if (i.a == Inst.SUBS) subscribe(i.b.a, i.b.b, (Consumer) i.b.c);
                else if (i.a == Inst.GATE) gate(i.b.a, i.b.b, (Predicate) i.b.c);
                else cancel(i.b.b);
            }
            cached.clear();
        }


        public boolean attempt(Event e) {
            if (active) throw new IllegalStateException("Event bus active");
            active = true;
            for (Class i : getType(e)) {
                HashMap<Object, List<Predicate>> ps = gates.get(i);
                boolean b = ps != null && ps.values().stream()
                        .flatMap(Collection::stream)
                        .anyMatch(j -> !j.test(e));
                if (b) {
                    active = false;
                    clean();
                    return false;
                }
            }
            active = false;
            clean();
            return true;
        }

        public void post(Event e) {
            if (active) throw new IllegalStateException("Event bus active");
            active = true;
            for (Class i : getType(e)) {
                HashMap<Object, List<Consumer>> cs = subs.get(i);
                if (cs != null) cs.values().stream().flatMap(Collection::stream).forEach(j -> j.accept(e));
            }
            active = false;
            clean();
        }

        @SuppressWarnings("Duplicates")
        public <T extends Event> void subscribe(Class<T> e, Object o, Consumer<T> c) {
            if (active) cached.add(new Pair<>(Inst.SUBS, new Trio<>(e, o, c)));
            else {
                subs.get(e).computeIfAbsent(o, i -> new ArrayList<>()).add(c);
                record.computeIfAbsent(o, i -> new HashSet<>()).add(e);
            }
        }

        private List<Class<? extends Event>> getType(Event e) {
            List<Class<? extends Event>> ret = new ArrayList<>();
            Class c = e.getClass();
            while (Event.class.isAssignableFrom(c)) {
                ret.add(c);
                c = c.getSuperclass();
            }
            return ret;
        }

        @SuppressWarnings("Duplicates")
        public <T extends Event> void gate(Class<T> e, Object o, Predicate<T> p) {
            if (active) cached.add(new Pair<>(Inst.GATE, new Trio<>(e, o, p)));
            else {
                gates.get(e).computeIfAbsent(o, i -> new ArrayList<>()).add(p);
                record.computeIfAbsent(o, i -> new HashSet<>()).add(e);
            }
        }

        public void cancel(Object o) {
            if (active) cached.add(new Pair<>(Inst.CANCEL, new Trio<>(null, o, null)));
            else {
                Set<Class> classes = record.remove(o);
                if (classes == null) return;
                classes.forEach(i -> {
                    subs.get(i).remove(o);
                    gates.get(i).remove(o);
                });
            }
        }

        enum Inst {SUBS, GATE, CANCEL}
    }

    public static abstract class ETask extends Event {
        public Graph.Task task;

        public ETask(Graph.Task task) {
            this.task = task;
        }

        public static class Pick extends ETask {
            public Widget source;

            public Pick(Graph.Task task, Widget source) {
                super(task);
                this.source = source;
            }
        }

        public static class Schedule extends ETask {
            public Cluster.Processor processor;

            public Schedule(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }

        public static class Executed extends ETask {
            public Cluster.Processor processor;

            public Executed(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }

        public static class Completed extends ETask {
            public Cluster.Processor processor;

            public Completed(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }

        public static class Cancelled extends ETask {
            public Cluster.Processor processor;

            public Cancelled(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }
    }

    public static class ETutorial extends Event {
        public final String id;

        public ETutorial(String id) {
            this.id = id;
        }
    }

    public static abstract class EGame extends Event {
        public static class Reset extends EGame {
        }

        public static class SpeedUp extends EGame {
        }

        public static class SpeedDown extends EGame {
        }

        public static class Pause extends EGame {
        }

        public static class Start extends EGame {
        }

        public static class Step extends EGame {
        }

        public static class Leave extends EGame {
        }

        public static class Finish extends EGame {
        }

        public static class Failed extends EGame {

        }
    }

    public static abstract class EGraph extends Event {
        public Graph graph;

        public EGraph(Graph graph) {
            this.graph = graph;
        }

        public static class Complete extends EGraph {
            public Complete(Graph graph) {
                super(graph);
            }
        }

        public static class Append extends EGraph {
            public Append(Graph graph) {
                super(graph);
            }
        }
    }
}
