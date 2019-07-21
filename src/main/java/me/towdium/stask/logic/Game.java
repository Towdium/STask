package me.towdium.stask.logic;

import com.google.gson.Gson;
import me.towdium.stask.logic.Cluster.Processor;
import me.towdium.stask.logic.Event.EGame;
import me.towdium.stask.logic.Event.EGraph;
import me.towdium.stask.logic.Event.ETask;
import me.towdium.stask.logic.Graph.Comm;
import me.towdium.stask.logic.Graph.Task;
import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Tickable;
import me.towdium.stask.utils.Utilities;
import me.towdium.stask.utils.time.Timer;
import me.towdium.stask.utils.wrap.Trio;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static me.towdium.stask.logic.Event.Bus.BUS;


/**
 * Author: Towdium
 * Date: 10/06/19
 */
public class Game implements Tickable {  // TODO remove extra space and disable early fetch
    static final int RATE = 20;
    static final float SPEED = 1f / RATE;
    Tutorial tutorial;
    Cluster cluster;
    Schedule schedule;
    History history = new History();
    Map<Processor, Status> processors = new HashMap<>();
    Map<Comm, Processor> output = new HashMap<>();
    Set<Task> finished = new HashSet<>();
    Map<Task, Processor> executing = new HashMap<>();
    SortedMap<Integer, List<Graph>> graphs = new TreeMap<>();
    Set<Graph> unfinished;
    List<Integer> aims;
    String desc;
    int count = 0;
    boolean statik;
    boolean running = false;
    int speed = 1;
    String name;
    Timer timer = new Timer(SPEED, i -> update());

    public Game(String id) {
        String json = Utilities.readString("/games/" + id + ".json");
        Gson gson = new Gson();
        Pojo.Game pojo = gson.fromJson(json, Pojo.Game.class);
        cluster = new Cluster(pojo.cluster);
        tutorial = pojo.tutorial == null ? null : Tutorial.get(pojo.tutorial, this);
        schedule = new Schedule();
        statik = pojo.times == null;
        aims = new ArrayList<>(pojo.aims);
        name = id;
        desc = pojo.desc;
        for (int i = 0; i < pojo.graphs.size(); i++)
            graphs.computeIfAbsent(statik ? 0 : pojo.times.get(i),
                    j -> new ArrayList<>()).add(new Graph(pojo.graphs.get(i)));
        for (Processor i : cluster.processors.values())
            processors.put(i, new Status(i));
        unfinished = graphs.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public int getSpeed() {
        return speed;
    }

    public String getDesc() {
        return desc;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSeconds() {
        return count / RATE;
    }

    public List<Integer> getAims() {
        return aims;
    }

    public boolean isStatic() {
        return statik;
    }

    @Nullable
    public Tutorial getTutorial() {
        return tutorial;
    }

    public int getCount() {
        return count;
    }

    public boolean isRunning() {
        return running;
    }

    public String getName() {
        return name;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Collection<Graph> getGraphs() {
        return statik ? graphs.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    public Schedule getSchedule() {
        return schedule;
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
        schedule.reset();
        unfinished = graphs.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        BUS.post(new EGame.Reset());
    }

    public void pause() {
        running = false;
    }

    @Override
    public void tick() {
        timer.tick();
    }

    private void update() {
        for (int j = 0; j < speed; j++) {
            if (!running) return;
            if (count % RATE == 0 && !statik) {
                int t = count / RATE;
                SortedMap<Integer, List<Graph>> m = graphs.tailMap(t);
                m = m.headMap(t + 1);
                m.values().stream().flatMap(Collection::stream)
                        .forEach(i -> BUS.post(new EGraph.Append(i)));
            }
            boolean valid = false;
            for (Status i : processors.values()) i.tickPre();
            history.update();
            for (Status i : processors.values())
                if (i.tickPost()) valid = true;
            if (!valid && statik && running) {
                running = false;
                BUS.post(new EGame.Failed());
            }
            count++;
        }
    }

    public History getHistory() {
        return history;
    }

    public boolean available(Game.Status s, Game.Status d) {
        if (cluster.comm == 0) return true;
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
            count = 0;
            comms.clear();
            input.clear();
        }

        public void tickPre() {
            List<Schedule.Node> ts = schedule.getTasks(processor);
            for (int i = 0; i < ts.size(); i++) {
                Schedule.Node n = ts.get(i);
                for (Comm c : n.comms)
                    if (!attempt(c)) return;
                if (working == null && i == 0) {
                    working = n.task;
                    executing.put(working, processor);
                    schedule.remove(processor, 0);
                }
            }
        }

        private boolean attempt(Comm c) {
            Processor src = output.get(c);
            if (cluster.comm == 0 && src != null) return true;
            if (input.contains(c) || src == processor) return true;

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
                    BUS.post(new ETask.Completed(working, processor));
                    for (Comm i : working.getSuccessor().values()) output.put(i, processor);
                    finished.add(working);
                    executing.remove(working);
                    Graph g = working.getGraph();
                    boolean complete = true;
                    for (Task t : g.getTasks()) {
                        if (!finished.contains(t)) {
                            complete = false;
                            break;
                        }
                    }
                    if (complete) {
                        BUS.post(new EGraph.Complete(g));
                        unfinished.remove(g);
                        if (unfinished.isEmpty()) {
                            BUS.post(new EGame.Finish());
                            running = false;
                        }
                    }
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
        }

        public Map<Graph.Work, Vector2i> getRecord(Processor p) {
            return records.get(p);
        }

        public void reset() {
            records.clear();
        }
    }
}
