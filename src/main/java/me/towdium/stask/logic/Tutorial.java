package me.towdium.stask.logic;

import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.WContainer;
import me.towdium.stask.client.widgets.WPanel;
import me.towdium.stask.logic.tutorials.TIntro;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public abstract class Tutorial {
    public static final int WIDTH = 300;
    public static final int HEIGHT = 400;

    public static Tutorial get(String id, Game g) {
        return Registry.loaders.get(id).apply(g);
    }

    protected static Widget frame(Widget w) {
        WContainer ret = new WContainer();
        ret.put(new WPanel(WIDTH, HEIGHT), 0, 0);
        ret.put(w, 0, 0);
        return ret;
    }

    public abstract Widget widget();

    public static class Registry {
        static HashMap<String, Function<Game, Tutorial>> loaders = new HashMap<>();

        static {
            loaders.put("1-1", TIntro::new);
        }
    }


}
