package me.towdium.stask;

import me.towdium.stask.utils.Circulator;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
public class Tests {
    @Test
    public void test() {
        Circulator<Integer> c = new Circulator<>();
        c.add(1);
        c.add(2);
        Iterator<Integer> it = c.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
            it.remove();
        }
    }
}
