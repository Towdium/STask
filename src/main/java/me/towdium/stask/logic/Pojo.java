package me.towdium.stask.logic;

import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 18/05/19
 */
public class Pojo {
    public static class Graph {
        Map<String, Task> tasks;
        List<List<String>> layout;
    }

    public static class Task {
        int in, out, time;
        String type;
        Map<String, Integer> after;
    }

    public static class Cluster {
        Map<String, Processor> processors;
        int comm;
    }

    public static class Processor {
        int color;
        float speed;
        Map<String, Float> speedup;
    }
}
