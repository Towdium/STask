package me.towdium.stask.logic;

import java.util.*;

/**
 * Author: Towdium
 * Date: 18/05/19
 */
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
                crr.after.put(pre, j);
                pre.before.put(crr, j);
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

    public static class Task {
        Map<Task, Integer> after = new IdentityHashMap<>();
        Map<Task, Integer> before = new IdentityHashMap<>();
        int time;
        String type;

        public int getTime() {
            return time;
        }

        public String getType() {
            return type;
        }

        public Map<Task, Integer> getAfter() {
            return Collections.unmodifiableMap(after);
        }

        public Map<Task, Integer> getBefore() {
            return Collections.unmodifiableMap(before);
        }
    }
}
