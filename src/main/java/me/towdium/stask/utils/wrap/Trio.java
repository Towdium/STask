package me.towdium.stask.utils.wrap;

/**
 * Author: Towdium
 * Date: 03/06/19
 */
public class Trio<T, U, V> {
    public T a;
    public U b;
    public V c;

    public Trio(T a, U b, V c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public int hashCode() {
        int ha = a.hashCode();
        ha = (ha << 10) ^ (ha >> 10);
        int hb = b.hashCode();
        hb = (hb << 20) ^ (hb >> 20);
        return ha ^ hb ^ c.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair p = (Pair) obj;
            return a.equals(p.a) && b.equals(p.b);
        }
        return false;
    }
}
