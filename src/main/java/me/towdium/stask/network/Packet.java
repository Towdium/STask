package me.towdium.stask.network;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Author: Towdium
 * Date: 07/04/19
 */
public abstract class Packet {
    public static void writeString(String str, ByteBuf buf) {
        byte[] bytes = str.getBytes(CharsetUtil.UTF_8);
        buf.writeInt(bytes.length).writeBytes(bytes);
    }

    public static String readString(ByteBuf buf) {
        int len = buf.readInt();
        return buf.readBytes(len).toString(CharsetUtil.UTF_8);
    }

    public abstract void serialize(ByteBuf b);

    public abstract void handle();

    public abstract String identifier();

    public static class Registry {
        static Map<String, Function<ByteBuf, Packet>> map = new HashMap<>();

        public static void register(String id, Function<ByteBuf, Packet> factory) {
            map.put(id, factory);
        }

        public static Function<ByteBuf, Packet> find(String id) {
            return map.get(id);
        }
    }
}
