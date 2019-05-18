package me.towdium.stask.logic;

import java.util.Map;

/**
 * Author: Towdium
 * Date: 18/05/19
 */
public class Pojo {
    public static class Graph {
        Map<String, Task> tasks;
    }

    public static class Task {
        int in, out, time;
        String type;
        Map<String, Integer> after;
    }

    public static class Cluster {
        Map<String, Processor> processors;
    }

    public static class Processor {
        int color;
        Map<String, Float> speedup;
    }
}
