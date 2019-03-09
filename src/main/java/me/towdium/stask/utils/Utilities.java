package me.towdium.stask.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
@NotNull
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
}
