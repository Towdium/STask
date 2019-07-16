package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Towdium
 * Date: 15/07/19
 */
public class WTutorial extends WContainer {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;

    List<Widget> widgets = new ArrayList<>();
    WButton left, right;
    int index = -1;

    public WTutorial() {
        put(new WPanel(WIDTH, HEIGHT), 0, 0);
        put(right = new WButtonText(40, 30, ">"), 340, 360);
        put(left = new WButtonText(40, 30, "<"), 20, 360);
    }

    public void update(Widget w, boolean change) {
        widgets.add(w);
        if (change) display(widgets.size() - 1);
        else refresh();
    }

    private void display(int i) {
        if (index != -1) remove(widgets.get(index));
        put(widgets.get(i), 0, 0);
        index = i;
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
