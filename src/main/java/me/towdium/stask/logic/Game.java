package me.towdium.stask.logic;

import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Comm;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Tickable;
import org.joml.Vector2i;

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
    History history = new History();
    Map<Processor, Status> processors = new HashMap<>();
    Map<Comm, Processor> output = new HashMap<>();
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

    public Cluster getCluster() {
        return cluster;
    }

    public Graph getGraph() {
        return graph;
    }

    public Allocation getAllocation() {
        return allocation;
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
        for (Status i : processors.values()) i.reset();
        output.clear();
        finished.clear();
        executing.clear();
        history.reset();
        allocation.reset();
    }

    public void pause() {
        running = false;
    }

    @Override
    public void tick() {
        if (!running) return;
        boolean valid = false;
        for (Status i : processors.values()) i.tickPre();
        history.update();
        for (Status i : processors.values())
            if (i.tickPost()) valid = true;
        if (!valid) running = false;
    }

    public History getHistory() {
        return history;
    }

    public static class Policy {
        boolean multipleComms = false;
        boolean immediateComms = false;
        boolean parallelComms = true;

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
        Map<Comm, Float> comms = new HashMap<>();
        Set<Comm> input = new HashSet<>();

        public Status(Processor processor) {
            this.processor = processor;
        }

        public void reset() {
            working = null;
            progress = 0;
            comms.clear();
            input.clear();
        }

        public void tickPre() {
            if (working == null) {
                List<Allocation.Node> ts = allocation.getTasks(processor);
                if (!ts.isEmpty()) {
                    Allocation.Node n = ts.get(0);
                    boolean ready = true;
                    for (Comm c : n.comms) {
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
                        for (Comm i : n.comms) input.remove(i);
                        working = n.task;
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
                    for (Comm i : working.getBefore().values()) output.put(i, processor);
                    finished.add(working);
                    executing.remove(working);
                    working = null;
                    progress = 0;
                }
            }
            Iterator<Map.Entry<Comm, Float>> it = comms.entrySet().iterator();
            while (it.hasNext()) {
                ret = true;
                Map.Entry<Comm, Float> i = it.next();
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

        public Map<Comm, Float> getComms() {
            return comms;
        }
    }

    public class History {
        Cache<Processor, Map<Graph.Work, Vector2i>> records = new Cache<>(i -> new HashMap<>());
        int count = 0;

        public void update() {
            processors.forEach((p, s) -> {
                Map<Graph.Work, Vector2i> record = records.get(p);
                Set<Graph.Work> ws = new HashSet<>();
                Graph.Task t = s.working;
                if (t != null) ws.add(t);
                ws.addAll(s.comms.keySet());
                for (Graph.Work i : ws) {
                    Vector2i v = record.get(i);
                    if (v != null) {
                        if (v.y != count) throw new RuntimeException("Multiple assignment");
                        else v.y++;
                    } else record.put(i, new Vector2i(count, count + 1));
                }
            });
            count++;
        }

        public Map<Graph.Work, Vector2i> getRecord(Processor p) {
            return records.get(p);
        }

        public void reset() {
            records.clear();
            count = 0;
        }
    }
}
