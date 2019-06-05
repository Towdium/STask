package me.towdium.stask;

import com.google.gson.Gson;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Pojo;
import me.towdium.stask.logic.Schedule;
import org.junit.jupiter.api.Test;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

import static me.towdium.stask.Graf.CLUSTER;
import static me.towdium.stask.Graf.GRAPH;

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
        Schedule schedule = new Schedule();

        schedule.assign(graph.getTask("a"), cluster.getProcessor("a"));
        Schedule.TimeAxis ta = schedule.attempt(graph.getTask("b"), cluster.getProcessor("b"));
        Integer f = ta.earliest(2);
        Objects.requireNonNull(f);
        List<Schedule.Assignment> as = schedule.assign(graph.getTask("b"), cluster.getProcessor("b"), f);
        int i = 0;
    }
}
