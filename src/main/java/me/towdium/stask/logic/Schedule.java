package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Log;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
@ParametersAreNonnullByDefault
public class Schedule {
    Map<Task, Assignment> tasks = new IdentityHashMap<>();
    Map<Processor, SortedMap<Float, Assignment>> processors = new IdentityHashMap<>();
    static final Assignment CONFLICT = new Assignment.Con();
    static final Assignment DEPENDENCY = new Assignment.Dep();

    public Assignment assign(Task task, Processor processor) {
        return assign(task, processor, -1);
    }

    public Assignment assign(Task task, Processor processor, float time) {
        Log.client.info("add");
        int total = processor.cost(task);
        float start = attempt(task, processor, time == -1 ? 0 : time);
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

    public float attempt(Task task, Processor processor) {
        return attempt(task, processor, 0);
    }

    public float attempt(Task task, Processor processor, float earliest) {
        float start = 0, end;
        for (Map.Entry<Task, Integer> i : task.after.entrySet()) {
            Assignment a = tasks.get(i.getKey());
            if (a == null) return -1;
            start = Math.max(start, a.end);
        }
        int total = processor.cost(task);
        SortedMap<Float, Assignment> m = processors.get(processor);
        if (m == null) return Math.max(start, earliest);
        SortedMap<Float, Assignment> t = m.tailMap(start);
        for (Map.Entry<Float, Assignment> i : t.entrySet()) {
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

    public Map<Processor, SortedMap<Float, Assignment>> getProcessors() {
        return Collections.unmodifiableMap(processors);
    }

    public enum Result {SUCCESS, DEPENDENCY, CONFLICT}

    public static class Assignment {
        Task task;
        Processor processor;
        float start, end;

        private Assignment() {
        }

        public Assignment(Task task, Processor processor, float start, float end) {
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

        public float getStart() {
            return start;
        }

        public float getEnd() {
            return end;
        }

        public float getDuration() {
            return end - start;
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
