package me.towdium.stask.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
@NotNull
public class Pair<K, V> {
    public K one;
    public V two;

    public Pair(K one, V two) {
        this.one = one;
        this.two = two;
    }

    @Override
    public int hashCode() {
        int hash = one.hashCode();
        return (hash << 16) ^ (hash >> 16) ^ two.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair p = (Pair) obj;
            return one.equals(p.one) && two.equals(p.two);
        }
        return false;
    }

    public Pair<K, V> setOne(K one) {
        this.one = one;
        return this;
    }

    public Pair<K, V> setTwo(V two) {
        this.two = two;
        return this;
    }
}
