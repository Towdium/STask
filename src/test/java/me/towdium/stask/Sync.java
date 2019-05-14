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
import me.towdium.stask.utils.time.Ticker;
import org.joml.Vector2i;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
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
        s.add(0, 0, test);
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
            Ticker ticker = new Ticker(1 / 200f);
            while (!w.isFinished()) {
                n.getClient().tick();
                w.tick();
                ticker.sync();
            }
        }
    }

    class WTest extends WContainer {
        boolean dirty = false;

        public WTest() {
            add(20, 20, new Node());
        }

        @Override
        public boolean onMouse(Vector2i mouse, int button, boolean state) {
            if (super.onMouse(mouse, button, state) && widgets.isEmpty()) {
                add(mouse.x - 20, mouse.y - 20, new Node());
                return true;
            } else return false;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            if (dirty) {
                network.getClient().send(new PMouse(mouse.x, mouse.y));
                dirty = false;
            }
        }

        public void move(int x, int y) {
            clear();
            add(x - 20, y - 20, new Node());
        }

        class Node extends WArea {
            boolean picked = false;
            Vector2i last = null;

            public Node() {
                super(40, 40);
            }

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                if (picked) {
                    p.drawRect(mouse.x - 20, mouse.y - 20, 40, 40);
                    if (!mouse.equals(last)) {
                        dirty = true;
                        last = mouse;
                    }
                } else p.drawRect(0, 0, 40, 40);
            }

            @Override
            public boolean onMouse(Vector2i mouse, int button, boolean state) {
                if (picked) {
                    if (!state) {
                        WTest.this.clear();
                        return true;
                    }
                } else {
                    if (state && button == 0 && inside(mouse)) {
                        picked = true;
                        return true;
                    }
                }
                return false;
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
