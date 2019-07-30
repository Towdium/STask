package me.towdium.stask.logic.algorithms;

import me.towdium.stask.logic.Algorithm;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.utils.wrap.Wrapper;

/**
 * Author: Towdium
 * Date: 02/07/19
 * Highest Level First with Estimated Times
 *
 * when start == true and comm == false
 * it works the same as standard algorithm
 */
public class AListHLFET extends AList {
    public AListHLFET(boolean start, boolean comm) {
        super(start, comm);
    }

    @Override
    public int priority(Graph.Task t) {
        Wrapper<Integer> max = new Wrapper<>(0);
        Algorithm.traverseSuccessors(t, i -> {
            int length = i.stream().mapToInt(Graph.Task::getTime).sum();
            max.v = Math.max(max.v, length);
        });
        return max.v;
    }
}
