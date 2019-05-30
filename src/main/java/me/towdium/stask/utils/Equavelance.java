package me.towdium.stask.utils;

/**
 * Author: Towdium
 * Date: 29/05/19
 */
public class Equavelance<T> {
    T v;

    public Equavelance(T v) {
        this.v = v;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(v);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Equavelance && v == ((Equavelance) obj).v;
    }
}
