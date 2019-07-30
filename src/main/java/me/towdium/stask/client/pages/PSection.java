package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.widgets.WArea;
import me.towdium.stask.client.widgets.WButtonText;
import me.towdium.stask.client.widgets.WPanel;
import me.towdium.stask.client.widgets.WText;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Levels;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public class PSection extends Page.Impl {
    Levels.Section section;
    PWrapper root;
    Page parent;
    Map<WArea, String> desc = new HashMap<>();
    WText text;

    public PSection(PWrapper r, Page p, Levels.Section s) {
        section = s;
        root = r;
        parent = p;
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        desc.clear();
        put(new WPanel(300, y + 20), -10, -10);
        for (int i = 0; i < section.levels.size(); i++) {
            Game g = new Game(section.levels.get(i));
            WArea w = new WButtonText(250, 40, "Level " + (i + 1)).setListener(j ->
                    root.display(() -> new PGame(root, this, g)));
            put(w, 20, 20 + 60 * i);
            desc.put(w, g.getDesc());
        }
        put(new WButtonText(250, 40, "Back").setListener(i -> root.display(() -> parent)), 20, y - 60);
        put(text = new WText(0xFFFFFF, "", x - 400), 350, 50);
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        super.onRefresh(mouse);
        text.str = desc.entrySet().stream()
                .filter(i -> i.getKey().onTest(mouse.sub(find(i.getKey()), new Vector2i())))
                .map(Map.Entry::getValue)
                .findAny().orElse("");
    }
}
