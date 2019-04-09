package me.towdium.stask.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.towdium.stask.network.Packet.Decoder;
import me.towdium.stask.network.Packet.Encoder;
import me.towdium.stask.utils.Pair;

import java.io.Closeable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Towdium
 * Date: 07/04/19
 */
public class Server implements Closeable, Runnable {
    Tunnel tunnel;
    Channel[] players = new Channel[4];
    volatile boolean close = false;
    volatile BlockingDeque<Pair<Packet, Context>> queue = new LinkedBlockingDeque<>();

    public Server() {
        tunnel = new Tunnel();
    }

    @Override
    public synchronized void close() {
        close = true;
        tunnel.close();
    }

    @Override
    public void run() {
        while (!close) tick();
    }

    protected void tick() {
        Pair<Packet, Context> p;
        while ((p = queue.poll()) != null) p.a.handle(p.b);
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

    class Tunnel implements Closeable {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        public Tunnel() {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new Encoder(), new Decoder(), new Handler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .bind(25566);
        }

        @Override
        public void close() {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    class Handler extends SimpleChannelInboundHandler<Packet> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            queue.add(new Pair<>(msg, new Context(ctx)));
        }
    }
}
