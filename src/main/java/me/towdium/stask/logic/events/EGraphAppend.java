package me.towdium.stask.logic.events;

import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Graph;

/**
 * Author: Towdium
 * Date: 29/06/19
 */
public class EGraphAppend extends Event {
    public Graph graph;

    public EGraphAppend(Graph graph) {
        this.graph = graph;
    }
}
