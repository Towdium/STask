package me.towdium.stask.utils;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 * Author: Towdium
 * Date: 04/03/19
 */
public class Cache<K, V> {
    HashMap<K, V> data = new HashMap<>();
    Function<K, V> generator;

    public Cache(Function<K, V> generator) {
        this.generator = generator;
    }

    public V get(K key) {
        V ret = data.get(key);
        if (ret == null) {
            ret = generator.apply(key);
            Objects.requireNonNull(ret);
            data.put(key, ret);
        }
        return ret;
    }
}
