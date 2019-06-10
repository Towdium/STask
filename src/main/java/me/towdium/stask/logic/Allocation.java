package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Cache;
import org.apache.commons.collections4.list.TreeList;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 09/06/19
 */
public class Allocation {
    Cache<Processor, List<Task>> processors = new Cache<>(i -> new TreeList<>());
    Map<Task, Processor> tasks = new IdentityHashMap<>();

    public void allocate(Task t, Processor p) {
        processors.get(p).add(t);
    }

    public void allocate(Task t, Processor p, int pos) {
        processors.get(p).add(pos, t);
    }

    public boolean allocated(Task t) {
        return tasks.containsKey(t);
    }

    public void remove(Task t) {
        Processor p = tasks.get(t);
        if (p == null) return;
        processors.get(p).remove(t);
    }

    public void remove(Processor p, int idx) {
        processors.get(p).remove(idx);
    }

    public List<Task> getTasks(Processor p) {
        return processors.get(p);
    }
}
