package me.towdium.stask;

import com.google.gson.Gson;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Pojo;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
public class Tests {
    @Test
    public void test() {
        @Language("JSON")
        String json = "{\n" +
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
                "  }\n" +
                "}";
        Gson gson = new Gson();
        Pojo.Graph pojo = gson.fromJson(json, Pojo.Graph.class);
        Graph graph = new Graph(pojo);
    }
}
