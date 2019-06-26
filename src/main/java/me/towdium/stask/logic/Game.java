package me.towdium.stask.logic;

import com.google.gson.Gson;
import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Graph.Comm;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Tickable;
import me.towdium.stask.utils.Utilities;
import me.towdium.stask.utils.wrap.Trio;
import org.joml.Vector2i;

import java.util.*;

/**
 * Author: Towdium
 * Date: 10/06/19
 */
public class Game implements Tickable {
    static final float SPEED = 0.1f;
    Tutorial tutorial;
    Cluster cluster;
    Graph graph;
    Allocation allocation;
    History history = new History();
    Map<Processor, Status> processors = new HashMap<>();
    Map<Comm, Processor> output = new HashMap<>();
    Set<Task> finished = new HashSet<>();
    Map<Task, Processor> executing = new HashMap<>();
    boolean running = false;

    public Game(String id) {
        String json = Utilities.readString("/games/" + id + ".json");
        Gson gson = new Gson();
        Pojo.Game pojo = gson.fromJson(json, Pojo.Game.class);
        cluster = new Cluster(pojo.cluster);
        graph = new Graph(pojo.graph);
        tutorial = Tutorial.get(pojo.tutorial);
        allocation = new Allocation();
        for (Processor i : cluster.processors.values())
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

    public boolean available(Game.Status s, Game.Status d) {
        if (cluster.policy.immediate) return true;
        if (!cluster.policy.background && (s.working != null || d.working != null)) return false;
        if (!cluster.policy.multiple && (!s.comms.isEmpty() || !d.comms.isEmpty())) return false;
        for (Trio<Float, Boolean, Processor> i : s.comms.values())
            if (!i.b && i.c == d.processor) return false;
        return true;
    }

    public class Status {
        Processor processor;
        Task working;
        float progress;
        Map<Comm, Trio<Float, Boolean, Processor>> comms = new HashMap<>();  // progress, input(true), companion
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
            List<Allocation.Node> ts = allocation.getTasks(processor);
            for (int i = 0; i < ts.size(); i++) {
                Allocation.Node n = ts.get(i);
                boolean ready = true;
                for (Comm c : n.comms) {
                    if (!attempt(c)) {
                        ready = false;
                        break;
                    }
                }
                if (working == null && i == 0 && ready) {
                    working = n.task;
                    executing.put(working, processor);
                    allocation.remove(processor, 0);
                    i--;
                }
            }
        }

        private boolean attempt(Comm c) {
            Processor src = output.get(c);
            if (input.contains(c) || src == processor || cluster.policy.immediate) return true;

            if (!comms.containsKey(c)) {
                if (src == null) return false;
                Status s = processors.get(src);
                if (available(s, this)) {
                    s.comms.put(c, new Trio<>(0f, false, processor));
                    comms.put(c, new Trio<>(0f, true, s.processor));
                }
            }
            return false;
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
            Iterator<Map.Entry<Comm, Trio<Float, Boolean, Processor>>> it = comms.entrySet().iterator();
            while (it.hasNext()) {
                ret = true;
                Map.Entry<Comm, Trio<Float, Boolean, Processor>> i = it.next();
                float p = i.getValue().a + SPEED / i.getKey().size * cluster.comm;
                if (p > 1) {
                    input.add(i.getKey());
                    it.remove();
                } else i.getValue().a = p;
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

        public Map<Comm, Trio<Float, Boolean, Processor>> getComms() {
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
