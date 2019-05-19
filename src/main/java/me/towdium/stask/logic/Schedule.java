package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Task;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
public class Schedule {
    Map<Task, Assignment> tasks = new IdentityHashMap<>();
    Map<Processor, SortedMap<Integer, Assignment>> processors = new IdentityHashMap<>();

    public Result assign(Task task, Processor processor) {
        return assign(task, processor, -1);
    }

    public Result assign(Task task, Processor processor, int time) {
        int total = processor.cost(task);
        int start = attempt(task, processor, time == -1 ? 0 : time);
        if (start == -1) return Result.DEPENDENCY;
        else if (time != -1 && start > time) return Result.CONFLICT;
        Assignment a = new Assignment(task, processor, start, start, start + total);
        processors.computeIfAbsent(processor, i -> new TreeMap<>()).put(start, a);
        tasks.put(task, a);
        return Result.SUCCESS;
    }

    public int attempt(Task task, Processor processor) {
        return attempt(task, processor, 0);
    }

    public int attempt(Task task, Processor processor, int earliest) {
        int start = 0, end;
        for (Map.Entry<Task, Integer> i : task.after.entrySet()) {
            Assignment a = tasks.get(i.getKey());
            if (a == null) return -1;
            start = Math.max(start, a.end);
        }
        int total = processor.cost(task);
        SortedMap<Integer, Assignment> m = processors.get(processor);
        if (m == null) return Math.max(start, earliest);
        SortedMap<Integer, Assignment> t = m.tailMap(start);
        for (Map.Entry<Integer, Assignment> i : t.entrySet()) {
            end = i.getKey();
            if (end - start > total) break;
        }
        return Math.max(start, earliest);
    }

    public enum Result {SUCCESS, DEPENDENCY, CONFLICT}

    public static class Assignment {
        Task task;
        Processor processor;
        int start, process, end;

        public Assignment(Task task, Processor processor, int start, int process, int end) {
            this.task = task;
            this.processor = processor;
            this.start = start;
            this.process = process;
            this.end = end;
        }
    }
}
