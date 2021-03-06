package me.towdium.stask.utils.wrap;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
@ParametersAreNonnullByDefault
public class Pair<K, V> {
    public K a;
    public V b;

    public Pair(K a, V b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        int hash = a.hashCode();
        return (hash << 16) ^ (hash >> 16) ^ b.hashCode();
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
