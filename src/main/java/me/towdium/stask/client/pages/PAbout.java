package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.widgets.WButton;
import me.towdium.stask.client.widgets.WButtonText;
import me.towdium.stask.client.widgets.WPanel;
import me.towdium.stask.client.widgets.WText;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 29/07/2019
 */
@ParametersAreNonnullByDefault
public class PAbout extends Page.Impl {
    static final String TEXT = "STask v0.0.1 by Juntong Liu, 2019\n\n" +
            "Msc project at University of Edinburgh\n\n" +
            "Supervised by Prof. Murray Cole";

    PWrapper root;
    Page parent;
    WButton back = new WButtonText(250, 40, "Back").setListener(i -> root.display(() -> parent));

    public PAbout(PWrapper root, Page parent) {
        this.root = root;
        this.parent = parent;
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        put(new WPanel(300, y + 20), -10, -10);
        put(new WText(0xFFFFFF, TEXT, x - 400), 350, 50);
        put(back, 20, y - 60);
    }
}
