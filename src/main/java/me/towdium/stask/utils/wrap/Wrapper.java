package me.towdium.stask.utils.wrap;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public class Wrapper<T> {
    public T v;

    public Wrapper() {
        //noinspection ConstantConditions
        this(null);
    }

    public Wrapper(T value) {
        this.v = value;
    }

    public Optional<T> toOptional() {
        return Optional.ofNullable(v);
    }
}
