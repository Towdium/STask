package me.towdium.stask.logic;

import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.tutorials.TIntro;
import me.towdium.stask.utils.Toggleable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public interface Tutorial extends Toggleable {
    static Tutorial get(String id, Game g) {
        return Registry.loaders.get(id).apply(g);
    }

    Widget widget();

    class Registry {
        static HashMap<String, Function<Game, Tutorial>> loaders = new HashMap<>();

        static {
            loaders.put("1-1", TIntro::new);
        }
    }

    class Impl implements Tutorial {
        protected Game game;
        List<Stage> stages;
        protected WTutorial widget = new WTutorial();
        int index = -1;

        public Impl(Game g) {
            game = g;
        }

        @Override
        public void activate() {
            update(0);
        }

        public void deactivate() {
            BUS.cancel(this);
            if (index != -1) stages.get(index).deactivate();
        }

        public void initialize(Stage... ss) {
            stages = Arrays.asList(ss);
        }

        private void update(int i) {
            if (index != -1) stages.get(index).deactivate();
            stages.get(i).activate();
            index = i;
        }

        @Override
        public Widget widget() {
            return widget;
        }

        protected void pass() {
            int next = index + 1;
            if (next < stages.size()) update(next);
            else {
                if (index != -1) stages.get(index).deactivate();
                index = -1;
                widget.clear();
            }
        }

        public interface Stage extends Toggleable {
        }
    }
}
