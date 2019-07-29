package me.towdium.stask.utils;


import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
            Log.client.info("" + is.available());
            is.read(buffer);
            ByteBuffer ret = createByteBuffer(buffer.length);
            ret.put(buffer);
            ret.flip();
            return ret;
        } catch (IOException e) {
            return null;
        }
    }

//    public static ByteBuffer read(String path) {
//        File f = new File(path);
//        try (InputStream is = new FileInputStream(f)) {
//            Objects.requireNonNull(is, "Resource not found: " + path);
//            byte[] buffer = new byte[is.available()];
//            Log.client.info("" + is.available());
//            is.read(buffer);
//            ByteBuffer ret = createByteBuffer(buffer.length);
//            ret.put(buffer);
//            ret.flip();
//            return ret;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

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

    public static String[] list(String path) {
        String error = "Resource not found: " + path;
        try {
            URL url = Utilities.class.getResource(path);
            if (url != null && url.getProtocol().equals("file")) {
                String[] ret = new File(url.toURI()).list();
                Objects.requireNonNull(ret, error);
                Arrays.sort(ret);
                return ret;
            }
            Objects.requireNonNull(url, error);
            if (url.getProtocol().equals("jar")) {
                String parent = url.getPath().substring(5, url.getPath().indexOf("!")); //strip out only the JAR file
                JarFile jar = new JarFile(parent);
                String s = path.startsWith("/") ? path.substring(1) : path;
                return jar.stream().map(ZipEntry::getName)
                        .filter(i -> i.startsWith(s) && !i.equals(s))
                        .map(i -> i.substring(s.length()))
                        .sorted().toArray(String[]::new);
            }
            throw new RuntimeException(error);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(error);
        }
    }

    public static void sleep(long ms, int ns) {
        try {
            Thread.sleep(ms, ns);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
