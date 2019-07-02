package me.towdium.stask.logic;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Author: Towdium
 * Date: 02/07/19
 */
public interface Algorithm {
    static List<Graph.Task> getAssignable(Graph g, Schedule s) {
        Predicate<Graph.Task> p = i -> i.getPredecessor().keySet().stream().allMatch(s::allocated);
        return g.getTasks().stream().filter(p).collect(Collectors.toList());
    }

    static void traverseSuccessors(Graph.Task t, Consumer<List<Graph.Task>> c) {
        traverse(t, i -> new ArrayList<>(i.getSuccessor().keySet()), c);
    }

    static void traversePredessors(Graph.Task t, Consumer<List<Graph.Task>> c) {
        traverse(t, i -> new ArrayList<>(i.getPredecessor().keySet()), c);
    }

    static void traverse(Graph.Task t, Function<Graph.Task, List<Graph.Task>> f, Consumer<List<Graph.Task>> c) {
        List<List<Graph.Task>> stack = new ArrayList<>();
        List<Graph.Task> temp = new ArrayList<>();
        stack.add(f.apply(t));
        temp.add(t);
        if (stack.get(0).isEmpty()) {
            c.accept(temp);
            return;
        }
        LOOP:
        while (!stack.isEmpty()) {
            List<Graph.Task> last = stack.get(stack.size() - 1);
            for (int i = last.size() - 1; i >= 0; i--) {
                Graph.Task task = last.remove(i);
                List<Graph.Task> next = f.apply(task);
                temp.add(task);
                if (next.isEmpty()) c.accept(temp);
                else {
                    stack.add(next);
                    continue LOOP;
                }
                temp.remove(temp.size() - 1);
            }
            if (last.isEmpty()) {
                stack.remove(stack.size() - 1);
                temp.remove(temp.size() - 1);
            }
        }
    }

    void run(Collection<Graph> g, Cluster c, Schedule s);

    class Estimator {
        Map<Cluster.Processor, Integer> processors = new HashMap<>();
        Map<Graph.Task, Map<Cluster.Processor, Integer>> tasks = new HashMap<>();

        public Estimator(Cluster c) {
            for (Cluster.Processor p : c.getProcessors().values()) processors.put(p, 0);
        }

        public void assign(Graph.Task t, Cluster.Processor p) {
            int earliest = processors.get(p);
            for (Map.Entry<Graph.Task, Graph.Comm> i : t.getPredecessor().entrySet()) {
                Map<Cluster.Processor, Integer> task = tasks.get(i.getValue().getSrc());
                Objects.requireNonNull(task, "Predecessor not assigned");
                Integer self = task.get(p);
                if (self == null) {
                    Iterator<Map.Entry<Cluster.Processor, Integer>> it = task.entrySet().iterator();
                    if (it.hasNext()) {
                        Map.Entry<Cluster.Processor, Integer> e = it.next();
                        earliest = Math.max(earliest, e.getValue() + e.getKey().comm(i.getValue().size, p));
                    } else throw new RuntimeException("Internal error");
                } else earliest = Math.max(earliest, self);
            }
            int end = earliest + p.cost(t);
            tasks.computeIfAbsent(t, i -> new HashMap<>()).put(p, end);
            processors.put(p, end);
        }

        public Cluster.Processor earliest() {
            return processors.entrySet().stream()
                    .min(Comparator.comparingInt(Map.Entry::getValue))
                    .orElseThrow(() -> new RuntimeException("Cluster empty"))
                    .getKey();
        }
    }
}
