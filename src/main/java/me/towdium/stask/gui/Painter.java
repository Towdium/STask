package me.towdium.stask.gui;

import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Utilities;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
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
    public static Stack<Matrix4f> matrices = new Stack<>();
    static FloatBuffer bufTexture = BufferUtils.createFloatBuffer(65536);
    static FloatBuffer bufVertex = BufferUtils.createFloatBuffer(65536);
    static float textureSize = 1024;
    static float[] fullQuad = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
    static Stack<Quad> masks = new Stack<>();
    static int shaderMat;
    static int shaderAlpha;
    static int shaderColor;
    static int shader;

    static {
        ByteBuffer data = Utilities.readBytes("/wqymono.ttf");
        Objects.requireNonNull(data, "Failed to load font");
        fontInfo = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont(fontInfo, data))
            throw new IllegalStateException("Failed to initialize font information.");
        fontScale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, 32);
        matrices.push(new Matrix4f().ortho2D(0, Window.windowWidth, Window.windowHeight, 0));
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer des = stack.mallocInt(1);
            IntBuffer asc = stack.mallocInt(1);
            IntBuffer gap = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, asc, des, gap);
            fontAscent = (int) (asc.get(0) * fontScale);
            fontDescent = (int) (des.get(0) * fontScale);
            fontGap = (int) (gap.get(0) * fontScale);
        }
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        int vert = GL30.glCreateShader(GL30.GL_VERTEX_SHADER);
        GL30.glShaderSource(vert, Utilities.readString("/shaders/shader.vert"));
        GL30.glCompileShader(vert);
        GL30.glGetShaderiv(vert, GL30.GL_COMPILE_STATUS, ib);
        if (ib.get(0) == 0) System.out.println(GL30.glGetProgramInfoLog(vert));
        int frag = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER);
        GL30.glShaderSource(frag, Utilities.readString("/shaders/shader.frag"));
        GL30.glCompileShader(frag);
        GL30.glGetShaderiv(frag, GL30.GL_COMPILE_STATUS, ib);
        if (ib.get(0) == 0) System.out.println(GL30.glGetProgramInfoLog(frag));
        shader = GL30.glCreateProgram();
        GL30.glAttachShader(shader, vert);
        GL30.glAttachShader(shader, frag);
        GL30.glLinkProgram(shader);
        GL30.glDeleteShader(vert);
        GL30.glDeleteShader(frag);
        GL30.glUseProgram(shader);
        shaderMat = GL30.glGetUniformLocation(shader, "mat");
        shaderColor = GL30.glGetUniformLocation(shader, "color");
        shaderAlpha = GL30.glGetUniformLocation(shader, "alpha");
        matUpdate();
        setColor(0xFFFFFF);
        setAlpha(false);
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
        setAlpha(true);
        Painter.Glyph g = Painter.glyphs.get(c);

        bufTexture.put(fullQuad);
        bufVertex.put(g.vertex);

        matPush();
        matTranslate(x, y);
        flush(g);
        matPop();
        setAlpha(false);
        return g.advance;
    }

    public static void clipSet(int xp, int yp, int xs, int ys) {
        GL30.glEnable(GL30.GL_SCISSOR_TEST);
        GL30.glScissor(xp, Window.windowHeight - yp - ys, xs, ys);
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds, int xsp, int ysp) {
        put(xdp, ydp, xdp + xds, ydp + yds, xsp, ysp, xsp + xds, ysp + yds);
        flush(textures.get(texture));
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
            int xsp, int ysp, int xss, int yss, int b) {
        drawTexture(texture, xdp, ydp, xds, yds, xsp, ysp, xss, yss, b, b, b, b);
    }

    public static void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
            int xsp, int ysp, int xss, int yss, int xl, int yt, int xr, int yb) {
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

        flush(textures.get(texture));
    }

    public static void clipRemove() {
        GL30.glDisable(GL30.GL_SCISSOR_TEST);
    }

    private static void flush(Texture texture) {
        bufTexture.flip();
        bufVertex.flip();
        texture.bind();
        GL30.glEnableVertexAttribArray(0);
        GL30.glEnableVertexAttribArray(1);
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 0, bufVertex);
        GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, bufTexture);
        GL30.glDrawArrays(GL30.GL_QUADS, 0, bufVertex.remaining() / 2);
        GL30.glDisableVertexAttribArray(0);
        GL30.glDisableVertexAttribArray(1);
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

    private static void matPush() {
        matrices.push(new Matrix4f(matrices.peek()));
    }

    private static void matPop() {
        matrices.pop();
        matUpdate();
    }

    private static void matTranslate(float x, float y) {
        matrices.peek().translate(x, y, 0);
        matUpdate();
    }

    private static void matUpdate() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        GL30.glUniformMatrix4fv(shaderMat, false, Painter.matrices.peek().get(fb));
    }

    private static void setColor(int c) {
        float a = 1 - (c >> 24 & 255) / 255.0F;
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        GL30.glUniform4f(shaderColor, r, g, b, a);
    }

    private static void setAlpha(boolean b) {
        GL30.glUniform1ui(1, b ? 1 : 0);
    }

    static class Quad {
        Vector3f a, b;

        public Quad(int xp, int yp, int xs, int ys) {
            a = new Vector3f(xp, yp, 1);
            b = new Vector3f(xp + xs, yp + ys, 1);
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

            int format = c == 3 ? GL30.GL_RGB : GL30.GL_RGBA;
            id = GL30.glGenTextures();
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
            if (c == 3 && (x & 3) != 0) GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 2 - (x & 1));
            GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, format, x, y, 0, format, GL30.GL_UNSIGNED_BYTE, image);

            STBImage.stbi_image_free(image);
        }

        public void bind() {
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
        }
    }

    public static class Glyph extends Texture {
        public float[] vertex;
        public int advance;

        public Glyph(char ch) {
            super(GL30.glGenTextures());
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer a = stack.mallocInt(1);
                IntBuffer b = stack.mallocInt(1);
                IntBuffer c = stack.mallocInt(1);
                IntBuffer d = stack.mallocInt(1);
                ByteBuffer bitmap = STBTruetype.stbtt_GetCodepointBitmap(
                        fontInfo, fontScale, fontScale, ch, a, b, c, d);
                GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
                GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);
                GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_ALPHA, a.get(0), b.get(0),
                        0, GL30.GL_ALPHA, GL30.GL_UNSIGNED_BYTE, bitmap);
                GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
                GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
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
