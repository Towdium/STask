package me.towdium.stask.logic;

import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.WTutorial;
import me.towdium.stask.logic.tutorials.*;
import me.towdium.stask.utils.Toggleable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
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
            loaders.put("1-1", TS1L1::new);
            loaders.put("1-2", TS1L2::new);
            loaders.put("1-3", TS1L3::new);
            loaders.put("1-4", TS1L4::new);
            loaders.put("1-5", TS1L5::new);
            loaders.put("1-6", TS1L6::new);
        }
    }

    class Impl implements Tutorial {
        protected WTutorial widget;
        protected Game game;

        public Impl(Game game) {
            this.game = game;
        }

        @Override
        public Widget widget() {
            return widget;
        }

        @Override
        public void activate() {
            widget = new WTutorial();
        }

        @Override
        public void deactivate() {
            BUS.cancel(this);
        }

        protected void schedule(String task, String processor) {
            Graph.Task t = game.getInitials().iterator().next().getTask(task);
            Cluster.Processor p = game.cluster.getProcessor(processor);
            game.schedule.allocate(t, p);
        }
    }
}
