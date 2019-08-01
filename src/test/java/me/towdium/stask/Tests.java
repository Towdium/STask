package me.towdium.stask;

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
        double a = 1. / 64 / 3;
        double b = 0;
        for (int i = 0; i < 64 * 3; i++) {
            b += a;
        }
        out.println(b >= 1);
    }
}
