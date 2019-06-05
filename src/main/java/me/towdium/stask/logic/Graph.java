package me.towdium.stask.logic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Author: Towdium
 * Date: 18/05/19
 */
@ParametersAreNonnullByDefault
public class Graph {
    Map<String, Task> tasks = new HashMap<>();
    Task root;
    List<List<Task>> layout = new ArrayList<>();

    public Graph(Pojo.Graph pojo) {
        pojo.tasks.forEach((s, t) -> {
            Task tmp = new Task();
            tmp.time = t.time;
            tmp.type = t.type;
            tasks.put(s, tmp);
        });
        pojo.tasks.forEach((s, t) -> {
            Task crr = tasks.get(s);
            t.after.forEach((i, j) -> {
                Task pre = tasks.get(i);
                Comm c = new Comm(pre, crr, j);
                crr.after.put(pre, c);
                pre.before.put(crr, c);
            });
        });
        pojo.layout.forEach(i -> {
            List<Task> tmp = new ArrayList<>();
            i.forEach(j -> tmp.add(j == null ? null : tasks.get(j)));
            layout.add(Collections.unmodifiableList(tmp));
        });

        int countA = 0;
        int countB = 0;
        for (Task t : tasks.values()) {
            if (t.before.isEmpty()) countB++;
            if (t.after.isEmpty()) {
                root = t;
                countA++;
            }
            if (countB > 1) throw new RuntimeException("Multiple end points.");
            if (countA > 1) throw new RuntimeException("Multiple start points.");
        }
    }

    public List<List<Task>> getLayout() {
        return Collections.unmodifiableList(layout);
    }

    public Task getRoot() {
        return root;
    }

    public Task getTask(String id) {
        return tasks.get(id);
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

    public static class Task extends Work {
        Map<Task, Comm> after = new IdentityHashMap<>();
        Map<Task, Comm> before = new IdentityHashMap<>();
        int time;
        String type;

        public int getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public Map<Task, Comm> getAfter() {
            return Collections.unmodifiableMap(after);
        }

        public Map<Task, Comm> getBefore() {
            return Collections.unmodifiableMap(before);
        }
    }
}
