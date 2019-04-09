package me.towdium.stask.network.packates;

import io.netty.buffer.ByteBuf;
import me.towdium.stask.network.Client;
import me.towdium.stask.network.Packet;
import me.towdium.stask.network.Server;
import me.towdium.stask.utils.Log;

/**
 * Author: Towdium
 * Date: 09/04/19
 */
public class PConnect extends Packet {
    public static final String IDENTIFIER = "connect";
    int index;

    public PConnect(int index) {
        this.index = index;
    }

    public PConnect() {
        this(-1);
    }

    public PConnect(ByteBuf buf) {
        index = buf.readInt();
    }

    @Override
    public void serialize(ByteBuf b) {
        b.writeInt(index);
    }

    @Override
    public void handle(Server.Context c) {
        int index = c.connect();
        c.reply(new PConnect(index));
    }

    @Override
    public void handle(Client.Context c) {
        c.connect(index);
        Log.network.info("Connected as client " + index);
    }

    @Override
    public String identifier() {
        return IDENTIFIER;
    }
}
