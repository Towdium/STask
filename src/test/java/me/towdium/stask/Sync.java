package me.towdium.stask;

import io.netty.buffer.ByteBuf;
import io.netty.channel.local.LocalAddress;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Widgets.WArea;
import me.towdium.stask.client.Widgets.WContainer;
import me.towdium.stask.client.Window;
import me.towdium.stask.network.Network;
import me.towdium.stask.network.Packet;
import me.towdium.stask.network.packates.PConnect;
import me.towdium.stask.utils.Log;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public class Sync {
    WTest test = new WTest();
    Network network;
    boolean master;

    public Sync(boolean master) {
        Log.network.setLevel(Log.Priority.TRACE);
        Packet.Registry.register(PMouse.IDENTIFIER, PMouse::new);
        Packet.Registry.register(PConnect.IDENTIFIER, PConnect::new);
        this.master = master;
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        if (args.length != 1) return;
        if ("0".equals(args[0])) new Sync(true).run();
        else if ("1".equals(args[0])) new Sync(false).run();
    }

    public void run() {
        Page.Simple s = new Page.Simple();
        s.put(test, 0, 0);
        try (Window w = new Window("Sync", s);
             Network n = new Network()) {
            network = n;
            w.display();

            if (master) {
                n.getDiscover().discoverable(true);
                n.getServer().bind(new InetSocketAddress(25566));
                SocketAddress a = n.getServer().bind(LocalAddress.ANY);
                n.getClient().connect(a);
                n.getClient().send(new PConnect());
                new Thread(() -> {
                    while (!n.isClosed()) n.getServer().tick();
                }).start();
            } else {
                n.getDiscover().search(i -> {
                    n.getClient().connect(new InetSocketAddress(i, 25566));
                    n.getClient().send(new PConnect());
                });
            }
            while (!w.isFinished()) {
                n.getClient().tick();
                w.tick();
            }
        }
    }

    class WTest extends WContainer {
        Node node = new Node();
        Vector2i last = new Vector2i(0, 0);

        public WTest() {
            put(node, 20, 20);
        }

        public void move(int x, int y) {
            put(node, x - 20, y - 20);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            if (node.picked && !mouse.equals(last)) update(mouse);
            super.onDraw(p, mouse);
        }

        @Override
        public void onMove(Vector2i mouse) {
            update(mouse);
        }

        private void update(Vector2i v) {
            last = v;
            move(v.x, v.y);
            network.getClient().send(new PMouse(v.x, v.y));
        }

        class Node extends WArea {
            boolean picked = false;

            public Node() {
                super(40, 40);
            }

            @Override
            public void onDraw(Painter p, @Nullable Vector2i mouse) {
                p.drawRect(0, 0, 40, 40);
            }

            @Override
            public boolean onClick(@Nullable Vector2i mouse, boolean left) {
                // TODO picked = state;
                return picked || onTest(mouse);
            }
        }
    }

    class PMouse extends Packet {
        public static final String IDENTIFIER = "mouse";
        int x, y;

        public PMouse(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public PMouse(ByteBuf buf) {
            x = buf.readInt();
            y = buf.readInt();
        }

        @Override
        public void serialize(ByteBuf b) {
            b.writeInt(x);
            b.writeInt(y);
        }

        @Override
        public void handle(Network.Server.Context c) {
            c.relay(new PMouse(x, y));
            Log.network.trace("Server received");
        }

        @Override
        public void handle(Network.Client.Context c) {
            test.move(x, y);
            Log.network.trace("Client " + network.getClient().getIndex() + " received");
        }

        @Override
        public String identifier() {
            return IDENTIFIER;
        }
    }
}
