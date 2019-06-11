package me.towdium.stask.utils;


import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.lwjgl.BufferUtils.createByteBuffer;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@ParametersAreNonnullByDefault
public class Utilities {
    @Nullable
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static ByteBuffer readBytes(String resource) {
        try (InputStream is = Utilities.class.getResourceAsStream(resource)) {
            Objects.requireNonNull(is, "Resource not found: " + resource);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            ByteBuffer ret = createByteBuffer(buffer.length);
            ret.put(buffer);
            ret.flip();
            return ret;
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String readString(String resource) {
        try (InputStream is = Utilities.class.getResourceAsStream(resource)) {
            Objects.requireNonNull(is, "Resource not found: " + resource);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sleep(long ms, int ns) {
        try {
            Thread.sleep(ms, ns);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface ListenerAction<W> {
        void invoke(W widget);
    }

    public static class Identity<T> {
        public T t;

        public Identity(T t) {
            this.t = t;
        }

        public static <T> Identity<T> of(T t) {
            return new Identity<>(t);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(t);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Identity && t == ((Identity) obj).t;
        }
    }
}
