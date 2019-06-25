package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Cache;
import org.apache.commons.collections4.list.TreeList;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Author: Towdium
 * Date: 09/06/19
 */
public class Allocation {
    Cache<Processor, List<Node>> processors = new Cache<>(i -> new TreeList<>());
    Map<Task, Processor> task2processor = new HashMap<>();
    Map<Task, Node> task2node = new HashMap<>();

    public void allocate(Task t, Processor p) {
        Node n = new Node(t);
        processors.get(p).add(n);
        task2processor.put(t, p);
        task2node.put(t, n);
    }

    public void allocate(Task t, Processor p, int pos) {
        Node n = new Node(t);
        processors.get(p).add(pos, n);
        task2processor.put(t, p);
        task2node.put(t, n);
    }

    public boolean allocated(Task t) {
        return task2processor.containsKey(t);
    }

    public void remove(Task t) {
        Processor p = task2processor.get(t);
        if (p == null) return;
        Node n = task2node.remove(t);
        if (n == null) throw new RuntimeException("Internal error");
        processors.get(p).remove(n);
        task2processor.remove(t);
    }

    public void remove(Processor p, int idx) {
        Node n = processors.get(p).remove(idx);
        task2processor.remove(n.task);
        task2node.remove(n.task);
    }

    @Nullable
    public Node getNode(Task t) {
        return task2node.get(t);
    }

    @Nullable
    public Processor getProcessor(Task t) {
        return task2processor.get(t);
    }

    public List<Node> getTasks(Processor p) {
        return processors.get(p);
    }

    public void reset() {
        processors.clear();
        task2processor.clear();
    }

    public static class Node {
        Task task;
        List<Graph.Comm> comms = new ArrayList<>();

        public Node(Task t) {
            task = t;
            comms.addAll(task.after.values());
        }

        public Task getTask() {
            return task;
        }

        public List<Graph.Comm> getComms() {
            return new ArrayList<>(comms);
        }

        public void setComms(List<Graph.Comm> l) {
            Set<Graph.Comm> s = new HashSet<>(comms);
            for (Graph.Comm i : l)
                if (!s.remove(i))
                    throw new RuntimeException("Invalid comm");
            l.addAll(s);
            comms = l;
        }
    }
}
