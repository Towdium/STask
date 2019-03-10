package me.towdium.stask.gui;

import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Utilities;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.BreakIterator;
import java.util.Objects;
import java.util.Stack;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
@NotNull
public class Painter {
    public static int fontHeight = 32;
    public static int fontAscent, fontDescent, fontGap;
    static Cache<String, Texture> textures = new Cache<>(Texture::new);
    static STBTTFontinfo fontInfo;
    static float fontScale;
    static Cache<Character, Glyph> glyphs = new Cache<>(Glyph::new);
    static Stack<Matrix4f> matrices = new Stack<>();
    static FloatBuffer bufTexture = BufferUtils.createFloatBuffer(65536);
    static FloatBuffer bufVertex = BufferUtils.createFloatBuffer(65536);
    static float textureSize = 1024;
    static float[] fullQuad = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
    static Stack<Quad> masks = new Stack<>();
    static Stack<Integer> colors = new Stack<>();
    static int shaderMModel;
    static int shaderIMode;
    static int shaderVColor;
    static int shaderMProj;
    static int shaderVClip;
    static int shader;

    static {
        // initialize font
        ByteBuffer data = Utilities.readBytes("/wqymono.ttf");
        Objects.requireNonNull(data, "Failed to load font");
        fontInfo = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont(fontInfo, data))
            throw new IllegalStateException("Failed to initialize font information.");
        fontScale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, 32);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer des = stack.mallocInt(1);
            IntBuffer asc = stack.mallocInt(1);
            IntBuffer gap = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, asc, des, gap);
            fontAscent = (int) (asc.get(0) * fontScale);
            fontDescent = (int) (des.get(0) * fontScale);
            fontGap = (int) (gap.get(0) * fontScale);
        }

        // initialize shader
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        int vert = GL30C.glCreateShader(GL30C.GL_VERTEX_SHADER);
        GL30C.glShaderSource(vert, Utilities.readString("/shaders/shader.vert"));
        GL30C.glCompileShader(vert);
        GL30C.glGetShaderiv(vert, GL30C.GL_COMPILE_STATUS, ib);
        if (ib.get(0) == 0) System.out.println(GL30C.glGetShaderInfoLog(vert));
        int frag = GL30C.glCreateShader(GL30C.GL_FRAGMENT_SHADER);
        GL30C.glShaderSource(frag, Utilities.readString("/shaders/shader.frag"));
        GL30C.glCompileShader(frag);
        GL30C.glGetShaderiv(frag, GL30C.GL_COMPILE_STATUS, ib);
        if (ib.get(0) == 0) System.out.println(GL30C.glGetShaderInfoLog(frag));
        shader = GL30C.glCreateProgram();
        GL30C.glAttachShader(shader, vert);
        GL30C.glAttachShader(shader, frag);
        GL30C.glLinkProgram(shader);
        GL30C.glDeleteShader(vert);
        GL30C.glDeleteShader(frag);
        GL30C.glUseProgram(shader);
        shaderMModel = GL30C.glGetUniformLocation(shader, "mat");
        shaderVColor = GL30C.glGetUniformLocation(shader, "color");
        shaderIMode = GL30C.glGetUniformLocation(shader, "mode");
        shaderMProj = GL30C.glGetUniformLocation(shader, "proj");
        shaderVClip = GL30C.glGetUniformLocation(shader, "clip");

        // initialize states
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        GL30C.glUniformMatrix4fv(shaderMProj, false, new Matrix4f()
                .ortho(0, Window.windowWidth, Window.windowHeight, 0, 0, 4096)
                .lookAlong(0, 0, -1, 0, 1, 0).get(fb));
        matrices.push(new Matrix4f().translate(0, 0, -1));
        colors.push(0xFFFFFF);
        matUpdate();
        colorUpdate();
        Source.TEXTURE.apply();
        Priority.NORMAL.apply();
        maskUpdate();
    }

    public static void drawText(String s, int xp, int yp, int xs) {
        matPush();
        matTranslate(xp, yp);
        BreakIterator it = BreakIterator.getLineInstance();
        it.setText(s);
        int old = 0, x = 0, y = 0;
        for (int i = it.next(); i != BreakIterator.DONE; i = it.next()) {
            int pos = x;
            for (int j = old; j < i; j++) {
                char ch = s.charAt(j);
                if (ch != ' ' && ch != '　' && ch != '\n')
                    pos += glyphs.get(s.charAt(j)).advance;
                if (pos > xs) break;
            }
            if (pos > xs) {
                x = 0;
                y += fontHeight;
                for (int j = old; j < i; j++) {
                    char ch = s.charAt(j);
                    if (ch == '\n') {
                        x = 0;
                        y += fontHeight;
                    } else if (x + glyphs.get(ch).advance < xs) {
                        x += drawChar(ch, x, y);
                    } else if (ch != ' ' && ch != '　') {
                        y += fontHeight;
                        x = drawChar(ch, 0, y);
                    }
                }
            } else {
                for (int j = old; j < i; j++) {
                    char ch = s.charAt(j);
                    if (ch != '\n') x += drawChar(ch, x, y);
                    else {
                        x = 0;
                        y += fontHeight;
                    }
                }
            }
            old = i;
        }
        matPop();
    }

    public static void drawText(String s, int xp, int yp) {
        int p = xp;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                yp += fontGap + fontHeight;
                p = xp;
            } else p += drawChar(s.charAt(i), p, yp);
        }
    }

    public static int drawChar(char c, int x, int y) {
        Source.ALPHA.apply();
        Painter.Glyph g = Painter.glyphs.get(c);
        g.bind();

        bufTexture.put(fullQuad);
        bufVertex.put(g.vertex);

        matPush();
        matTranslate(x, y);
        flush();
        matPop();
        Source.TEXTURE.apply();
        return g.advance;
    }


    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds, int xsp, int ysp) {
        textures.get(texture).bind();
        put(xdp, ydp, xdp + xds, ydp + yds, xsp, ysp, xsp + xds, ysp + yds);
        flush();
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
                                   int xsp, int ysp, int xss, int yss, int b) {
        drawTexture(texture, xdp, ydp, xds, yds, xsp, ysp, xss, yss, b, b, b, b);
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
                                   int xsp, int ysp, int xss, int yss, int xl, int yt, int xr, int yb) {
        textures.get(texture).bind();

        int xdl = xdp + xl, xdr = xdp + xds - xr;
        int ydt = ydp + yt, ydb = ydp + yds - yb;
        int xsl = xsp + xl, xsr = xsp + xss - xr;
        int yst = ysp + yt, ysb = ysp + yss - yb;
        int xs1 = xsp + xss, ys1 = ysp + yss;
        int xd1 = xdp + xds, yd1 = ydp + yds;

        put(xdp, ydp, xdl, ydt, xsp, ysp, xsl, yst);
        put(xdp, ydb, xdl, yd1, xsp, ysb, xsl, ys1);
        put(xdr, ydp, xd1, ydt, xsr, ysp, xs1, yst);
        put(xdr, ydb, xd1, yd1, xsr, ysb, xs1, ys1);
        put(xdp, ydt, xdl, ydb, xsp, yst, xsl, ysb, true);
        put(xdr, ydt, xd1, ydb, xsr, yst, xs1, ysb, true);
        put(xdl, ydp, xdr, ydt, xsl, ysp, xsr, yst, true);
        put(xdl, ydb, xdr, yd1, xsl, ysb, xsr, ys1, true);
        put(xdl, ydt, xdr, ydb, xsl, yst, xsr, ysb, true);

        flush();
    }

    public static void drawRect(int xp, int yp, int xs, int ys) {
        put(xp, yp, xp + xs, yp + ys, 0, 0, 0, 0);
        flush();
    }

    private static void flush() {
        bufTexture.flip();
        bufVertex.flip();
        GL30C.glEnableVertexAttribArray(0);
        GL30C.glEnableVertexAttribArray(1);
        GL30C.glVertexAttribPointer(0, 2, GL30C.GL_FLOAT, false, 0, bufVertex);
        GL30C.glVertexAttribPointer(1, 2, GL30C.GL_FLOAT, false, 0, bufTexture);
        GL30C.glDrawArrays(GL30C.GL_QUADS, 0, bufVertex.remaining() / 2);
        GL30C.glDisableVertexAttribArray(0);
        GL30C.glDisableVertexAttribArray(1);
        bufTexture.clear();
        bufVertex.clear();
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

    public static void matPush() {
        matrices.push(new Matrix4f(matrices.peek()));
    }

    public static void matPop() {
        matrices.pop();
        matUpdate();
    }

    public static void matTranslate(float x, float y) {
        matTranslate(x, y, 0);
    }

    public static void matTranslate(float x, float y, float z) {
        matrices.peek().translate(x, y, z);
        matUpdate();
    }

    private static void matUpdate() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        GL30C.glUniformMatrix4fv(shaderMModel, false, Painter.matrices.peek().get(fb));
    }

    public static void maskPush(int xp, int yp, int xs, int ys) {
        Quad quad = new Quad(xp, yp, xs, ys).transformed(matrices.peek());
        masks.push(masks.isEmpty() ? quad : new Quad(masks.peek()).intersect(quad));
        maskUpdate();
    }

    public static void maskPop() {
        masks.pop();
        maskUpdate();
    }

    public static void colorPush(int color) {
        colors.push(color);
        colorUpdate();
    }

    public static void colorPop() {
        colors.pop();
        colorUpdate();
    }

    private static void colorUpdate() {
        int c = colors.peek();
        float a = 1 - (c >> 24 & 255) / 255.0F;
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        GL30C.glUniform4f(shaderVColor, r, g, b, a);
    }

    private static void maskUpdate() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(24);
        if (masks.isEmpty()) {
            for (int i = 0; i < 24; i++) fb.put(24);
        } else {
            Quad q = masks.peek();
            fb.put(1).put(0).put(0).put(-q.a.x);
            fb.put(-1).put(0).put(0).put(q.b.x);
            fb.put(0).put(1).put(0).put(-q.a.y);
            fb.put(0).put(-1).put(0).put(q.b.y);
            fb.put(0).put(0).put(1).put(-q.a.z);
            fb.put(0).put(0).put(-1).put(q.b.z);
        }
        fb.flip();
        GL30C.glUniform4fv(shaderVClip, fb);
    }

    static class Quad {
        Vector4f a, b;

        public Quad(int xp, int yp, int xs, int ys) {
            if (xs < 0 || ys < 0) throw new RuntimeException("Size cannot be negative");
            a = new Vector4f(xp, yp, 0, 1);
            b = new Vector4f(xp + xs, yp + ys, 0, 1);
        }

        public Quad(Quad q) {
            a = new Vector4f(q.a);
            b = new Vector4f(q.b);
        }

        public Quad intersect(Quad q) {
            a = new Vector4f(Math.max(a.x, q.a.x), Math.max(a.y, q.a.y), Math.max(a.z, q.a.z), 1);
            b = new Vector4f(Math.min(b.x, q.b.x), Math.min(b.y, q.b.y), Math.min(b.z, q.b.z), 1);
            return this;
        }

        public Quad transformed(Matrix4f m) {
            a.mulProject(m);
            b.mulProject(m);
            for (int i = 0; i < 3; i++) {
                float f1 = a.get(i);
                float f2 = b.get(i);
                if (f2 < f1) {
                    a.setComponent(i, f2);
                    b.setComponent(i, f1);
                }
            }
            return this;
        }
    }

    enum Source {
        SOLID, ALPHA, TEXTURE;

        public void apply() {
            GL30C.glUniform1i(shaderIMode, ordinal());
        }
    }

    public enum Priority {
        PRIORITIZED, NORMAL;

        public void apply() {
            GL30C.glStencilMask(this == PRIORITIZED ? 0xFF : 0);
        }
    }

    public static class Texture {
        int id;

        protected Texture(int id) {
            this.id = id;
        }

        public Texture(String s) {
            ByteBuffer image = Utilities.readBytes("/textures/" + s);
            if (image == null) throw new IllegalStateException("Failed to load texture: " + s);

            int c, x, y;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer wb = stack.mallocInt(1);
                IntBuffer hb = stack.mallocInt(1);
                IntBuffer cb = stack.mallocInt(1);

                image = STBImage.stbi_load_from_memory(image, wb, hb, cb, 0);
                if (image == null)
                    throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());

                x = wb.get(0);
                y = hb.get(0);
                c = cb.get(0);
            }

            int format = c == 3 ? GL30C.GL_RGB : GL30C.GL_RGBA;
            id = GL30C.glGenTextures();
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, id);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_NEAREST);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_NEAREST);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_S, GL30C.GL_CLAMP_TO_EDGE);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_T, GL30C.GL_CLAMP_TO_EDGE);
            if (c == 3 && (x & 3) != 0) GL30C.glPixelStorei(GL30C.GL_UNPACK_ALIGNMENT, 2 - (x & 1));
            GL30C.glTexImage2D(GL30C.GL_TEXTURE_2D, 0, format, x, y, 0, format, GL30C.GL_UNSIGNED_BYTE, image);

            STBImage.stbi_image_free(image);
        }

        public void bind() {
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, id);
        }
    }

    public static class Glyph extends Texture {
        public float[] vertex;
        public int advance;

        public Glyph(char ch) {
            super(GL30C.glGenTextures());
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer a = stack.mallocInt(1);
                IntBuffer b = stack.mallocInt(1);
                IntBuffer c = stack.mallocInt(1);
                IntBuffer d = stack.mallocInt(1);
                ByteBuffer bitmap = STBTruetype.stbtt_GetCodepointBitmap(
                        fontInfo, fontScale, fontScale, ch, a, b, c, d);
                GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, id);
                GL30C.glPixelStorei(GL30C.GL_UNPACK_ALIGNMENT, 1);
                GL30C.glTexImage2D(GL30C.GL_TEXTURE_2D, 0, GL30C.GL_ALPHA, a.get(0), b.get(0),
                        0, GL30C.GL_ALPHA, GL30C.GL_UNSIGNED_BYTE, bitmap);
                GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_NEAREST);
                GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_NEAREST);
                if (bitmap != null) STBTruetype.stbtt_FreeBitmap(bitmap);
                STBTruetype.stbtt_GetCodepointBitmapBox(fontInfo, ch, fontScale, fontScale, a, b, c, d);
                float x0f = a.get(0);
                float x1f = c.get(0);
                float y0f = b.get(0);
                float y1f = d.get(0);
                vertex = new float[]{x0f, y0f, x1f, y0f, x1f, y1f, x0f, y1f};
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, ch, a, b);
                advance = (int) Math.ceil(a.get(0) * fontScale);
            }
        }
    }
}
