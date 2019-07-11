package me.towdium.stask.client.pages;

import me.towdium.stask.client.Page;
import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.*;
import me.towdium.stask.logic.Algorithm;
import me.towdium.stask.logic.Event.EGame.*;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;
import me.towdium.stask.logic.algorithms.AListHLEFT;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;

import static me.towdium.stask.logic.Event.Bus.BUS;

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
    Widget tutorial;
    WButton start = new WButtonText(120, 30, "start").setListener(i -> {
        if (!BUS.attempt(new Start())) return;
        game.start();
        BUS.post(new Start());
    });
    WButton schedule = new WButtonText(120, 30, "schedule").setListener(i -> {
        Algorithm a = new AListHLEFT();
        a.run(game.getGraphs(), game.getCluster(), game.getSchedule());
    });
    WButton reset = new WButtonText(120, 30, "reset").setListener(i -> {
        if (!BUS.attempt(new Reset())) return;
        game.reset();
        BUS.post(new Reset());
    });
    WButton pause = new WButtonText(120, 30, "pause").setListener(i -> {
        if (!BUS.attempt(new Pause())) return;
        game.pause();
        BUS.post(new Pause());
    });
    WButton step = new WButtonText(120, 30, "step").setListener(i -> {
        if (!BUS.attempt(new Step())) return;
        game.start();
        game.tick();
        game.pause();
        BUS.post(new Step());
    });
    WButton leave = new WButtonText(120, 30, "leave").setListener(i -> {
        if (!BUS.attempt(new Leave())) return;
        BUS.post(new Leave());
        root.display(() -> parent);
    });

    public PGame(PWrapper r, Page p, Game g) {
        root = r;
        parent = p;
        game = g;
        graphs = new WGraphs(game, 0);
        Tutorial t = game.getTutorial();
        if (t != null) tutorial = t.widget();
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
        put(new WSchedule(x - 300, 100, game), 0, y - 200);
        put(new WCluster(game), 100, 100);
        put(new WHistory(x - 300, game), 0, y - 100);
        put(schedule, x - 140, y - 250);
        put(start, x - 140, y - 210);
        put(reset, x - 140, y - 170);
        put(pause, x - 140, y - 130);
        put(step, x - 140, y - 90);
        put(leave, x - 140, y - 50);
        if (tutorial != null) put(tutorial, x - Tutorial.WIDTH - 20, 20);
    }
}
