package me.towdium.stask.network.packates;

import io.netty.buffer.ByteBuf;
import me.towdium.stask.network.Packet;

/**
 * Author: Towdium
 * Date: 07/04/19
 */
public class PString extends Packet {
    static final String IDENTIFIER = "string";

    static {
        Packet.Registry.register(IDENTIFIER, PString::new);
    }

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
    public void handle() {
        System.out.println(str);
    }

    @Override
    public String identifier() {
        return IDENTIFIER;
    }
}