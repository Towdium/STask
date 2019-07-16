package me.towdium.stask.logic;

import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.tutorials.TIntro;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public interface Tutorial {
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
        int index = 0;
        WTutorial widget = new WTutorial();
        Event.Filter filter = new Event.Filter(this);

        public Impl(Game g) {
            game = g;
            Event.Bus.BUS.subscribe(Event.class, this, i -> {
                int next = index + 1;
                if (stages.get(index).pass() && next < stages.size()) update(next);
            });
        }

        public void initialize(Stage... ss) {
            stages = Arrays.asList(ss);
            update(0);
        }

        private void update(int i) {
            index = i;
            Stage s = stages.get(i);
            s.activate(widget);
            filter.update(s::test);
        }

        @Override
        public Widget widget() {
            return widget;
        }

        public interface Stage {
            boolean pass();

            boolean test(Event e);

            void activate(WTutorial w);
        }
    }
}
