package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.widgets.WArea;
import me.towdium.stask.client.widgets.WButtonText;
import me.towdium.stask.client.widgets.WPanel;
import me.towdium.stask.client.widgets.WText;
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
public class PSingle extends Page.Impl {
    PWrapper root;
    Page parent;
    Map<WArea, String> desc = new HashMap<>();
    WText text;

    public PSingle(PWrapper r, Page p) {
        root = r;
        parent = p;
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        desc.clear();
        put(new WPanel(300, y + 20), -10, -10);
        for (int i = 0; i < Levels.sections.size(); i++) {
            Levels.Section s = Levels.sections.get(i);
            WArea w = new WButtonText(250, 40, "Section " + (i + 1)).setListener(j ->
                    root.display(() -> new PSection(root, this, s)));
            put(w, 20, 20 + 60 * i);
            desc.put(w, s.desc);
        }
        put(new WButtonText(250, 40, "back").setListener(i -> root.display(() -> parent)), 20, y - 60);
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
