package me.towdium.stask;

import me.towdium.stask.client.Widgets.WContainer;
import me.towdium.stask.client.Window;
import me.towdium.stask.network.Network;
import me.towdium.stask.network.Packet;
import me.towdium.stask.network.packates.PConnect;
import me.towdium.stask.network.packates.PString;
import me.towdium.stask.utils.time.Ticker;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class STask {

    public static void main(String[] args) {
        Packet.Registry.register(PString.IDENTIFIER, PString::new);
        Packet.Registry.register(PConnect.IDENTIFIER, PConnect::new);

        Ticker ticker = new Ticker(1 / 200f);
        try (Window w = new Window("STask", 800, 600, new WContainer());
             Network n = new Network()) {
            n.getClient().send(new PString("Hello"));
            w.display();
            while (w.isFinished()) {
                n.getClient().tick();
                n.getServer().tick();
                w.tick();
                ticker.sync();
            }
        }

    }
}
