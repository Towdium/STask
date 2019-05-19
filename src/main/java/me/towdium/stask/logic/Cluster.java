package me.towdium.stask.logic;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
public class Cluster {
    Map<String, Processor> processors = new HashMap<>();
    int comm;

    public Cluster(Pojo.Cluster pojo) {
        comm = pojo.comm;
        pojo.processors.forEach((s, p) -> {
            Processor tmp = new Processor();
            tmp.color = p.color;
            tmp.speed = p.speed;
            tmp.speedup = p.speedup;
            processors.put(s, tmp);
        });
    }

    public class Processor {
        int color;
        float speed;
        Map<String, Float> speedup;

        public int cost(Graph.Task task) {
            float ret = task.time * speed;
            Float k = speedup.get(task.type);
            if (k != null) ret *= k;
            return (int) Math.ceil(ret);
        }

        public int getColor() {
            return color;
        }

        public float getSpeed() {
            return speed;
        }

        public Map<String, Float> getSpeedup() {
            return speedup;
        }
    }
}
