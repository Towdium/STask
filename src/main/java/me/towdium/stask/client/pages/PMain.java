package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.widgets.WButton;
import me.towdium.stask.client.widgets.WButtonText;
import me.towdium.stask.client.widgets.WPanel;
import me.towdium.stask.client.Window;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
public class PMain extends Page.Impl {
    PWrapper root;
    Window window;
    WButton single = new WButtonText(250, 40, "Singleplayer").setListener(i ->
            root.display(() -> new PSingle(root, this)));
    WButton multi = new WButtonText(250, 40, "Multiplayer");
    WButton sandbox = new WButtonText(250, 40, "Sandbox");
    WButton about = new WButtonText(250, 40, "About");
    WButton exit = new WButtonText(250, 40, "Exit").setListener(i -> window.terminate());

    public PMain(PWrapper r, Window w) {
        root = r;
        window = w;
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        put(new WPanel(300, y + 20), -10, -10);
        put(single, 20, 20);
        put(multi, 20, 80);
        put(sandbox, 20, 140);
        put(about, 20, y - 120);
        put(exit, 20, y - 60);
    }
}
