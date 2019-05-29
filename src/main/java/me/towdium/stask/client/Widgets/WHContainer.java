package me.towdium.stask.client.Widgets;

import me.towdium.stask.logic.Graph;
import me.towdium.stask.utils.wrap.Wrapper;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 28/05/19
 */
public class WHContainer extends WContainer<WHighlight> implements WHighlight {
    @Override
    public boolean onHighlight(Vector2i mouse) {
        Wrapper<Graph.Task> ret = new Wrapper<>();
        return widgets.forward((w, v) -> w.onHighlight(mouse));
    }
}
