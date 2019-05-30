package me.towdium.stask.logic;

import java.util.*;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
public class Cluster {
    Map<String, Processor> processors = new HashMap<>();
    List<Processor> layout = new ArrayList<>();

    int comm;

    public Cluster(Pojo.Cluster pojo) {
        comm = pojo.comm;
        pojo.processors.forEach((s, p) -> {
            Processor tmp = new Processor();
            tmp.speed = p.speed;
            tmp.speedup = p.speedup;
            tmp.name = s;
            processors.put(s, tmp);
        });
        pojo.layout.forEach(s -> layout.add(processors.get(s)));
    }

    public Map<String, Processor> getProcessors() {
        return Collections.unmodifiableMap(processors);
    }

    public List<Processor> getLayout() {
        return Collections.unmodifiableList(layout);
    }

    public class Processor {
        String name;
        float speed;
        Map<String, Float> speedup;

        public int cost(Graph.Task task) {
            float ret = task.time * speed;
            Float k = speedup.get(task.type);
            if (k != null) ret *= k;
            return (int) Math.ceil(ret);
        }

        public String getName() {
            return name;
        }

        public float getSpeed() {
            return speed;
        }

        public Map<String, Float> getSpeedup() {
            return speedup;
        }
    }
}
