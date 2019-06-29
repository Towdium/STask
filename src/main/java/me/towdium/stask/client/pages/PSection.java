package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Widgets.WButtonText;
import me.towdium.stask.client.Widgets.WPanel;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Levels;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
public class PSection extends Page.Impl {
    Levels.Section section;
    PWrapper root;
    Page parent;

    public PSection(PWrapper r, Page p, Levels.Section s) {
        section = s;
        root = r;
        parent = p;
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        put(new WPanel(300, y + 20), -10, -10);
        for (int i = 0; i < section.levels.size(); i++) {
            int n = i;
            put(new WButtonText(250, 40, "Level " + (i + 1)).setListener(j -> root.display(() ->
                    new PGame(root, this, new Game(section.levels.get(n))))), 20, 20 + 60 * i);
        }
        put(new WButtonText(250, 40, "back").setListener(i -> root.display(() -> parent)), 20, y - 60);
    }
}
