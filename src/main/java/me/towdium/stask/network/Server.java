package me.towdium.stask.network;

import java.io.Closeable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Author: Towdium
 * Date: 07/04/19
 */
public class Server implements Closeable {
    Thread thread;
    Tunnel tunnel;
    volatile boolean close = false;
    volatile BlockingDeque<Packet> queue = new LinkedBlockingDeque<>();

    public Server() {
        tunnel = new Tunnel.Server(queue::add);
        thread = new Thread(() -> {
            while (!close) {
                Packet p;
                while ((p = queue.poll()) != null) p.handle();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public synchronized void close() {
        close = true;
        tunnel.close();
    }
}
