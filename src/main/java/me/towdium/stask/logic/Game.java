package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Tickable;

import java.util.*;

/**
 * Author: Towdium
 * Date: 10/06/19
 */
public class Game implements Tickable {
    static final float SPEED = 0.1f;
    Cluster cluster;
    Graph graph;
    Allocation allocation;
    Policy policy;
    Map<Processor, Status> processors = new HashMap<>();
    Map<Graph.Comm, Processor> output = new HashMap<>();
    Set<Task> finished = new HashSet<>();
    Map<Task, Processor> executing = new HashMap<>();
    boolean running = false;

    public Game(Cluster c, Graph g, Allocation a, Policy p) {
        this.cluster = c;
        this.graph = g;
        this.allocation = a;
        this.policy = p;
        for (Processor i : c.processors.values())
            processors.put(i, new Status(i));
    }

    public Map<Processor, Status> getProcessors() {
        return processors;
    }

    public Status getProcessor(Processor p) {
        return processors.get(p);
    }

    public boolean finished(Task t) {
        return finished.contains(t);
    }

    public boolean executing(Task t) {
        return executing.containsKey(t);
    }

    public void start() {
        running = true;
    }

    public void reset() {
        running = false;
        for (Processor i : cluster.processors.values())
            processors.put(i, new Status(i));
        output = new HashMap<>();
        finished = new HashSet<>();
        executing = new HashMap<>();
    }

    public void pause() {
        running = false;
    }

    @Override
    public void tick() {
        if (!running) return;
        boolean valid = false;
        for (Status i : processors.values()) i.tickPre();
        for (Status i : processors.values())
            if (i.tickPost()) valid = true;
        if (!valid) running = false;
    }

    public static class Policy {
        boolean multipleComms = false;
        boolean immediateComms = false;
        boolean parallelComms = false;

        public boolean available(Status s) {
            if (immediateComms) return true;
            if (s.working == null || parallelComms)
                return multipleComms || s.comms.isEmpty();
            else return false;
        }
    }

    public class Status {
        Processor processor;
        Task working;
        float progress;
        Map<Graph.Comm, Float> comms = new LinkedHashMap<>();
        Set<Graph.Comm> input = new HashSet<>();

        public Status(Processor processor) {
            this.processor = processor;
        }

        public void tickPre() {
            if (working == null) {
                List<Task> ts = allocation.getTasks(processor);
                if (!ts.isEmpty()) {
                    Task t = ts.get(0);
                    boolean ready = true;
                    for (Graph.Comm c : t.getAfter().values()) {
                        if (!input.contains(c)) {
                            if (!comms.containsKey(c)) {
                                Processor src = output.get(c);
                                if (src != null && policy.available(this)) {
                                    if (src != processor) {
                                        ready = false;
                                        Status s = processors.get(src);
                                        if (policy.available(s)) {
                                            s.comms.put(c, 0f);
                                            comms.put(c, 0f);
                                        }
                                    }
                                } else ready = false;
                            } else ready = false;
                        }
                    }
                    if (ready) {
                        for (Graph.Comm i : t.getAfter().values()) input.remove(i);
                        working = t;
                        executing.put(working, processor);
                        allocation.remove(working);
                    }
                }
            }
        }

        public boolean tickPost() {
            boolean ret = false;
            if (working != null) {
                ret = true;
                progress += SPEED / working.time * processor.getSpeed() * processor.getSpeedup(working.type);
                if (progress > 1) {
                    for (Graph.Comm i : working.getBefore().values()) output.put(i, processor);
                    finished.add(working);
                    executing.remove(working);
                    working = null;
                    progress = 0;
                }
            }
            Iterator<Map.Entry<Graph.Comm, Float>> it = comms.entrySet().iterator();
            while (it.hasNext()) {
                ret = true;
                Map.Entry<Graph.Comm, Float> i = it.next();
                float p = i.getValue() + SPEED / i.getKey().size * cluster.comm;
                if (p > 1) {
                    input.add(i.getKey());
                    it.remove();
                } else i.setValue(p);
            }
            return ret;
        }

        public Processor getProcessor() {
            return processor;
        }

        public Task getWorking() {
            return working;
        }

        public float getProgress() {
            return progress;
        }

        public Map<Graph.Comm, Float> getComms() {
            return comms;
        }
    }
}
