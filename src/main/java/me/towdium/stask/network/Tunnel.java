package me.towdium.stask.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.Closeable;
import java.util.List;
import java.util.function.Consumer;

/**
 * Author: Towdium
 * Date: 06/04/19
 */
public interface Tunnel extends Closeable {
    void send(Packet p);

    @Override
    void close();

    class Listener extends SimpleChannelInboundHandler<Packet> {
        Consumer<Packet> consumer;

        public Listener(Consumer<Packet> c) {
            consumer = c;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            consumer.accept(msg);
        }
    }

    class Encoder extends MessageToByteEncoder<Packet> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) {
            out.writeInt(0);
            Packet.writeString(msg.identifier(), out);
            msg.serialize(out);
            out.setInt(0, out.readableBytes());
        }
    }

    class Decoder extends ByteToMessageDecoder {
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

    class Server implements Tunnel {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        ChannelFuture channel;

        public Server(Consumer<Packet> c) {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new Encoder(), new Decoder(), new Listener(c));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = b.bind(25566);
        }

        @Override
        public void close() {
            System.out.println("Shutdown server");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

        @Override
        public void send(Packet p) {
            channel.channel().writeAndFlush(p);
        }
    }

    class Client implements Tunnel {
        EventLoopGroup worker = new NioEventLoopGroup();
        ChannelFuture channel;

        public Client(Consumer<Packet> c) {
            Bootstrap b = new Bootstrap();
            b.group(worker);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new Decoder(), new Encoder(), new Listener(c));
                }
            });

            channel = b.connect("127.0.0.1", 25566);
        }

        @Override
        public void close() {
            System.out.println("Shutdown client");
            worker.shutdownGracefully();
        }

        @Override
        public void send(Packet p) {
            channel.channel().writeAndFlush(p);
        }
    }
}
