package me.towdium.stask.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.io.Closeable;
import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Author: Towdium
 * Date: 06/04/19
 */

public class Discover implements Closeable {
    static final String SERVER = "Knock knock from STask server";
    static final String CLIENT = "Knock knock from STask client";
    NioEventLoopGroup group;
    ChannelFuture channel;
    Consumer<InetAddress> search;
    Set<InetAddress> discovered = new HashSet<>();
    boolean discoverable = false;

    public Discover() {
        group = new NioEventLoopGroup();
        channel = new Bootstrap().group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(final NioDatagramChannel ch) {
                        ch.pipeline().addLast(new Handler());
                    }
                }).bind(new InetSocketAddress(25566));
    }

    static InetSocketAddress address() {
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

    @Override
    public void close() {
        group.shutdownGracefully();
    }

    public void activate(Consumer<InetAddress> consumer) {
        discovered.clear();
        search = consumer;
        ByteBuf buf = Unpooled.copiedBuffer(CLIENT.getBytes(CharsetUtil.UTF_8));
        channel.channel().writeAndFlush(new DatagramPacket(buf, address())).syncUninterruptibly();
    }

    public void deactivate() {
        search = null;
    }

    public void setDiscoverable(boolean b) {
        discoverable = b;
    }

    public class Handler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
            final ByteBuf buf = msg.content();
            if (buf.toString(CharsetUtil.UTF_8).equals(CLIENT)) {
                if (!discoverable) return;
                ByteBuf out = ctx.alloc().buffer();
                out.writeBytes(SERVER.getBytes(CharsetUtil.UTF_8));
                ctx.writeAndFlush(new DatagramPacket(out, address()));
            } else if (buf.toString(CharsetUtil.UTF_8).equals(SERVER)) {
                InetAddress address = msg.sender().getAddress();
                if (search != null && discovered.add(address)) search.accept(address);
            }
        }
    }
}
