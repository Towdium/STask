package me.towdium.stask.client;

import me.towdium.stask.utils.Cache;
import me.towdium.stask.utils.Log;
import me.towdium.stask.utils.Quad;
import me.towdium.stask.utils.Utilities;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Closeable;
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
@ParametersAreNonnullByDefault
public class Painter {
    static final FloatBuffer BUF_TEXTURE = BufferUtils.createFloatBuffer(65536);
    static final FloatBuffer BUF_VERTEX = BufferUtils.createFloatBuffer(65536);
    static final FloatBuffer BUF_16 = BufferUtils.createFloatBuffer(16);
    static final FloatBuffer BUF_24 = BufferUtils.createFloatBuffer(24);
    static final float TEXTURE_SIZE = 1024;
    static final float[] FULL_QUAD = new float[]{0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
    public static final int fontHeight = 24;
    public static final int fontAscent, fontDescent, fontGap;
    static STBTTFontinfo fontInfo;
    static ByteBuffer fontData;
    static final Vector4f TEST_A = new Vector4f(0, 0, 0, 1);

    Cache<String, Texture> textures = new Cache<>(Texture::new);
    Stack<Matrix4f> matrices = new Stack<>();
    Stack<Quad> masks = new Stack<>();
    Stack<Integer> colors = new Stack<>();
    static final Vector4f TEST_B = new Vector4f(0, 1, 0, 1);
    Texture empty = new Texture(-1);
    int shaderMModel;
    int shaderIMode;
    int shaderVColor;
    int shaderMProj;
    int shaderVClip;
    int shaderID;
    Window window;

    Painter(Window window) {
        this.window = window;
        IntBuffer ib = BufferUtils.createIntBuffer(1);
        int vert = GL30C.glCreateShader(GL30C.GL_VERTEX_SHADER);
        GL30C.glShaderSource(vert, Objects.requireNonNull(Utilities.readString(
                "/shaders/shader.vert"), "Internal error"));
        GL30C.glCompileShader(vert);
        GL30C.glGetShaderiv(vert, GL30C.GL_COMPILE_STATUS, ib);
        if (ib.get(0) == 0) System.out.println(GL30C.glGetShaderInfoLog(vert));
        int frag = GL30C.glCreateShader(GL30C.GL_FRAGMENT_SHADER);
        GL30C.glShaderSource(frag, Objects.requireNonNull(Utilities.readString(
                "/shaders/shader.frag"), "Internal error"));
        GL30C.glCompileShader(frag);
        GL30C.glGetShaderiv(frag, GL30C.GL_COMPILE_STATUS, ib);
        if (ib.get(0) == 0) System.out.println(GL30C.glGetShaderInfoLog(frag));
        shaderID = GL30C.glCreateProgram();
        GL30C.glAttachShader(shaderID, vert);
        GL30C.glAttachShader(shaderID, frag);
        GL30C.glLinkProgram(shaderID);
        GL30C.glDeleteShader(vert);
        GL30C.glDeleteShader(frag);
        GL30C.glUseProgram(shaderID);
        shaderMModel = GL30C.glGetUniformLocation(shaderID, "mat");
        shaderVColor = GL30C.glGetUniformLocation(shaderID, "color");
        shaderIMode = GL30C.glGetUniformLocation(shaderID, "mode");
        shaderMProj = GL30C.glGetUniformLocation(shaderID, "proj");
        shaderVClip = GL30C.glGetUniformLocation(shaderID, "clip");
        matrices.push(new Matrix4f().translate(0, 0, 0));
        updateMatrix();
        colors.push(0xFFFFFF);
        updateColor();
        maskUpdate();
    }

    void onResize(int width, int height) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        GL30C.glUniformMatrix4fv(shaderMProj, false, new Matrix4f()
                .ortho(0, width, height, 0, 0, 4096)
                .lookAlong(0, 0, -1, 0, 1, 0).get(fb));
    }

    static {
        // initialize font
        // TODO crash in jar
        fontData = Objects.requireNonNull(Utilities.readBytes("/wqymono.ttf"), "Failed to load font");
        fontInfo = STBTTFontinfo.create();
        Log.client.info("" + fontData.get(1));
        Log.client.info(fontData.toString());
        Log.client.info(fontInfo.toString());
        if (!STBTruetype.stbtt_InitFont(fontInfo, fontData))
            throw new IllegalStateException("Failed to initialize font information.");
        float scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, fontHeight);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer des = stack.mallocInt(1);
            IntBuffer asc = stack.mallocInt(1);
            IntBuffer gap = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, asc, des, gap);
            fontAscent = (int) Math.ceil(asc.get(0) * scale);
            fontDescent = -(int) Math.ceil(des.get(0) * scale);
            fontGap = (int) Math.ceil(gap.get(0) * scale);
        }
    }

    Cache<Integer, Cache<Character, Glyph>> glyphs = new Cache<>(i -> new Cache<>(j -> new Glyph(j, i)));

    public void drawTextWrapped(String s, int xp, int yp, int xs) {
        int sc = getScale();
        try (SMatrix mat = matrix()) {
            mat.translate(xp, yp);
            BreakIterator it = BreakIterator.getLineInstance();
            it.setText(s);
            int old = 0;
            int x = 0;
            int y = 0;
            for (int i = it.next(); i != BreakIterator.DONE; i = it.next()) {
                int pos = x;
                for (int j = old; j < i; j++) {
                    char ch = s.charAt(j);
                    if (ch != ' ' && ch != '　' && ch != '\n')
                        pos += glyphs.get(sc).get(s.charAt(j)).advance;
                    if (pos > xs) break;
                }
                if (pos > xs) {
                    x = 0;
                    y += fontHeight * 1.2;
                    for (int j = old; j < i; j++) {
                        char ch = s.charAt(j);
                        if (ch == '\n') {
                            x = 0;
                            y += fontHeight * 1.2;
                        } else if (x + glyphs.get(sc).get(ch).advance < xs) {
                            x += drawChar(ch, x, y, sc);
                        } else if (ch != ' ' && ch != '　') {
                            y += fontHeight * 1.2;
                            x = drawChar(ch, 0, y, sc);
                        }
                    }
                } else {
                    for (int j = old; j < i; j++) {
                        char ch = s.charAt(j);
                        if (ch != '\n') x += drawChar(ch, x, y, sc);
                        else {
                            x = 0;
                            y += fontHeight * 1.2;
                        }
                    }
                }
                old = i;
            }
        }
    }

    public void drawRect(int xp, int yp, int xs, int ys, int w) {
        drawRect(xp, yp, xs - w, w);
        drawRect(xp, yp + w, w, ys - w);
        drawRect(xp + xs - w, yp, w, ys - w);
        drawRect(xp + w, yp + ys - w, xs - w, w);
    }

    public void drawTextCut(String s, int xp, int yp, int xs) {
        int sc = getScale();
        int dots = glyphs.get(sc).get('.').advance * 3;
        int cut = 0;
        boolean full = true;
        int index = 0;
        for (; index < s.length() && cut < xs - dots; index++) {
            cut += glyphs.get(sc).get(s.charAt(index)).advance;
        }
        int len = cut;
        for (int i = index; i < s.length(); i++) {
            len += glyphs.get(sc).get(s.charAt(i)).advance;
            if (len > xs) {
                full = false;
                break;
            }
        }
        if (full) drawText(s, xp + (xs - len) / 2, yp);
        else drawText(s.substring(0, index) + "...", xp + (xs - cut - dots) / 2, yp);
    }

    public void drawText(String s, int xp, int yp) {
        int sc = getScale();
        int p = xp;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                yp += fontGap + fontHeight;
                p = xp;
            } else p += drawChar(s.charAt(i), p, yp, sc);
        }
    }

    public void drawTextRight(String s, int xp, int yp) {
        drawText(s, xp - getLength(s), yp);
    }

    public void drawTextCenter(String s, int xp, int yp) {
        drawText(s, xp - getLength(s) / 2, yp);
    }

    public int getLength(String s) {
        int mul = getScale();
        int len = 0;
        for (int i = 0; i < s.length(); i++) len += glyphs.get(mul).get(s.charAt(i)).advance;
        return len;
    }

    public int getScale() {
        Matrix4f mat = matrices.get(matrices.size() - 1);
        float mul = TEST_A.mul(mat, new Vector4f()).negate().add(TEST_B.mul(mat, new Vector4f())).y;
        return (int) mul;
    }

    private int drawChar(char c, int x, int y, int s) {
        Painter.Glyph g = glyphs.get(s).get(c);
        g.bind();

        BUF_TEXTURE.put(FULL_QUAD);
        BUF_VERTEX.put(g.vertex);

        try (SMatrix m = matrix()) {
            m.translate(x, y);
            m.scale(1f / s, 1f / s);
            flush();
        }
        return g.advance;
    }

    public void drawTexture(String texture, int xdp, int ydp, int xds, int yds, int xsp, int ysp) {
        textures.get(texture).bind();
        put(xdp, ydp, xdp + xds, ydp + yds, xsp, ysp, xsp + xds, ysp + yds);
        flush();
    }

    public void drawResource(Resource r, int x, int y) {
        try (SMatrix matrix = matrix()) {
            matrix.translate(x, y);
            matrix.scale(r.mul, r.mul);
            drawTexture(r.id, 0, 0, r.xs, r.ys, r.xp, r.yp);
        }
    }

    public void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
                            int xsp, int ysp, int xss, int yss, int b) {
        drawTexture(texture, xdp, ydp, xds, yds, xsp, ysp, xss, yss, b, b, b, b);
    }

    public void drawTexture(String texture, int xdp, int ydp, int xds, int yds,
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

    public void drawRect(int xp, int yp, int xs, int ys) {
        empty.bind();
        put(xp, yp, xp + xs, yp + ys, 0, 0, 0, 0);
        flush();
    }

    public SMatrix matrix() {
        return matrix(0);
    }

    public SMatrix matrix(int i) {
        matrices.push(new Matrix4f(matrices.get(matrices.size() - 1 - i)));
        if (i != 0) updateMatrix();
        return new SMatrix();
    }

    public State mask(int xp, int yp, int xs, int ys) {
        Quad quad = new Quad(xp, yp, xs, ys).transformed(matrices.peek());
        masks.push(masks.isEmpty() ? quad : new Quad(masks.peek()).intersect(quad));
        maskUpdate();
        return () -> {
            masks.pop();
            maskUpdate();
        };
    }

    public State mask(Quad q) {
        return mask((int) q.a.x, (int) q.a.y, (int) (q.b.x - q.a.x), (int) (q.b.y - q.a.y));
    }

    public State color(int color) {
        int old = colors.peek();
        // o -> old, c -> input color, n -> new, u -> to update
        float oa = (old >> 24) / 255f;
        float or = (old >> 16 & 0xFF) / 255f;
        float og = (old >> 8 & 0xFF) / 255f;
        float ob = (old & 0xFF) / 255f;
        float ca = (color >> 24) / 255f;
        float cr = (color >> 16 & 0xFF) / 255f;
        float cg = (color >> 8 & 0xFF) / 255f;
        float cb = (color & 0xFF) / 255f;
        float na = 1 - (1 - oa) * (1 - ca);
        float nr = or * cr;
        float ng = og * cg;
        float nb = ob * cb;
        int ua = (int) (na * 255);
        int ur = (int) (nr * 255);
        int ug = (int) (ng * 255);
        int ub = (int) (nb * 255);
        int compose = (ua << 24) + (ur << 16) + (ug << 8) + ub;
        colors.push(compose);
        updateColor();
        return () -> {
            colors.pop();
            updateColor();
        };
    }

    public State color(float transparency) {
        int alpha = (int) (transparency * 255);
        return color((alpha << 24) + 0xFFFFFF);
    }

    private void updateColor() {
        int c = colors.peek();
        float a = 1 - (c >> 24 & 255) / 255.0F;
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        GL30C.glUniform4f(shaderVColor, r, g, b, a);
    }

    private void maskUpdate() {
        if (masks.isEmpty()) {
            for (int i = 0; i < 24; i++) BUF_24.put(24);
        } else {
            Quad q = masks.peek();
            BUF_24.put(1).put(0).put(0).put(-q.a.x);
            BUF_24.put(-1).put(0).put(0).put(q.b.x);
            BUF_24.put(0).put(1).put(0).put(-q.a.y);
            BUF_24.put(0).put(-1).put(0).put(q.b.y);
            BUF_24.put(0).put(0).put(1).put(-q.a.z);
            BUF_24.put(0).put(0).put(-1).put(q.b.z);
        }
        BUF_24.flip();
        GL30C.glUniform4fv(shaderVClip, BUF_24);
        BUF_24.clear();
    }

    private void updateMatrix() {
        GL30C.glUniformMatrix4fv(shaderMModel, false, matrices.peek().get(BUF_16));
        BUF_16.clear();
    }

    @FunctionalInterface
    public interface State extends Closeable {
        @Override
        void close();
    }

    public class SMatrix implements State {
        private SMatrix() {
        }

        public void translate(float x, float y) {
            translate(x, y, 0);
        }

        public void translate(float x, float y, float z) {
            matrices.peek().translate(x, y, z);
            updateMatrix();
        }

        public void scale(float x, float y) {
            scale(x, y, 0);
        }

        public void scale(float x, float y, float z) {
            matrices.peek().scale(x, y, z);
            updateMatrix();
        }

        public void rotate(float rad) {
            matrices.peek().rotate(rad, new Vector3f(0, 0, 1));
            updateMatrix();
        }

        public void rotate(float rad, Vector3f axis) {
            matrices.peek().rotate(rad, axis);
            updateMatrix();
        }

        @Override
        public void close() {
            matrices.pop();
            updateMatrix();
        }
    }

    private static void flush() {
        BUF_TEXTURE.flip();
        BUF_VERTEX.flip();
        GL30C.glEnableVertexAttribArray(0);
        GL30C.glEnableVertexAttribArray(1);
        GL30C.glVertexAttribPointer(0, 2, GL30C.GL_FLOAT, false, 0, BUF_VERTEX);
        GL30C.glVertexAttribPointer(1, 2, GL30C.GL_FLOAT, false, 0, BUF_TEXTURE);
        GL30C.glDrawArrays(GL30C.GL_QUADS, 0, BUF_VERTEX.remaining() / 2);
        GL30C.glDisableVertexAttribArray(0);
        GL30C.glDisableVertexAttribArray(1);
        BUF_TEXTURE.clear();
        BUF_VERTEX.clear();
    }

    private static void put(int xd0, int yd0, int xd1, int yd1, int xs0, int ys0, int xs1, int ys1) {
        put(xd0, yd0, xd1, yd1, xs0, ys0, xs1, ys1, false);
    }

    @SuppressWarnings("Duplicates")
    private static void put(int xd0, int yd0, int xd1, int yd1, int xs0, int ys0, int xs1, int ys1, boolean expand) {
        float xt0 = xs0 / TEXTURE_SIZE, xt1 = xs1 / TEXTURE_SIZE;
        float yt0 = ys0 / TEXTURE_SIZE, yt1 = ys1 / TEXTURE_SIZE;
        if (expand) {
            int xd = xs1 - xs0, yd = ys1 - ys0;
            float xtp = xt0 + ((xd1 - xd0) % xd) / TEXTURE_SIZE;
            float ytp = yt0 + ((yd1 - yd0) % yd) / TEXTURE_SIZE;

            for (int yp0 = yd0; yp0 < yd1; ) {
                int yp1 = yp0 + yd;
                for (int xp0 = xd0; xp0 < xd1; ) {
                    int xp1 = xp0 + xd;
                    float xt = xp1 > xd1 ? xtp : xt1;
                    float yt = yp1 > yd1 ? ytp : yt1;
                    int xp = Math.min(xp1, xd1);
                    int yp = Math.min(yp1, yd1);
                    BUF_TEXTURE.put(xt0).put(yt0).put(xt).put(yt0).put(xt).put(yt).put(xt0).put(yt);
                    BUF_VERTEX.put(xp0).put(yp0).put(xp).put(yp0).put(xp).put(yp).put(xp0).put(yp);
                    xp0 = xp1;
                }
                yp0 = yp1;
            }
        } else {
            BUF_TEXTURE.put(xt0).put(yt0).put(xt1).put(yt0).put(xt1).put(yt1).put(xt0).put(yt1);
            BUF_VERTEX.put(xd0).put(yd0).put(xd1).put(yd0).put(xd1).put(yd1).put(xd0).put(yd1);
        }
    }

    private class Texture {
        static final int SOLID = 0, ALPHA = 1, TEXTURE = 2;

        int id;

        protected Texture(int id) {
            this.id = id;
        }

        public Texture(String s) {
            int c, x, y;
            ByteBuffer image = Utilities.readBytes("/textures/" + s + ".png");
            if (image == null) throw new RuntimeException("Failed to load texture: " + s);

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer wb = stack.mallocInt(1);
                IntBuffer hb = stack.mallocInt(1);
                IntBuffer cb = stack.mallocInt(1);
                image = STBImage.stbi_load_from_memory(image, wb, hb, cb, 0);
                if (image == null) {
                    String err = STBImage.stbi_failure_reason();
                    throw new RuntimeException("Failed to load image: " + err);
                }
                x = wb.get(0);
                y = hb.get(0);
                c = cb.get(0);
            }

            int format = c == 3 ? GL30C.GL_RGB : GL30C.GL_RGBA;
            id = GL30C.glGenTextures();
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, id);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_LINEAR);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_LINEAR);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_S, GL30C.GL_CLAMP_TO_EDGE);
            GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_T, GL30C.GL_CLAMP_TO_EDGE);
            if (c == 3 && (x & 3) != 0) GL30C.glPixelStorei(GL30C.GL_UNPACK_ALIGNMENT, 2 - (x & 1));
            GL30C.glTexImage2D(GL30C.GL_TEXTURE_2D, 0, format, x, y, 0, format, GL30C.GL_UNSIGNED_BYTE, image);

            STBImage.stbi_image_free(image);
        }

        public void bind() {
            if (this == empty) {
                GL30C.glUniform1i(shaderIMode, SOLID);
            } else {
                GL30C.glUniform1i(shaderIMode, TEXTURE);
                GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, id);
            }
        }
    }

    private class Glyph extends Texture {
        public float[] vertex;
        public int advance;

        public Glyph(char ch, int size) {
            super(GL30C.glGenTextures());

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer a = stack.mallocInt(1);
                IntBuffer b = stack.mallocInt(1);
                IntBuffer c = stack.mallocInt(1);
                IntBuffer d = stack.mallocInt(1);
                GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, id);
                GL30C.glPixelStorei(GL30C.GL_UNPACK_ALIGNMENT, 1);
                float scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, fontHeight * size);
                ByteBuffer bitmap = STBTruetype.stbtt_GetCodepointBitmap(
                        fontInfo, scale, scale, ch, a, b, c, d);
                GL30C.glTexImage2D(GL30C.GL_TEXTURE_2D, 0, GL30C.GL_ALPHA, a.get(0), b.get(0),
                        0, GL30C.GL_ALPHA, GL30C.GL_UNSIGNED_BYTE, bitmap);
                if (bitmap != null) STBTruetype.stbtt_FreeBitmap(bitmap);
                GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MAG_FILTER, GL30C.GL_LINEAR);
                GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_MIN_FILTER, GL30C.GL_LINEAR);
                GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_S, GL30C.GL_CLAMP_TO_EDGE);
                GL30C.glTexParameteri(GL30C.GL_TEXTURE_2D, GL30C.GL_TEXTURE_WRAP_T, GL30C.GL_CLAMP_TO_EDGE);
                STBTruetype.stbtt_GetCodepointBitmapBox(fontInfo, ch, scale, scale, a, b, c, d);
                float x0f = a.get(0);
                float x1f = c.get(0);
                float y0f = b.get(0);
                float y1f = d.get(0);
                vertex = new float[]{x0f, y0f, x1f, y0f, x1f, y1f, x0f, y1f};
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, ch, a, b);
                advance = (int) Math.ceil(a.get(0) * scale / size);
            }
        }

        @Override
        public void bind() {
            GL30C.glUniform1i(shaderIMode, ALPHA);
            GL30C.glBindTexture(GL30C.GL_TEXTURE_2D, id);
        }
    }
}
