package me.towdium.stask.logic;

import com.google.gson.annotations.SerializedName;

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

    public enum Model {
        @SerializedName(value = "IC", alternate = {"ic"})
        IC,
        @SerializedName(value = "BCMC", alternate = {"bcmc"})
        BCMC,
        @SerializedName(value = "BCSC", alternate = {"bcsc"})
        BCSC,
        @SerializedName(value = "SC", alternate = {"sc"})
        SC
    }

    public static class Cluster {
        Map<String, Processor> processors;
        List<String> layout;
        int comm;
        Model model;
    }

    public static class Processor {
        float speed;
        Map<String, Float> speedup;
    }

    public static class Game {
        String cluster, tutorial;
        List<Integer> times;
        List<String> graphs;
        List<Integer> aims;
        String desc;
    }

    public static class Levels {
        List<Section> sections;
    }

    public static class Section {
        List<String> levels;
        String desc;
    }
}
