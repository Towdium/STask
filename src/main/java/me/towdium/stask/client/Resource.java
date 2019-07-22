package me.towdium.stask.client;

/**
 * Author: Towdium
 * Date: 20/07/19
 */
public class Resource {
    public static final Resource START = new Resource("1", 0, 0, 120, 120, 0.4f);
    public static final Resource PAUSE = new Resource("1", 120, 0, 120, 120, 0.4f);
    public static final Resource RESET = new Resource("1", 240, 0, 120, 120, 0.4f);
    public static final Resource CLOSE = new Resource("1", 360, 0, 120, 120, 0.4f);
    public static final Resource PLUS = new Resource("1", 480, 0, 120, 120, 0.3f);
    public static final Resource MINUS = new Resource("1", 600, 0, 120, 120, 0.3f);
    public static final Resource CURSOR = new Resource("1", 0, 120, 60, 60, 0.5f);
    public static final Resource CLOCK = new Resource("1", 60, 120, 60, 60, 0.5f);
    public static final Resource CLASS = new Resource("1", 120, 120, 60, 60, 0.5f);
    public static final Resource PROCESSOR = new Resource("1", 180, 120, 60, 60, 0.5f);
    public static final Resource COMM = new Resource("1", 240, 120, 60, 60, 0.5f);
    public static final Resource INFINITY = new Resource("1", 300, 120, 120, 60, 0.5f);
    public static final Resource RIGHT = new Resource("1", 420, 120, 60, 60, 0.5f);
    public static final Resource LEFT = new Resource("1", 480, 120, 60, 60, 0.5f);
    public static final Resource SPEED = new Resource("1", 540, 120, 60, 60, 0.5f);
    public static final Resource SPECIAL = new Resource("1", 600, 120, 60, 60, 0.5f);

    public final String id;
    public final int xp, yp, xs, ys;
    public final float mul;

    public Resource(String id, int xp, int yp, int xs, int ys, float mul) {
        this.id = id;
        this.xp = xp;
        this.yp = yp;
        this.xs = xs;
        this.ys = ys;
        this.mul = mul;
    }

    public Resource(String id, int xp, int yp, int xs, int ys) {
        this(id, xp, yp, xs, ys, 0.5f);
    }
}
