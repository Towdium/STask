package me.towdium.stask;

import com.google.gson.Gson;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Widgets.WGraph;
import me.towdium.stask.client.Widgets.WSchedule;
import me.towdium.stask.client.Window;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Pojo;
import me.towdium.stask.logic.Schedule;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
@ParametersAreNonnullByDefault
public class Graf {
    static final String GRAPH = "{\n" +
            "  \"tasks\": {\n" +
            "    \"a\": {\n" +
            "      \"time\": 1,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {}\n" +
            "    },\n" +
            "    \"b\": {\n" +
            "      \"time\": 4,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\"a\": 2}\n" +
            "    },\n" +
            "    \"c\": {\n" +
            "      \"time\": 1,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\"a\": 8}\n" +
            "    },\n" +
            "    \"d\": {\n" +
            "      \"time\": 2,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\n" +
            "        \"b\": 2,\n" +
            "        \"c\": 8\n" +
            "      }\n" +
            "    },\n" +
            "    \"e\": {\n" +
            "      \"time\": 2,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\n" +
            "        \"b\": 3\n" +
            "      }\n" +
            "    },\n" +
            "    \"f\": {\n" +
            "      \"time\": 2,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\n" +
            "        \"d\": 3,\n" +
            "        \"e\": 1\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"layout\": [\n" +
            "    [\"a\"], [\"b\", \"c\"], [\"d\", \"e\"], [\"f\"]\n" +
            "  ]\n" +
            "}";

    static final String CLUSTER = "{\n" +
            "  \"comm\": 1,\n" +
            "  \"processors\": {\n" +
            "    \"a\": {\n" +
            "      \"speed\": 1,\n" +
            "      \"speedup\": {}\n" +
            "    },\n" +
            "    \"b\": {\n" +
            "      \"speed\": 1,\n" +
            "      \"speedup\": {}\n" +
            "    }\n" +
            "  },\n" +
            "  \"layout\": [\"a\", \"b\"]\n" +
            "}";


    public static void main(String[] args) {
        Page.Simple root = new Page.Simple();
        Gson gson = new Gson();
        Graph graph = new Graph(gson.fromJson(GRAPH, Pojo.Graph.class));
        Cluster cluster = new Cluster(gson.fromJson(CLUSTER, Pojo.Cluster.class));
        Schedule schedule = new Schedule();


        root.put(new WGraph(300, 300, graph, schedule), 0, 0);
        root.put(new WSchedule(300, 100, schedule, cluster), 0, 300);

        try (Window w = new Window("Test Graph", root)) {
            w.display();
            while (!w.isFinished()) w.tick();
        }
    }
}
