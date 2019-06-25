package me.towdium.stask.utils;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 13/03/19
 */
@ParametersAreNonnullByDefault
public class Quad {
    public Vector4f a, b;

    public Quad(int xp, int yp, int xs, int ys) {
        if (xs < 0 || ys < 0) throw new RuntimeException("Size cannot be negative");
        a = new Vector4f(xp, yp, -4096, 1);
        b = new Vector4f(xp + xs, yp + ys, 4096, 1);
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

    public static boolean inside(@Nullable Vector2i v, float x1, float y1, float x2, float y2) {
        return v != null && v.x > x1 && v.x < x2 && v.y > y1 && v.y < y2;
    }

    public static boolean inside(@Nullable Vector2i v, int x2, int y2) {
        return inside(v, 0, 0, x2, y2);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean inside(@Nullable Vector2i v) {
        return inside(v, a.x, a.y, b.x, b.y);
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