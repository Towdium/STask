package me.towdium.stask.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.BufferUtils.createByteBuffer;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class Utilities {
    @Nullable
    public static ByteBuffer read(String resource) {
        try {
            InputStream is = Utilities.class.getResourceAsStream(resource);
            byte[] buffer = new byte[is.available()];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            ByteBuffer ret = createByteBuffer(buffer.length);
            ret.put(buffer);
            ret.flip();
            return ret;
        } catch (IOException e) {
            return null;
        }
    }
}
