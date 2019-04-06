package me.towdium.stask.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.*;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Author: Towdium
 * Date: 06/04/19
 */
public class Discover {
    static final String SERVER = "Knock knock from STask server";
    static final String CLIENT = "Knock knock from STask client";
    static NioEventLoopGroup group;
    static Channel channel;
    static BooleanSupplier status;
    static Consumer<InetAddress> search;

    public static void setup(BooleanSupplier status) {
        Discover.status = status;
        group = new NioEventLoopGroup();
        channel = new Bootstrap().group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(final NioDatagramChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new Handler());
                    }
                }).bind(new InetSocketAddress(25566))
                .syncUninterruptibly().channel();
    }

    public static void shutdown() {
        group.shutdownGracefully();
    }

    public static void activate(Consumer<InetAddress> consumer) {
        search = consumer;
        ByteBuf buf = Unpooled.copiedBuffer(CLIENT.getBytes(CharsetUtil.UTF_8));
        channel.writeAndFlush(new DatagramPacket(buf, broadcast())).syncUninterruptibly();
    }

    public static void deactivate() {
        search = null;
    }

    static InetSocketAddress broadcast() {
        InetAddress broadcast = null;
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()) continue;
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    broadcast = interfaceAddress.getBroadcast();
                    if (broadcast != null) break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(broadcast);
        return new InetSocketAddress(broadcast, 25566);
    }

    public static class Handler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
            final ByteBuf buf = msg.content();
            if (buf.toString(CharsetUtil.UTF_8).equals(CLIENT)) {
                if (status == null || !status.getAsBoolean()) return;
                ByteBuf out = ctx.alloc().buffer();
                out.writeBytes(SERVER.getBytes(CharsetUtil.UTF_8));
                ctx.writeAndFlush(new DatagramPacket(out, broadcast()));
            } else if (buf.toString(CharsetUtil.UTF_8).equals(SERVER)) {
                if (search != null) search.accept(msg.sender().getAddress());
            }
        }
    }
}
