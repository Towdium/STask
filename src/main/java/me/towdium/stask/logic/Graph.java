package me.towdium.stask.logic;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 18/05/19
 */
public class Graph {
    Map<String, Task> tasks = new HashMap<>();
    Task root;

    public Graph(Pojo.Graph pojo) {
        pojo.tasks.forEach((s, t) -> {
            Task tmp = new Task();
            tmp.in = t.in;
            tmp.out = t.out;
            tmp.time = t.time;
            tmp.type = t.type;
        });
        pojo.tasks.forEach((s, t) -> {
            Task crr = tasks.get(s);
            t.after.forEach((i, j) -> {
                Task pre = tasks.get(i);
                crr.after.put(pre, j);
                pre.before.put(crr, j);
            });
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

    public static class Task {
        Map<Task, Integer> after = new IdentityHashMap<>();
        Map<Task, Integer> before = new IdentityHashMap<>();
        int in, out, time;
        String type;
    }
}
