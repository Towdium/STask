package me.towdium.stask;

import io.netty.buffer.ByteBuf;
import me.towdium.stask.gui.Painter;
import me.towdium.stask.gui.Widgets.WArea;
import me.towdium.stask.gui.Widgets.WContainer;
import me.towdium.stask.gui.Window;
import me.towdium.stask.network.Client;
import me.towdium.stask.network.Packet;
import me.towdium.stask.network.Server;
import me.towdium.stask.network.packates.PConnect;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
public class Network extends Client {
    WTest test = new WTest();

    public Network() {
        Packet.Registry.register(PMouse.IDENTIFIER, PMouse::new);
        Packet.Registry.register(PConnect.IDENTIFIER, PConnect::new);

        tunnel.send(new PConnect());
        Window.display(new WContainer().add(0, 0, test));
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        if (args.length != 1) return;

        if ("0".equals(args[0])) {
            try (Server s = new Server();
                 Client c = new Network()) {
                new Thread(s).start();
                c.run();
            }
        } else if ("1".equals(args[0])) {
            try (Client c = new Network()) {
                c.run();
            }
        }
    }

    @Override
    protected void tick() {
        super.tick();
        Window.tick();
        if (Window.finished()) close();
    }

    @Override
    public synchronized void close() {
        super.close();
        Window.destroy();
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
        public void onDraw(Vector2i mouse) {
            super.onDraw(mouse);
            if (dirty) {
                tunnel.send(new PMouse(mouse.x, mouse.y));
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
            public void onDraw(Vector2i mouse) {
                if (picked) {
                    Painter.drawRect(mouse.x - 20, mouse.y - 20, 40, 40);
                    if (!mouse.equals(last)) {
                        dirty = true;
                        last = mouse;
                    }
                } else Painter.drawRect(0, 0, 40, 40);
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
        public void handle(Server.Context c) {
            c.relay(new PMouse(x, y));
            System.out.println("Server received");
        }

        @Override
        public void handle(Context c) {
            test.move(x, y);
            System.out.println("Client " + getIndex() + " received");
        }

        @Override
        public String identifier() {
            return IDENTIFIER;
        }
    }
}
