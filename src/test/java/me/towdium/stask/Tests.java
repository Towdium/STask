package me.towdium.stask;

import com.google.gson.Gson;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Pojo;
import org.junit.jupiter.api.Test;

import javax.annotation.ParametersAreNonnullByDefault;

import static me.towdium.stask.Demo.CLUSTER;
import static me.towdium.stask.Demo.GRAPH;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
@ParametersAreNonnullByDefault
public class Tests {

    @Test
    public void test() {
        Gson gson = new Gson();
        Graph graph = new Graph(gson.fromJson(GRAPH, Pojo.Graph.class));
        Cluster cluster = new Cluster(gson.fromJson(CLUSTER, Pojo.Cluster.class));
        int i = 0;
    }
}
