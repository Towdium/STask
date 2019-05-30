package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Log;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
public class Schedule {
    Map<Task, Assignment> tasks = new IdentityHashMap<>();
    Map<Processor, SortedMap<Integer, Assignment>> processors = new IdentityHashMap<>();
    static final Assignment CONFLICT = new Assignment.Con();
    static final Assignment DEPENDENCY = new Assignment.Dep();

    public Assignment assign(Task task, Processor processor) {
        return assign(task, processor, -1);
    }

    public Assignment assign(Task task, Processor processor, int time) {
        Log.client.info("add");
        int total = processor.cost(task);
        int start = attempt(task, processor, time == -1 ? 0 : time);
        if (start == -1) return DEPENDENCY;
        else if (time != -1 && start > time) return CONFLICT;
        Assignment a = new Assignment(task, processor, start, start + total);
        processors.computeIfAbsent(processor, i -> new TreeMap<>()).put(start, a);
        tasks.put(task, a);
        return a;
    }

    @Nullable
    public Assignment getAssignment(Task t) {
        return tasks.get(t);
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

    public void cancel(Assignment a) {
        Log.client.info("remove");
        tasks.remove(a.task);
        processors.get(a.processor).remove(a.start);
    }

    public Map<Processor, SortedMap<Integer, Assignment>> getProcessors() {
        return Collections.unmodifiableMap(processors);
    }

    public enum Result {SUCCESS, DEPENDENCY, CONFLICT}

    public static class Assignment {
        Task task;
        Processor processor;
        int start, end;

        private Assignment() {
        }

        public Assignment(Task task, Processor processor, int start, int end) {
            this.task = task;
            this.processor = processor;
            this.start = start;
            this.end = end;
        }

        public Task getTask() {
            return task;
        }

        public Processor getProcessor() {
            return processor;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public Result getResult() {
            return Result.SUCCESS;
        }

        static class Dep extends Assignment {
            @Override
            public Result getResult() {
                return Result.DEPENDENCY;
            }
        }

        static class Con extends Assignment {
            @Override
            public Result getResult() {
                return Result.CONFLICT;
            }
        }
    }
}
