package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Widget;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public class WCompose extends WContainer implements WOwner {
    List<WOwner> owners = new ArrayList<>();
    List<Widget> pinned = new ArrayList<>();
    boolean building = true;

    public WCompose compose(Widget w) {
        if (!building) throw new IllegalStateException("Not building");
        pinned.add(w);
        if (w instanceof WOwner) {
            WOwner o = (WOwner) w;
            owners.add(o);
            o.onTransfer(this);
        }
        super.put(w, 0, 0);
        return this;
    }

    @Override
    public WContainer put(Widget widget, int x, int y) {
        building = false;
        return super.put(widget, x, y);
    }

    @Override
    public void onTransfer(WOwner to) {
        for (WOwner i : owners) i.onTransfer(to);
    }

    @Override
    public WContainer clear() {
        WContainer ret = super.clear();
        for (Widget i : pinned) super.put(i, 0, 0);
        return ret;
    }
}
