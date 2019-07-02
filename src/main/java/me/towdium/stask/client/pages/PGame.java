package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.widgets.*;
import me.towdium.stask.logic.Algorithm;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.algorithms.AListHLEFT;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 18/06/19
 */
@ParametersAreNonnullByDefault
public class PGame extends Page.Impl {
    PWrapper root;
    Page parent;
    Game game;
    WGraphs graphs;

    public PGame(PWrapper r, Page p, Game g) {
        root = r;
        parent = p;
        game = g;
        graphs = new WGraphs(game, 0);
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        super.onRefresh(mouse);
        game.tick();
    }

    @Override
    protected void onLayout(int x, int y) {
        clear();
        graphs.setX(x - 300);
        put(graphs, 300, 0);
        put(new WSchedule(x - 100, 100, game), 0, y - 200);
        put(new WCluster(game), 100, 100);
        put(new WHistory(x - 100, game), 0, y - 100);
        put(new WButtonText(120, 30, "schedule").setListener(i -> {
            Algorithm a = new AListHLEFT();
            a.run(game.getGraphs(), game.getCluster(), game.getSchedule());
        }), x - 140, y - 250);
        put(new WButtonText(120, 30, "start").setListener(i -> game.start()), x - 140, y - 210);
        put(new WButtonText(120, 30, "reset").setListener(i -> game.reset()), x - 140, y - 170);
        put(new WButtonText(120, 30, "pause").setListener(i -> game.pause()), x - 140, y - 130);
        put(new WButtonText(120, 30, "step").setListener(i -> {
            game.start();
            game.tick();
            game.pause();
        }), x - 140, y - 90);
        put(new WButtonText(120, 30, "leave").setListener(i -> root.display(() -> parent)), x - 140, y - 50);
    }
}
