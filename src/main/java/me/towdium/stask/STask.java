package me.towdium.stask;

import me.towdium.stask.gui.Widgets.WContainer;
import me.towdium.stask.gui.Window;
import me.towdium.stask.network.Client;
import me.towdium.stask.network.Discover;
import me.towdium.stask.network.Packet;
import me.towdium.stask.network.Server;
import me.towdium.stask.network.packates.PConnect;
import me.towdium.stask.network.packates.PString;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class STask extends Client {
    public STask() {
        tunnel.send(new PString("Hello"));
        Window.display(new WContainer());
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        Packet.Registry.register(PString.IDENTIFIER, PString::new);
        Packet.Registry.register(PConnect.IDENTIFIER, PConnect::new);

        try (Discover d = new Discover();
             Server s = new Server();
             STask c = new STask()) {
            new Thread(s).start();
            c.run();
        }
    }

    @Override
    protected void tick() {
        super.tick();
        Window.tick();
        if (Window.finished()) close();
    }

    @Override
    public synchronized void close() {
        super.close();
        Window.destroy();
    }
}
