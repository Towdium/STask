package me.towdium.stask;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
public class Tests {


    @Test
    public void test() {
        Matrix4f ortho = new Matrix4f().ortho(-1, 1, -1, 1, -200, 0).lookAlong(0, 0, -1, 0, 1, 0);
        Matrix4f transp = new Matrix4f().translate(0, 0, 10);
        System.out.println(new Vector4f(0, 0, 0, 1).mulProject(ortho));
        System.out.println(ortho.transform(new Vector4f(0, 0, 100, 1)));
        System.out.println(new Vector4f(0, 0, 0, 1).mulProject(transp).mulProject(ortho));
    }
}
