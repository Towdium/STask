package me.towdium.stask.logic;

import me.towdium.stask.utils.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Author: Towdium
 * Date: 26/06/19
 */
public class Event {
    @SuppressWarnings("unchecked")
    public static class Bus {
        Cache<Class, List<Consumer>> subs = new Cache<>(i -> new ArrayList<>());
        Cache<Class, List<Predicate>> gates = new Cache<>(i -> new ArrayList<>());

        public boolean attempt(Event e) {
            for (Class i : getType(e)) {
                List<Predicate> ps = gates.get(i);
                if (ps != null) {
                    for (Predicate p : ps) {
                        if (p.test(e)) return false;
                    }
                }
            }
            return true;
        }

        public void post(Event e) {
            for (Class i : getType(e)) {
                List<Consumer> cs = subs.get(i);
                if (cs != null) {
                    for (Consumer c : cs) c.accept(e);
                }
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

        public <T extends Event> void subscribe(Class<T> e, Consumer<T> c) {
            subs.get(e).add(c);
        }

        public <T extends Event> void gate(Class<T> e, Predicate<T> p) {
            gates.get(e).add(p);
        }
    }

    public static abstract class ETask extends Event {
        Graph.Task task;
        Cluster.Processor processor;

        public ETask(Graph.Task task, Cluster.Processor processor) {
            this.task = task;
            this.processor = processor;
        }

        public static class Scheduled extends ETask {
            public Scheduled(Graph.Task task, Cluster.Processor processor) {
                super(task, processor);
            }
        }

        public static class Executed extends ETask {
            public Executed(Graph.Task task, Cluster.Processor processor) {
                super(task, processor);
            }
        }

        public static class Completed extends ETask {
            public Completed(Graph.Task task, Cluster.Processor processor) {
                super(task, processor);
            }
        }

        public static class Cancelled extends ETask {
            public Cancelled(Graph.Task task, Cluster.Processor processor) {
                super(task, processor);
            }
        }
    }

    public static abstract class EGame extends Event {
        public static class Reset extends EGame {
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
