package me.towdium.stask.utils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayOutputStream;
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
    static byte[] BUFFER = new byte[1048576];

    public static ByteBuffer readBytes(String resource) {
        byte[] read = readArray(resource);
        ByteBuffer ret = createByteBuffer(read.length);
        ret.put(read);
        ret.flip();
        return ret;
    }

    public static byte[] readArray(String resource) {
        String error = "Resource not found: " + resource;
        try (InputStream is = Utilities.class.getResourceAsStream(resource)) {
            Objects.requireNonNull(is, error);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int size;
            while (-1 != (size = is.read(BUFFER))) os.write(BUFFER, 0, size);
            return os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(error);
        }
    }

    public static String readString(String resource) {
        byte[] read = readArray(resource);
        return new String(read, StandardCharsets.UTF_8);
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
