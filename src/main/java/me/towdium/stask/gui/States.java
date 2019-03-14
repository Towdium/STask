package me.towdium.stask.gui;

import me.towdium.stask.utils.Quad;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30C;

import java.io.Closeable;
import java.nio.FloatBuffer;
import java.util.Stack;

/**
 * Author: Towdium
 * Date: 13/03/19
 */
public class States {
    static Stack<Matrix4f> matrices = new Stack<>();
    static Stack<Quad> masks = new Stack<>();
    static Stack<Integer> colors = new Stack<>();
    static Stack<Boolean> priority = new Stack<>();

    static {
        matrices.push(new Matrix4f().translate(0, 0, 0));
        SMatrix.update();
        colors.push(0xFFFFFF);
        colorUpdate();
        priority.push(false);
        priorityUpdate();
        maskUpdate();
    }

    public static SMatrix matrix() {
        return matrix(0);
    }

    public static SMatrix matrix(int i) {
        matrices.push(new Matrix4f(matrices.get(matrices.size() - 1 - i)));
        if (i != 0) SMatrix.update();
        return new SMatrix();
    }

    public static State mask(int xp, int yp, int xs, int ys) {
        Quad quad = new Quad(xp, yp, xs, ys).transformed(matrices.peek());
        masks.push(masks.isEmpty() ? quad : new Quad(masks.peek()).intersect(quad));
        maskUpdate();
        return () -> {
            masks.pop();
            maskUpdate();
        };
    }

    public static State color(int color) {
        colors.push(color);
        colorUpdate();
        return () -> {
            colors.pop();
            colorUpdate();
        };
    }

    public static State priority(boolean prioritized) {
        priority.push(prioritized);
        priorityUpdate();
        return () -> {
            priority.pop();
            priorityUpdate();
        };
    }

    private static void colorUpdate() {
        int c = colors.peek();
        float a = 1 - (c >> 24 & 255) / 255.0F;
        float r = (c >> 16 & 255) / 255.0F;
        float g = (c >> 8 & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        GL30C.glUniform4f(Painter.shaderVColor, r, g, b, a);
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
        GL30C.glUniform4fv(Painter.shaderVClip, fb);
    }

    private static void priorityUpdate() {
        if (priority.peek()) {
            GL30C.glStencilFunc(GL30C.GL_ALWAYS, 1, 0xFF);
            GL30C.glStencilOp(GL30C.GL_REPLACE, GL30C.GL_REPLACE, GL30C.GL_REPLACE);
        } else {
            GL30C.glStencilFunc(GL30C.GL_NOTEQUAL, 1, 0xFF);
            GL30C.glStencilOp(GL30C.GL_KEEP, GL30C.GL_KEEP, GL30C.GL_KEEP);
        }
    }

    @FunctionalInterface
    public interface State extends Closeable {
        @Override
        void close();
    }

    public static class SMatrix implements State {
        private SMatrix() {
        }

        private static void update() {
            FloatBuffer fb = BufferUtils.createFloatBuffer(16);
            GL30C.glUniformMatrix4fv(Painter.shaderMModel, false, matrices.peek().get(fb));
        }

        public void translate(float x, float y) {
            translate(x, y, 0);
        }

        public void translate(float x, float y, float z) {
            matrices.peek().translate(x, y, z);
            update();
        }

        @Override
        public void close() {
            matrices.pop();
            update();
        }
    }
}
