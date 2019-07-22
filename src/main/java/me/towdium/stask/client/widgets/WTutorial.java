package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Resource;
import me.towdium.stask.client.Widget;
import me.towdium.stask.logic.Event;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 15/07/19
 */
@ParametersAreNonnullByDefault
public class WTutorial extends WContainer {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 420;

    List<Widget> widgets = new ArrayList<>();
    Map<Integer, String> ids = new HashMap<>();
    WButton left, right;
    int index = -1;

    public WTutorial() {
        put(new WPanel(WIDTH, HEIGHT), 0, 0);
        put(right = new WButtonIcon(30, 30, Resource.RIGHT, "Next"), 360, 380);
        put(left = new WButtonIcon(30, 30, Resource.LEFT, "Previous"), 10, 380);
    }

    public void update(Widget w, boolean change) {
        update(w, change, null);
    }

    public void update(Widget w, boolean change, @Nullable String id) {
        widgets.add(w);
        int idx = widgets.size() - 1;
        if (id != null) ids.put(idx, id);
        if (change) display(idx);
        else refresh();
    }

    public void update(String s, boolean change) {
        update(s, change, null);
    }

    public void update(String s, boolean change, @Nullable String id) {
        Widget w = (p, m) -> p.drawTextWrapped(s, 10, 10 + Painter.fontAscent, WTutorial.WIDTH - 20);
        update(w, change, id);
    }

    private void display(int i) {
        if (index != -1) remove(widgets.get(index));
        put(widgets.get(i), 0, 0);
        index = i;
        String id = ids.remove(i);
        if (id != null) BUS.post(new Event.ETutorial(id));
        refresh();
    }

    private void refresh() {
        if (index == -1) {
            left.setListener(null);
            right.setListener(null);
            return;
        }
        right.setListener(index + 1 < widgets.size() ? w -> display(index + 1) : null);
        left.setListener(index > 0 ? w -> display(index - 1) : null);
    }
}
