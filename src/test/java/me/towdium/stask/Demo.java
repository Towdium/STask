package me.towdium.stask;

import com.google.gson.Gson;
import me.towdium.stask.client.Widgets.PGame;
import me.towdium.stask.client.Window;
import me.towdium.stask.logic.*;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.time.Timer;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
@ParametersAreNonnullByDefault
public class Demo {
    public static final String GRAPH = "{\n" +
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

    public static final String CLUSTER = "{\n" +
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
        Gson gson = new Gson();
        Graph graph = new Graph(gson.fromJson(GRAPH, Pojo.Graph.class));
        Cluster cluster = new Cluster(gson.fromJson(CLUSTER, Pojo.Cluster.class));
        Allocation allocation = new Allocation();
        Game game = new Game(cluster, graph, allocation, new Game.Policy());
        Log.client.setLevel(Log.Priority.TRACE);
        Timer timer = new Timer(1 / 20f, i -> game.tick());


        try (Window w = new Window("Test Graph", new PGame(game))) {
            w.display();
            while (!w.isFinished()) {
                w.tick();
                timer.tick();
            }
        }
    }
}
