package me.towdium.stask;

import org.junit.jupiter.api.Test;

import java.text.BreakIterator;

/**
 * Author: Towdium
 * Date: 06/03/19
 */
public class Tests {
    @Test
    public static void test() {
        BreakIterator it = BreakIterator.getLineInstance();
        it.setText("Here is some text");
        //for (int i = it.next(); i != )
    }
}
