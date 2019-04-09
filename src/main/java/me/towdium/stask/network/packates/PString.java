package me.towdium.stask.network.packates;

import io.netty.buffer.ByteBuf;
import me.towdium.stask.network.Client;
import me.towdium.stask.network.Packet;
import me.towdium.stask.network.Server;

/**
 * Author: Towdium
 * Date: 07/04/19
 */
public class PString extends Packet {
    public static final String IDENTIFIER = "string";

    String str;

    public PString(ByteBuf buf) {
        str = readString(buf);
    }

    public PString(String s) {
        this.str = s;
    }

    @Override
    public void serialize(ByteBuf b) {
        writeString(str, b);
    }

    @Override
    public void handle(Server.Context c) {
        System.out.println("Server received: " + str);
    }

    @Override
    public void handle(Client.Context c) {
        System.out.println("Client received: " + str);
    }

    @Override
    public String identifier() {
        return IDENTIFIER;
    }
}