package me.towdium.stask.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.List;
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

    public abstract void handle(Server.Context c);

    public abstract void handle(Client.Context c);

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

    public static class Encoder extends MessageToByteEncoder<Packet> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) {
            out.writeInt(0);
            Packet.writeString(msg.identifier(), out);
            msg.serialize(out);
            out.setInt(0, out.readableBytes());
        }
    }

    public static class Decoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            if (in.readableBytes() < 4) return;
            int len = in.getInt(0);
            if (in.readableBytes() < len) return;
            in.readInt();
            String id = Packet.readString(in);
            Packet p = Packet.Registry.find(id).apply(in);
            out.add(p);
        }
    }
}
