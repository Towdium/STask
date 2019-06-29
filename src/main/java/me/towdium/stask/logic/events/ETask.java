package me.towdium.stask.logic.events;

import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Graph;

/**
 * Author: Towdium
 * Date: 29/06/19
 */
public class ETask extends Event {
    Graph.Task task;
    Cluster.Processor processor;
    Operation operation;

    public ETask(Graph.Task task, Cluster.Processor processor, Operation operation) {
        this.task = task;
        this.processor = processor;
        this.operation = operation;
    }

    public enum Operation {CANCELED, SCHEDULED, EXECUTED, COMPLETED}
}
