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
        Policy policy;
    }

    public static class Policy {
        boolean multiple, immediate, background;
    }

    public static class Processor {
        float speed;
        Map<String, Float> speedup;
    }

    public static class Game {
        String cluster, tutorial;
        List<Integer> times;
        List<String> graphs;
    }

    public static class Levels {
        List<Section> sections;
    }

    public static class Section {
        List<String> levels;
    }
}
