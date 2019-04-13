package me.towdium.stask.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.towdium.stask.network.Packet.Decoder;
import me.towdium.stask.network.Packet.Encoder;
import me.towdium.stask.utils.Closeable;
import me.towdium.stask.utils.Tickable;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public class Client extends Closeable implements Tickable {
    final Context context = new Context();
    final BlockingDeque<Packet> queue = new LinkedBlockingDeque<>();
    private EventLoopGroup worker = new NioEventLoopGroup(1);
    private Channel channel;
    private int index = -1;

    public Client() {
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

    public int getIndex() {
        return index;
    }

    @Override
    public synchronized void close() {
        super.close();
        worker.shutdownGracefully();
    }

    @Override
    public void tick() {
        Packet p;
        while ((p = queue.poll()) != null) p.handle(context);
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
            queue.add(msg);
        }
    }
}
