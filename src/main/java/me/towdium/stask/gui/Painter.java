package me.towdium.stask.gui;

import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Utilities;
import org.jetbrains.annotations.NotNull;
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
@NotNull
public class Painter {
    public static int fontHeight = 32;
    public static int fontAscent, fontDescent, fontGap;
    static Cache<String, Integer> textures = new Cache<>(Painter::genTexture);
    static STBTTFontinfo fontInfo;
    static float fontScale;
    static Cache<Character, Glyph> glyphs = new Cache<>(Painter::genGlyph);
    static FloatBuffer bufTexture = BufferUtils.createFloatBuffer(65536);
    static FloatBuffer bufVertex = BufferUtils.createFloatBuffer(65536);
    static float textureSize = 1024;
    static float[] fullQuad = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};

    static {
        ByteBuffer data = Utilities.read("/wqymono.ttf");
        Objects.requireNonNull(data, "Failed to load font");
        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, data)) throw new IllegalStateException("Failed to initialize font information.");
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        fontScale = stbtt_ScaleForPixelHeight(fontInfo, 32);
        try (MemoryStack stack = stackPush()) {
            IntBuffer des = stack.mallocInt(1);
            IntBuffer asc = stack.mallocInt(1);
            IntBuffer gap = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, asc, des, gap);
            fontAscent = (int) (asc.get(0) * fontScale);
            fontDescent = (int) (des.get(0) * fontScale);
            fontGap = (int) (gap.get(0) * fontScale);
        }
    }

    public static void drawString(String s, int x, int y) {
        int p = x;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                y += fontGap + fontHeight;
                p = x;
            } else p += drawChar(s.charAt(i), p, y);
        }
    }

    public static int drawChar(char c, int x, int y) {
        Painter.Glyph g = Painter.glyphs.get(c);

        bufTexture.put(fullQuad);
        bufVertex.put(g.vertex);

        glPushMatrix();
        glTranslatef(x, y, 0);
        flush(g.id);
        glPopMatrix();

        return g.advance;
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
            int xsp, int ysp, int xss, int yss, int b) {
        drawTextureA(texture, xdp, ydp, xdp + xds, ydp + yds,
                xsp, ysp, xsp + xss, ysp + yss, b, b, b, b);
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
            int xsp, int ysp, int xss, int yss, int xl, int yt, int xr, int yb) {
        drawTextureA(texture, xdp, ydp, xdp + xds, ydp + yds,
                xsp, ysp, xsp + xss, ysp + yss, xl, yt, xr, yb);
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds, int xsp, int ysp) {
        drawTextureA(texture, xdp, ydp, xdp + xds, ydp + yds, xsp, ysp);
    }

    private static void drawTextureA(String texture, int xd0, int yd0, int xd1, int yd1, int xs0, int ys0) {
        put(xd0, yd0, xd1, yd1, xs0, ys0, xs0 + xd1 - xd0, ys0 + yd1 - yd0);
        flush(textures.get(texture));
    }

    private static void drawTextureA(String texture, int xd0, int yd0, int xd1, int yd1,
            int xs0, int ys0, int xs1, int ys1, int xl, int yt, int xr, int yb) {
        int t = textures.get(texture);
        int xdl = xd0 + xl, xdr = xd1 - xr;
        int ydt = yd0 + yt, ydb = yd1 - yb;
        int xsl = xs0 + xl, xsr = xs1 - xr;
        int yst = ys0 + yt, ysb = ys1 - yb;

        put(xd0, yd0, xdl, ydt, xs0, ys0, xsl, yst);
        put(xd0, ydb, xdl, yd1, xs0, ysb, xsl, ys1);
        put(xdr, yd0, xd1, ydt, xsr, ys0, xs1, yst);
        put(xdr, ydb, xd1, yd1, xsr, ysb, xs1, ys1);
        put(xd0, ydt, xdl, ydb, xs0, yst, xsl, ysb, true);
        put(xdr, ydt, xd1, ydb, xsr, yst, xs1, ysb, true);
        put(xdl, yd0, xdr, ydt, xsl, ys0, xsr, yst, true);
        put(xdl, ydb, xdr, yd1, xsl, ysb, xsr, ys1, true);
        put(xdl, ydt, xdr, ydb, xsl, yst, xsr, ysb, true);

        flush(t);
    }

    private static void put(int xd0, int yd0, int xd1, int yd1, int xs0, int ys0, int xs1, int ys1) {
        put(xd0, yd0, xd1, yd1, xs0, ys0, xs1, ys1, false);
    }

    @SuppressWarnings("Duplicates")
    private static void put(int xd0, int yd0, int xd1, int yd1, int xs0, int ys0, int xs1, int ys1, boolean expand) {
        float xt0 = xs0 / textureSize, xt1 = xs1 / textureSize;
        float yt0 = ys0 / textureSize, yt1 = ys1 / textureSize;
        if (expand) {
            int xd = xs1 - xs0, yd = ys1 - ys0;
            float xtp = xt0 + ((xd1 - xd0) % xd) / textureSize;
            float ytp = yt0 + ((yd1 - yd0) % yd) / textureSize;

            for (int yp0 = yd0; yp0 < yd1; ) {
                int yp1 = yp0 + yd;
                for (int xp0 = xd0; xp0 < xd1; ) {
                    int xp1 = xp0 + xd;
                    float xt = xp1 > xd1 ? xtp : xt1;
                    float yt = yp1 > yd1 ? ytp : yt1;
                    int xp = Math.min(xp1, xd1);
                    int yp = Math.min(yp1, yd1);
                    bufTexture.put(xt0).put(yt0).put(xt).put(yt0).put(xt).put(yt).put(xt0).put(yt);
                    bufVertex.put(xp0).put(yp0).put(xp).put(yp0).put(xp).put(yp).put(xp0).put(yp);
                    xp0 = xp1;
                }
                yp0 = yp1;
            }
        } else {
            bufTexture.put(xt0).put(yt0).put(xt1).put(yt0).put(xt1).put(yt1).put(xt0).put(yt1);
            bufVertex.put(xd0).put(yd0).put(xd1).put(yd0).put(xd1).put(yd1).put(xd0).put(yd1);
        }
    }

    private static void flush(int texture) {
        bufTexture.flip();
        bufVertex.flip();
        glBindTexture(GL_TEXTURE_2D, texture);
        glVertexPointer(2, GL_FLOAT, 0, bufVertex);
        glTexCoordPointer(2, GL_FLOAT, 0, bufTexture);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glDrawArrays(GL_QUADS, 0, bufVertex.remaining() / 2);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
        bufTexture.clear();
        bufVertex.clear();
    }

    private static Glyph genGlyph(Character i) {
        Glyph ret = new Glyph();
        try (MemoryStack stack = stackPush()) {
            IntBuffer a = stack.mallocInt(1);
            IntBuffer b = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            IntBuffer d = stack.mallocInt(1);
            ret.id = glGenTextures();
            ByteBuffer bitmap = stbtt_GetCodepointBitmap(fontInfo, fontScale, fontScale, i, a, b, c, d);
            glBindTexture(GL_TEXTURE_2D, ret.id);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, a.get(0), b.get(0),
                    0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            if (bitmap != null) stbtt_FreeBitmap(bitmap);
            stbtt_GetCodepointBitmapBox(fontInfo, i, fontScale, fontScale, a, b, c, d);
            float x0f = a.get(0);
            float x1f = c.get(0);
            float y0f = b.get(0);
            float y1f = d.get(0);
            ret.vertex = new float[]{x0f, y0f, x1f, y0f, x1f, y1f, x0f, y1f};
            stbtt_GetCodepointHMetrics(fontInfo, i, a, b);
            ret.advance = (int) Math.ceil(a.get(0) * fontScale);
        }
        return ret;
    }

    private static Integer genTexture(String i) {
        ByteBuffer image = Utilities.read("/textures/" + i);
        if (image == null) throw new IllegalStateException("Failed to load texture: " + i);

        int c, x, y;

        try (MemoryStack stack = stackPush()) {
            IntBuffer wb = stack.mallocInt(1);
            IntBuffer hb = stack.mallocInt(1);
            IntBuffer cb = stack.mallocInt(1);

            image = stbi_load_from_memory(image, wb, hb, cb, 0);
            if (image == null) throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

            x = wb.get(0);
            y = hb.get(0);
            c = cb.get(0);
        }

        int format = c == 3 ? GL_RGB : GL_RGBA;
        int ret = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, ret);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        if (c == 3 && (x & 3) != 0) glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (x & 1));
        glTexImage2D(GL_TEXTURE_2D, 0, format, x, y, 0, format, GL_UNSIGNED_BYTE, image);

        stbi_image_free(image);
        return ret;
    }

    public static class Glyph {
        public int id;
        public float[] vertex;
        public int advance;
    }
}
