package me.towdium.stask;

import com.google.gson.Gson;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Widgets.WGraph;
import me.towdium.stask.client.Window;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Pojo;
import org.intellij.lang.annotations.Language;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
public class Graf {
    @Language("JSON")
    static final String JSON = "{\n" +
            "  \"tasks\": {\n" +
            "    \"a\": {\n" +
            "      \"in\": 5,\n" +
            "      \"out\": 10,\n" +
            "      \"time\": 1,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {}\n" +
            "    },\n" +
            "    \"b\": {\n" +
            "      \"in\": 2,\n" +
            "      \"out\": 2,\n" +
            "      \"time\": 4,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\"a\": 2}\n" +
            "    },\n" +
            "    \"c\": {\n" +
            "      \"in\": 8,\n" +
            "      \"out\": 4,\n" +
            "      \"time\": 1,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\"a\": 8}\n" +
            "    },\n" +
            "    \"d\": {\n" +
            "      \"in\": 6,\n" +
            "      \"out\": 2,\n" +
            "      \"time\": 2,\n" +
            "      \"type\": \"\\u03b1\",\n" +
            "      \"after\": {\n" +
            "        \"b\": 2,\n" +
            "        \"c\": 8\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"layout\": [\n" +
            "    [\"a\"], [\"b\", \"c\"], [\"d\"]\n" +
            "  ]\n" +
            "}";

    public static void main(String[] args) {
        Page.Simple root = new Page.Simple();
        Pojo.Graph pojo = new Gson().fromJson(JSON, Pojo.Graph.class);
        Graph graph = new Graph(pojo);
        root.put(new WGraph(300, 300, graph), 0, 0);

        try (Window w = new Window("Test Graph", root)) {
            w.display();
            while (!w.isFinished()) w.tick();
        }
    }
}
