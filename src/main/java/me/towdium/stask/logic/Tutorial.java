package me.towdium.stask.logic;

import me.towdium.stask.utils.Cache;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public class Tutorial {
    static HashMap<String, Supplier<Tutorial>> loaders = new HashMap<>();
    static Cache<String, Tutorial> cache = new Cache<>(i -> loaders.get(i).get());

    static {
    }

    public static Tutorial get(@Nullable String id) {
        return id == null ? null : cache.get(id);
    }
}
