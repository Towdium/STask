package me.towdium.stask.utils.wrap;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
public class Lazy<T> {
    T value;
    Supplier<T> supplier;

    public Lazy(Supplier<T> s) {
        supplier = s;
    }

    public T get() {
        if (value == null) value = Objects.requireNonNull(supplier.get());
        return value;
    }
}
