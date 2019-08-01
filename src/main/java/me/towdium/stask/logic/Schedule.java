package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Cache;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Author: Towdium
 * Date: 09/06/19
 */
public class Schedule {
    Cache<Processor, List<Node>> processors = new Cache<>(i -> new ArrayList<>());
    Map<Task, Node> tasks = new HashMap<>();

    public Schedule(Schedule schedule) {
        schedule.processors.foreach((p, ns) -> processors.get(p).addAll(ns));
        tasks.putAll(schedule.tasks);
    }

    public Schedule() {
    }

    public void allocate(Task t, Processor p) {
        Node old = tasks.get(t);
        if (old != null) processors.get(old.processor).remove(old);
        Node n = new Node(t, p);
        processors.get(p).add(n);
        tasks.put(t, n);
    }

    public void allocate(Task t, Processor p, int pos) {
        Node old = tasks.get(t);
        if (old != null) {
            List<Node> ns = processors.get(old.processor);
            int idx = ns.indexOf(old);
            if (idx < pos) pos--;
            ns.remove(idx);
        }
        Node n = new Node(t, p);
        processors.get(p).add(pos, n);
        tasks.put(t, n);
    }

    public boolean allocated(Task t) {
        return tasks.containsKey(t);
    }

    public Node remove(Task t) {
        Node n = tasks.remove(t);
        if (n == null) return null;
        processors.get(n.processor).remove(n);
        return n;
    }

    public void remove(Processor p, int idx) {
        Node n = processors.get(p).remove(idx);
        tasks.remove(n.task);
    }

    @Nullable
    public Node getNode(Task t) {
        return tasks.get(t);
    }

    @Nullable
    public Processor getProcessor(Task t) {
        Node n = tasks.get(t);
        return n == null ? null : n.processor;
    }

    public List<Node> getTasks(Processor p) {
        return processors.get(p);
    }

    public void reset() {
        processors.clear();
        tasks.clear();
    }

    public static class Node {
        Task task;
        Processor processor;
        List<Graph.Comm> comms = new ArrayList<>();

        public Node(Task t, Processor p) {
            task = t;
            processor = p;
            comms.addAll(task.predecessor.values());
        }

        public Processor getProcessor() {
            return processor;
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
