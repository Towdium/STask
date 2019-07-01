package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Widget;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
public interface WOwner extends Widget {
    void onTransfer(WOwner to);
}
