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
 * <p>
 * At speed = 1, it runs 1/4 actual speed, which is 20 ticks per actual second.
 * For one simulation second, it takes RATE * 4 = 80 ticks.
 */
public class Game implements Tickable {
    static final int RATE = 20;
    static final double SPEED = 1f / RATE;
    Tutorial tutorial;
    Cluster cluster;
    Schedule schedule, backup;
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
    int speed;
    String name;
    Timer timer = new Timer(SPEED, i -> update());

    public Game(Cluster c, List<Graph> gs) {
        cluster = c;
        schedule = new Schedule();
        statik = true;
        name = "Generated";
        desc = "";
        speed = 4;
        gs.forEach(i -> graphs.computeIfAbsent(0, j -> new ArrayList<>()).add(i));
        for (Processor i : cluster.processors.values()) processors.put(i, new Status(i));
        unfinished = graphs.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Game(String id) {
        String json = Utilities.readString("/games/" + id + ".json");
        Gson gson = new Gson();
        Pojo.Game pojo = gson.fromJson(json, Pojo.Game.class);
        cluster = new Cluster(pojo.cluster);
        tutorial = pojo.tutorial == null ? null : Tutorial.get(pojo.tutorial, this);
        schedule = new Schedule();
        statik = pojo.times == null;
        aims = pojo.aims == null ? null : new ArrayList<>(pojo.aims);
        name = id;
        desc = pojo.desc;
        speed = statik ? 4 : 1;
        for (int i = 0; i < pojo.graphs.size(); i++)
            graphs.computeIfAbsent(statik ? 0 : pojo.times.get(i),
                    j -> new ArrayList<>()).add(new Graph(pojo.graphs.get(i)));
        for (Processor i : cluster.processors.values()) processors.put(i, new Status(i));
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
        return count / RATE / 4;
    }

    @Nullable
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

    public List<Graph> getInitials() {
        return statik ? graphs.values().stream().flatMap(Collection::stream)
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    public List<Graph> getGraphs() {
        return graphs.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
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
        if (statik && count == 0) backup = new Schedule(schedule);
    }

    public void reset() {
        running = false;
        for (Status i : processors.values()) i.reset();
        output.clear();
        finished.clear();
        executing.clear();
        history.reset();
        if (statik && count != 0) schedule = backup;
        else schedule.reset();
        count = 0;
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
            if (count % (RATE * 4) == 0 && !statik) {
                int t = count / (RATE * 4);
                SortedMap<Integer, List<Graph>> m = graphs.tailMap(t);
                m = m.headMap(t + 1);
                m.values().stream().flatMap(Collection::stream)
                        .forEach(i -> BUS.post(new EGraph.Append(i)));
            }
            boolean valid = false;
            for (Processor i: cluster.layout) processors.get(i).tickPre1();
            for (Processor i: cluster.layout) processors.get(i).tickPre2();
            history.update();
            BUS.post(new EGame.Tick(count++));
            for (Processor i: cluster.layout)
                if (processors.get(i).tickPost()) valid = true;
            if (!valid && statik && running) {
                running = false;
                BUS.post(new EGame.Failed());
            }
        }
    }

    public History getHistory() {
        return history;
    }

    // available for communication from s to d
    public boolean available(Game.Status s, Game.Status d) {
        if (cluster.comm == 0) return true;
        if (!cluster.policy.background && (s.working != null || d.working != null)) return false;
        if (!cluster.policy.multiple && (!s.comms.isEmpty() || !d.comms.isEmpty())) return false;
        for (Trio<Double, Boolean, Processor> i : s.comms.values())
            if (!i.b && i.c == d.processor) return false;
        return true;
    }

    // available for task execution
    public boolean available(Game.Status s) {
        return s.working == null && (cluster.policy.background || s.comms.isEmpty());
    }

    public class Status {
        Processor processor;
        Task working;
        double progress;
        Map<Comm, Trio<Double, Boolean, Processor>> comms = new HashMap<>();  // progress, input(true), companion
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

        public void tickPre1() {
            List<Schedule.Node> ts = schedule.getTasks(processor);
            if (ts.isEmpty()) return;
            Schedule.Node node = ts.get(0);
            for (Comm c : node.comms) if (!attempt(c) && !getCluster().policy.multiple) return;
        }

        public void tickPre2() {
            List<Schedule.Node> ts = schedule.getTasks(processor);
            if (ts.isEmpty()) return;
            Schedule.Node node = ts.get(0);
            for (Comm c : node.comms) if (!ready(c)) return;
            if (available(this)) {
                working = node.task;
                executing.put(working, processor);
                schedule.remove(processor, 0);
            }
        }

        private boolean ready(Comm c) {
            Processor src = output.get(c);
            if (cluster.comm == 0 && src != null) return true;
            return input.contains(c) || src == processor;
        }

        private boolean attempt(Comm c) {
            Processor src = output.get(c);
            if (ready(c)) return true;
            else if (!comms.containsKey(c)) {
                if (src == null) return false;
                Status s = processors.get(src);
                if (available(s, this)) {
                    s.comms.put(c, new Trio<>(.0, false, processor));
                    comms.put(c, new Trio<>(.0, true, s.processor));
                }
            }
            return false;
        }

        public boolean tickPost() {
            boolean ret = false;
            if (working != null) {
                ret = true;
                progress += SPEED / 4 / working.time * processor.getSpeed() * processor.getSpeedup(working.type);
                if (progress + 1e-8 > 1) {
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
            Iterator<Map.Entry<Comm, Trio<Double, Boolean, Processor>>> it = comms.entrySet().iterator();
            while (it.hasNext()) {
                ret = true;
                Map.Entry<Comm, Trio<Double, Boolean, Processor>> i = it.next();
                double p = i.getValue().a + SPEED / 4 / i.getKey().size * cluster.comm;
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

        @Nullable
        public Task getWorking() {
            return working;
        }

        public double getProgress() {
            return progress;
        }

        public Map<Comm, Trio<Double, Boolean, Processor>> getComms() {
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
