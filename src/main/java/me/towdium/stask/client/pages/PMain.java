package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Window;
import me.towdium.stask.client.widgets.WButton;
import me.towdium.stask.client.widgets.WButtonText;
import me.towdium.stask.client.widgets.WPanel;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
public class PMain extends Page.Impl {
    PWrapper root;
    Window window;
    WButton single = new WButtonText(250, 40, "Singleplayer").setListener(i ->
            root.display(() -> new PSingle(root, this)));
    WButton testing = new WButtonText(250, 40, "Testing").setListener(i ->
            root.display(() -> new PTest(root, this)));
    WButton about = new WButtonText(250, 40, "About").setListener(i ->
            root.display(() -> new PAbout(root, this)));
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
        put(testing, 20, 80);
        put(about, 20, y - 120);
        put(exit, 20, y - 60);
    }
}
