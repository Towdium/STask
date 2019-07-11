package me.towdium.stask.logic;

import me.towdium.stask.client.Widget;
import me.towdium.stask.utils.Cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;
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
        Cache<Class, WeakHashMap<Object, List<Consumer>>> subs = new Cache<>(i -> new WeakHashMap<>());
        Cache<Class, WeakHashMap<Object, List<Predicate>>> gates = new Cache<>(i -> new WeakHashMap<>());

        private Bus() {
        }

        public boolean attempt(Event e) {
            for (Class i : getType(e)) {
                WeakHashMap<Object, List<Predicate>> ps = gates.get(i);
                if (ps != null && ps.values().stream().flatMap(Collection::stream)
                        .anyMatch(j -> !j.test(e))) return false;
            }
            return true;
        }

        public void post(Event e) {
            for (Class i : getType(e)) {
                WeakHashMap<Object, List<Consumer>> cs = subs.get(i);
                if (cs != null) cs.values().stream().flatMap(Collection::stream).forEach(j -> j.accept(e));
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

        public <T extends Event> void subscribe(Class<T> e, Object o, Consumer<T> c) {
            subs.get(e).computeIfAbsent(o, i -> new ArrayList<>()).add(c);
        }

        public <T extends Event> void gate(Class<T> e, Object o, Predicate<T> p) {
            gates.get(e).computeIfAbsent(o, i -> new ArrayList<>()).add(p);
        }
    }

    public static abstract class ETask extends Event {
        Graph.Task task;


        public ETask(Graph.Task task) {
            this.task = task;
        }

        public static class Pick extends ETask {
            Widget source;

            public Pick(Graph.Task task, Widget source) {
                super(task);
                this.source = source;
            }
        }

        public static class Schedule extends ETask {
            Cluster.Processor processor;

            public Schedule(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }

        public static class Executed extends ETask {
            Cluster.Processor processor;

            public Executed(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }

        public static class Completed extends ETask {
            Cluster.Processor processor;

            public Completed(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }

        public static class Cancelled extends ETask {
            Cluster.Processor processor;

            public Cancelled(Graph.Task task, Cluster.Processor processor) {
                super(task);
                this.processor = processor;
            }
        }
    }

    public static abstract class EGame extends Event {
        public static class Reset extends EGame {
        }

        public static class Pause extends EGame {
        }

        public static class Start extends EGame {
        }

        public static class Step extends EGame {
        }

        public static class Leave extends EGame {
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
