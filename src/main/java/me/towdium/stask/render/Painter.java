package me.towdium.stask.render;

import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Utilities;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
public class Painter {
    public static Cache<String, Integer> textures = new Cache<>(i -> {
        ByteBuffer image = Utilities.read("/textures/" + i);
        if (image == null) throw new IllegalStateException("Failed to load texture: " + i);

        int w, h, c;

        try (MemoryStack stack = stackPush()) {
            IntBuffer wb = stack.mallocInt(1);
            IntBuffer hb = stack.mallocInt(1);
            IntBuffer cb = stack.mallocInt(1);

            image = stbi_load_from_memory(image, wb, hb, cb, 0);
            if (image == null) throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

            w = wb.get(0);
            h = hb.get(0);
            c = cb.get(0);

//            System.out.println("HelloWorld width: " + w);
//            System.out.println("HelloWorld height: " + h);
//            System.out.println("HelloWorld components: " + c);
        }

        int format = c == 3 ? GL_RGB : GL_RGBA;
        int ret = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, ret);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        if (c == 3 && (w & 3) != 0) glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (w & 1));
        glTexImage2D(GL_TEXTURE_2D, 0, format, w, h, 0, format, GL_UNSIGNED_BYTE, image);

        stbi_image_free(image);
        return ret;
    });
    static STBTTFontinfo font;
    public static Cache<Character, Glyph> glyphs = new Cache<>(i -> {
        Glyph ret = new Glyph();
        try (MemoryStack stack = stackPush()) {
            IntBuffer a = stack.mallocInt(1);
            IntBuffer b = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            IntBuffer d = stack.mallocInt(1);
            ret.id = glGenTextures();
            float scale = stbtt_ScaleForPixelHeight(font, 32);
            ByteBuffer bitmap = stbtt_GetCodepointBitmap(font, scale, scale, i, a, b, c, d);
            glBindTexture(GL_TEXTURE_2D, ret.id);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, a.get(0), b.get(0),
                    0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            if (bitmap != null) stbtt_FreeBitmap(bitmap);
            stbtt_GetCodepointBitmapBox(font, i, scale, scale, a, b, c, d);
            float x0f = a.get(0);
            float x1f = c.get(0);
            float y0f = b.get(0);
            float y1f = d.get(0);
            ret.vertex = new float[]{x0f, y0f, x1f, y0f, x1f, y1f, x0f, y1f};
            stbtt_GetCodepointHMetrics(font, i, a, b);
            ret.advance = (int) Math.ceil(a.get(0) * scale);
        }
        return ret;
    });
    static FloatBuffer bufTexture = BufferUtils.createFloatBuffer(256);
    static FloatBuffer bufVertex = BufferUtils.createFloatBuffer(256);
    static int height = 32;
    static float[] fullQuad = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};

    static {
        ByteBuffer data = Utilities.read("/wqymono.ttf");
        Objects.requireNonNull(data, "Failed to load font");
        font = STBTTFontinfo.create();
        if (!stbtt_InitFont(font, data)) throw new IllegalStateException("Failed to initialize font information.");
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    }

    public static void drawString(String s, int x, int y) {
        int p = x;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                y += height + 4;
                p = x;
            } else p += drawChar(s.charAt(i), p, y);
        }
    }

    public static int drawChar(char c, int x, int y) {
        Painter.Glyph g = Painter.glyphs.get(c);

        bufTexture.clear();
        bufVertex.clear();
        bufTexture.put(fullQuad);
        bufVertex.put(g.vertex);
        bufTexture.flip();
        bufVertex.flip();

        glPushMatrix();
        glTranslatef(x, y, 0);
        flush(g.id);
        glPopMatrix();

        return g.advance;
    }

    private static void flush(int texture) {
        glBindTexture(GL_TEXTURE_2D, texture);
        glVertexPointer(2, GL_FLOAT, 0, bufVertex);
        glTexCoordPointer(2, GL_FLOAT, 0, bufTexture);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glDrawArrays(GL_QUADS, 0, 4);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    public static void drawTexture(String texture, int x, int y) {
        bufTexture.clear();
        bufVertex.clear();
        bufTexture.put(fullQuad);
        bufVertex.put(0).put(0).put(x).put(0).put(x).put(y).put(0).put(y);
        bufTexture.flip();
        bufVertex.flip();

        flush(textures.get(texture));
    }

    public static class Glyph {
        public int id;
        public float[] vertex;
        public int advance;
    }
}
