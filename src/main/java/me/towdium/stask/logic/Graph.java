package me.towdium.stask.logic;

import com.google.gson.Gson;
import me.towdium.stask.utils.Utilities;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Author: Towdium
 * Date: 18/05/19
 */
@ParametersAreNonnullByDefault
public class Graph {
    Map<String, Task> tasks = new HashMap<>();
    List<List<Task>> layout = new ArrayList<>();
    List<Task> entries = new ArrayList<>();
    List<Task> exits = new ArrayList<>();

    public static String[] list() {
        return Arrays.stream(Utilities.list("/graphs/"))
                .map(i -> i.substring(0, i.length() - 5)).toArray(String[]::new);
    }

    public Graph(String id) {
        String json = Utilities.readString("/graphs/" + id + ".json");
        Gson gson = new Gson();
        Pojo.Graph pojo = gson.fromJson(json, Pojo.Graph.class);
        pojo.tasks.forEach((s, t) -> {
            Task tmp = new Task();
            tmp.time = t.time;
            tmp.type = t.type;
            tmp.name = s;
            tasks.put(s, tmp);
        });
        pojo.tasks.forEach((s, t) -> {
            Task crr = tasks.get(s);
            t.after.forEach((i, j) -> {
                Task pre = tasks.get(i);
                if (pre == null) throw new IllegalArgumentException("Task dependency not found: " + i);
                Comm c = new Comm(pre, crr, j);
                crr.predecessor.put(pre, c);
                pre.successor.put(crr, c);
            });
        });
        pojo.layout.forEach(i -> {
            List<Task> tmp = new ArrayList<>();
            i.forEach(j -> tmp.add(j == null ? null : tasks.get(j)));
            layout.add(tmp);
        });
        for (Task t : tasks.values()) {
            if (t.successor.isEmpty()) exits.add(t);
            if (t.predecessor.isEmpty()) entries.add(t);
        }
    }

    public List<List<Task>> getLayout() {
        return Collections.unmodifiableList(layout);
    }

    public Task getTask(String id) {
        return tasks.get(id);
    }

    public Collection<Task> getTasks() {
        return tasks.values();
    }

    public static class Work {
    }

    public static class Comm extends Work {
        Task src, dst;
        int size;

        public Comm(Task src, Task dst, int size) {
            this.src = src;
            this.dst = dst;
            this.size = size;
        }

        public Task getSrc() {
            return src;
        }

        public Task getDst() {
            return dst;
        }

        public int getSize() {
            return size;
        }
    }

    public class Task extends Work {
        Map<Task, Comm> predecessor = new LinkedHashMap<>();
        Map<Task, Comm> successor = new LinkedHashMap<>();
        String name;
        int time;
        String type;

        public Graph getGraph() {
            return Graph.this;
        }

        public String getName() {
            return name;
        }

        public int getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public Map<Task, Comm> getPredecessor() {
            return Collections.unmodifiableMap(predecessor);
        }

        public Map<Task, Comm> getSuccessor() {
            return Collections.unmodifiableMap(successor);
        }
    }
}
