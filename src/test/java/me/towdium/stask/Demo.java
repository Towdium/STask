package me.towdium.stask;

import me.towdium.stask.client.Window;
import me.towdium.stask.client.pages.PMain;
import me.towdium.stask.client.pages.PWrapper;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 19/05/19
 */
@ParametersAreNonnullByDefault
public class Demo {

    public static void main(String[] args) {
        PWrapper wrapper = new PWrapper();

        try (Window w = new Window("Test Graph", wrapper)) {
            w.display();
            wrapper.display(() -> new PMain(wrapper, w));
            while (!w.isFinished()) w.tick();
        }
    }
}
