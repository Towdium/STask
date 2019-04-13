package me.towdium.stask.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import me.towdium.stask.utils.Closeable;
import me.towdium.stask.utils.Tickable;

import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

/**
 * Author: Towdium
 * Date: 13/04/19
 */
public class Network extends Closeable {
    NioEventLoopGroup group = new NioEventLoopGroup(1);
    Server server = new Server();
    Client client = new Client();
    Discover discover = new Discover();

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

    @Override
    public void close() {
        group.shutdownGracefully();
    }

    @Override
    public boolean isClosed() {
        return group.isTerminated();
    }

    public Server getServer() {
        return server;
    }

    public Client getClient() {
        return client;
    }

    public Discover getDiscover() {
        return discover;
    }

    public class Client implements Tickable {
        final Context context = new Context();
        final BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
        private Channel channel;
        private int index = -1;

        public void connect(SocketAddress address) {
            Bootstrap b;
            if (address instanceof LocalAddress) {
                b = new Bootstrap().channel(LocalChannel.class);
            } else {
                b = new Bootstrap().channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true);
            }

            channel = b.group(group)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        public void initChannel(Channel ch) {
                            ch.pipeline().addLast(new Packet.Decoder(), new Packet.Encoder(), new Handler());
                        }
                    }).connect(address).syncUninterruptibly().channel();
        }

        public int getIndex() {
            return index;
        }

        @Override
        public void tick() {
            Runnable r;
            while ((r = queue.poll()) != null) r.run();
        }

        public void send(Packet p) {
            channel.writeAndFlush(p);
        }

        public class Context {
            public void reply(Packet p) {
                channel.writeAndFlush(p);
            }

            public void connect(int index) {
                Client.this.index = index;
            }
        }

        class Handler extends SimpleChannelInboundHandler<Packet> {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
                queue.add(() -> msg.handle(context));
            }
        }
    }

    public class Server implements Tickable {
        final BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
        Channel[] players = new Channel[4];
        Map<SocketAddress, Channel> channels = new HashMap<>();

        public SocketAddress bind(SocketAddress address) {
            Channel c;
            ServerBootstrap b;
            if (address instanceof LocalAddress) {
                b = new ServerBootstrap().channel(LocalServerChannel.class);
            } else {
                b = new ServerBootstrap().channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
            }

            c = b.group(group, group)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        public void initChannel(Channel ch) {
                            ch.pipeline().addLast(new Packet.Encoder(), new Packet.Decoder(), new Handler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .bind(address).syncUninterruptibly().channel();
            channels.put(c.localAddress(), c);
            return c.localAddress();
        }

        public void unbind(SocketAddress address) {
            channels.get(address).close();
            channels.remove(address);
        }

        @Override
        public void tick() {
            Runnable r;
            while ((r = queue.poll()) != null) r.run();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public class Context {
            Channel channel;

            public Context(ChannelHandlerContext ctx) {
                channel = ctx.channel();
            }

            public void reply(Packet p) {
                channel.writeAndFlush(p);
            }

            public void relay(Packet p) {
                for (Channel c : players) {
                    if (c != null && c != channel) c.writeAndFlush(p);
                }
            }

            public void broadcast(Packet p) {
                for (Channel c : players) c.writeAndFlush(p);
            }

            public int player() {
                for (int i = 0; i < players.length; i++) {
                    if (players[i] == channel) return i;
                }
                return -1;
            }

            public int connect() {
                for (int i = 0; i < players.length; i++) {
                    if (players[i] == null) {
                        players[i] = channel;
                        return i;
                    }
                }
                return -1;
            }
        }

        class Handler extends SimpleChannelInboundHandler<Packet> {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
                queue.add(() -> msg.handle(new Context(ctx)));
            }
        }
    }

    public class Discover {
        static final String SERVER = "Knock knock from STask server";
        static final String CLIENT = "Knock knock from STask client";
        Channel channel;
        Consumer<InetAddress> search;
        Set<InetAddress> discovered = new HashSet<>();
        boolean discoverable = false;

        public Discover() {
            channel = new Bootstrap().group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        public void initChannel(final NioDatagramChannel ch) {
                            ch.pipeline().addLast(new Handler());
                        }
                    }).bind(new InetSocketAddress(25566))
                    .syncUninterruptibly().channel();
        }

        public void search(Consumer<InetAddress> consumer) {
            search = consumer;
            if (consumer != null) {
                discovered.clear();
                ByteBuf buf = Unpooled.copiedBuffer(CLIENT.getBytes(CharsetUtil.UTF_8));
                channel.writeAndFlush(new DatagramPacket(buf, broadcast())).syncUninterruptibly();
            }
        }

        public void discoverable(boolean b) {
            discoverable = b;
        }

        public class Handler extends SimpleChannelInboundHandler<DatagramPacket> {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
                final ByteBuf buf = msg.content();
                if (buf.toString(CharsetUtil.UTF_8).equals(CLIENT) && discoverable) {
                    ByteBuf out = ctx.alloc().buffer();
                    out.writeBytes(SERVER.getBytes(CharsetUtil.UTF_8));
                    ctx.writeAndFlush(new DatagramPacket(out, broadcast()));
                } else if (buf.toString(CharsetUtil.UTF_8).equals(SERVER)) {
                    InetAddress address = msg.sender().getAddress();
                    if (search != null && discovered.add(address)) client.queue.add(() -> search.accept(address));
                }
            }
        }
    }
}