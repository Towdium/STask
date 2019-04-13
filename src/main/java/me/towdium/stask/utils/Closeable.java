package me.towdium.stask.utils;

/**
 * Author: Towdium
 * Date: 13/04/19
 */
public abstract class Closeable implements AutoCloseable {
    protected volatile boolean closed;

    @Override
    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }
}
