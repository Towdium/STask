package me.towdium.stask.logic;

import com.google.gson.Gson;
import me.towdium.stask.utils.Utilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
@ParametersAreNonnullByDefault
public class Cluster {
    Map<String, Processor> processors = new HashMap<>();
    List<Processor> layout = new ArrayList<>();
    Policy policy;
    public static final float MULTIPLIER = 100f;

    int comm;

    public static List<String> list() {
        return Arrays.stream(Utilities.list("/clusters/"))
                .map(s -> s.substring(0, s.length() - 5))
                .collect(Collectors.toList());
    }

    public Cluster(String id) {
        String json = Utilities.readString("/clusters/" + id + ".json");
        Gson gson = new Gson();
        Pojo.Cluster pojo = gson.fromJson(json, Pojo.Cluster.class);
        if (pojo.policy == null) policy = null;
        else {
            policy = new Policy(pojo.policy.multiple, pojo.policy.background);
            if (policy.multiple && !policy.background)
                throw new IllegalArgumentException("Multiple not allowed when not background");
        }
        comm = pojo.comm;
        pojo.processors.forEach((s, p) -> {
            Processor tmp = new Processor();
            tmp.speed = p.speed;
            tmp.speedup = p.speedup == null ? new HashMap<>() : p.speedup;
            tmp.name = s;
            processors.put(s, tmp);
        });
        pojo.layout.forEach(s -> {
            Processor p = processors.get(s);
            Objects.requireNonNull(p, "Invalid processor in layout: " + s);
            layout.add(p);
        });
    }

    public int getComm() {
        return comm;
    }

    public Policy getPolicy() {
        return policy;
    }

    public Map<String, Processor> getProcessors() {
        return Collections.unmodifiableMap(processors);
    }

    public Processor getProcessor(String id) {
        return processors.get(id);
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
            return (int) Math.ceil(ret * MULTIPLIER);
        }

        public int comm(int data, Processor p) {
            if (comm == 0 || p == this) return 0;
            else return (int) Math.ceil(data / (float) comm * MULTIPLIER);
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

        public float getSpeedup(String type) {
            Float ret = speedup.get(type);
            return ret == null ? 1 : ret;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Policy {
        public final boolean multiple;
        public final boolean background;

        public Policy(boolean multiple, boolean background) {
            this.multiple = multiple;
            this.background = background;
        }
    }
}
