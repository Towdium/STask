package me.towdium.stask;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import javax.annotation.ParametersAreNonnullByDefault;

import static java.lang.System.out;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
@ParametersAreNonnullByDefault
public class Tests {
    @Test
    public void test() {
        Matrix4f m = new Matrix4f()
                .lookAlong(0, 0, 1, 0, -1, 0);
        out.println(m.transform(new Vector4f(1, 1, 1, 1)));
    }
}
