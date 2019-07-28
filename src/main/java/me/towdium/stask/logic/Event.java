package me.towdium.stask.logic;

import me.towdium.stask.client.Widget;
import me.towdium.stask.utils.Cache;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        public boolean attempt(Event e) {
            List<Predicate> l = getType(e).stream()
                    .flatMap(i -> gates.get(i).values().stream())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            return l.stream().allMatch(i -> i.test(e));
        }

        public void post(Event e) {
            List<Consumer> l = getType(e).stream()
                    .flatMap(i -> subs.get(i).values().stream())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            l.forEach(i -> i.accept(e));
        }

        @SuppressWarnings("Duplicates")
        public <T extends Event> void subscribe(Class<T> e, Object o, Consumer<T> c) {
            subs.get(e).computeIfAbsent(o, i -> new ArrayList<>()).add(c);
            record.computeIfAbsent(o, i -> new HashSet<>()).add(e);
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
            gates.get(e).computeIfAbsent(o, i -> new ArrayList<>()).add(p);
            record.computeIfAbsent(o, i -> new HashSet<>()).add(e);
        }

        public void cancel(Object o) {
            Set<Class> classes = record.remove(o);
            if (classes == null) return;
            classes.forEach(i -> {
                subs.get(i).remove(o);
                gates.get(i).remove(o);
            });
        }
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

        public static class Comm extends ETask {
            public final List<Graph.Comm> order;

            public Comm(Graph.Task task, List<Graph.Comm> order) {
                super(task);
                this.order = order;
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

        public static class Speed extends EGame {
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

        public static class Tick extends EGame {
            public final int count;

            public Tick(int count) {
                this.count = count;
            }
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
