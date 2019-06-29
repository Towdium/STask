package me.towdium.stask.logic.events;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Graph;

/**
 * Author: Towdium
 * Date: 29/06/19
 */
public class EGraphComplete extends Event {
    public Graph graph;

    public EGraphComplete(Graph g) {
        graph = g;
    }
}
