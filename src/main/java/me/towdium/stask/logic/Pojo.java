package me.towdium.stask.logic;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 18/05/19
 */
@ParametersAreNonnullByDefault
public class Pojo {
    public static class Graph {
        Map<String, Task> tasks;
        List<List<String>> layout;
    }

    public static class Task {
        int time;
        String type;
        Map<String, Integer> after;
    }

    public static class Cluster {
        Map<String, Processor> processors;
        List<String> layout;
        int comm;
    }

    public static class Processor {
        float speed;
        Map<String, Float> speedup;
    }
}
