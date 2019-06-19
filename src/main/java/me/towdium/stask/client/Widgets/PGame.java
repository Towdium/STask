package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Page;
import me.towdium.stask.logic.Game;

/**
 * Author: Towdium
 * Date: 18/06/19
 */
public class PGame extends Page.Simple {
    Game game;

    public PGame(Game g) {
        game = g;
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        put(new WGraph(400, y - 200, game), 200, 0);
        put(new WAllocation(x - 100, 100, game), 0, y - 200);
        put(new WGame(game), 100, 100);
        put(new WHistory(x - 100, 100, game), 0, y - 100);
        put(new WButtonText(80, 30, "start").setListener(i -> game.start()), x - 100, y - 170);
        put(new WButtonText(80, 30, "reset").setListener(i -> game.reset()), x - 100, y - 130);
        put(new WButtonText(80, 30, "pause").setListener(i -> game.pause()), x - 100, y - 90);
        put(new WButtonText(80, 30, "step").setListener(i -> {
            game.start();
            game.tick();
            game.pause();
        }), x - 100, y - 50);
    }
}
