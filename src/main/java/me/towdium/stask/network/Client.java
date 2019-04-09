package me.towdium.stask.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.towdium.stask.network.Packet.Decoder;
import me.towdium.stask.network.Packet.Encoder;

import java.io.Closeable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public class Client implements Closeable, Runnable {
    final Context context = new Context();
    protected Tunnel tunnel;
    protected int index = -1;
    volatile BlockingDeque<Packet> queue = new LinkedBlockingDeque<>();
    volatile boolean close = false;

    public Client() {
        tunnel = new Tunnel();
    }

    public int getIndex() {
        return index;
    }

    @Override
    public synchronized void close() {
        tunnel.close();
        close = true;
    }

    @Override
    public void run() {
        while (!close) tick();
    }

    protected void tick() {
        Packet p;
        while ((p = queue.poll()) != null) p.handle(context);
    }

    public class Tunnel implements Closeable {
        EventLoopGroup worker = new NioEventLoopGroup();
        Channel channel;

        public Tunnel() {
            Bootstrap b = new Bootstrap();
            b.group(worker);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new Decoder(), new Encoder(), new Handler());
                }
            });
            channel = b.connect("127.0.0.1", 25566).syncUninterruptibly().channel();
        }

        @Override
        public void close() {
            worker.shutdownGracefully();
        }

        public void send(Packet p) {
            channel.writeAndFlush(p);
        }
    }

    public class Context {
        public void reply(Packet p) {
            tunnel.channel.writeAndFlush(p);
        }

        public void connect(int index) {
            Client.this.index = index;
        }
    }

    class Handler extends SimpleChannelInboundHandler<Packet> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            queue.add(msg);
        }
    }
}
