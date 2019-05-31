package me.towdium.stask;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Window;
import me.towdium.stask.network.Network;
import me.towdium.stask.network.Packet;
import me.towdium.stask.network.packates.PConnect;
import me.towdium.stask.network.packates.PString;
import me.towdium.stask.utils.time.Timer;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
public class STask {

    public static void main(String[] args) {
        Packet.Registry.register(PString.IDENTIFIER, PString::new);
        Packet.Registry.register(PConnect.IDENTIFIER, PConnect::new);

        Timer ticker = new Timer(1 / 200f);
        try (Window w = new Window("STask", new Page.Simple());
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
