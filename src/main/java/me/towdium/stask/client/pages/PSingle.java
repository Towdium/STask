package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Widgets.WButtonText;
import me.towdium.stask.client.Widgets.WPanel;
import me.towdium.stask.logic.Levels;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
public class PSingle extends Page.Impl {
    PWrapper root;
    Page parent;

    public PSingle(PWrapper r, Page p) {
        root = r;
        parent = p;
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        put(new WPanel(300, y + 20), -10, -10);
        for (int i = 0; i < Levels.sections.size(); i++) {
            int n = i;
            put(new WButtonText(250, 40, "Section " + (i + 1)).setListener(j -> root.display(() ->
                    new PSection(root, this, Levels.sections.get(n)))), 20, 20 + 60 * i);
        }
        put(new WButtonText(250, 40, "back").setListener(i -> root.display(() -> parent)), 20, y - 60);
    }
}
