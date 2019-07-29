package me.towdium.stask.logic.algorithms;

import me.towdium.stask.logic.Algorithm;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.logic.Schedule;
import me.towdium.stask.utils.wrap.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Towdium
 * Date: 02/07/19
 */
public abstract class AList implements Algorithm {
    @Override
    public void run(Collection<Graph> g, Cluster c, Schedule s) {
        Estimator e = new Estimator(c);
        List<Task> ts = g.stream().flatMap(i -> i.getTasks().stream())
                .map(i -> new Pair<>(i, priority(i)))
                .sorted((a, b) -> b.b - a.b)
                .map(i -> i.a).collect(Collectors.toCollection(LinkedList::new));

        Iterator<Task> it = ts.iterator();
        while (it.hasNext()) {
            Task i = it.next();
            if (i.getPredecessor().keySet().stream().allMatch(s::allocated)) {
                Cluster.Processor p = e.earliest();
                s.allocate(i, p);
                e.assign(i, p);
                it.remove();
                it = ts.iterator();
            }
        }
    }

    @Override
    public boolean accepts(Cluster c, List<Graph> gs) {
        return Estimator.accepts(c);
    }

    public abstract int priority(Task t);
}
