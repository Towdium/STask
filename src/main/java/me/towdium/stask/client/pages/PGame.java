package me.towdium.stask.client.pages;

import me.towdium.stask.client.Animator;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Painter.Resource;
import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.*;
import me.towdium.stask.logic.Algorithm;
import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Event.EGame.*;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;
import me.towdium.stask.logic.algorithms.AListHLEFT;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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
    WButton start = new WButtonIcon(120, 30, Resource.START).setListener(i -> {
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
        BUS.subscribe(Event.EGame.Finish.class, this, i ->
                Widget.page().overlay(new Center(new Success(), Success.WIDTH, Success.HEIGHT)));
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
        put(new WSchedule(x - 300, game), 0, y - WSchedule.HEIGHT - WHistory.HEIGHT - 5);
        put(new WCluster(game), 100, 100);
        put(new WHistory(x - 300, game), 0, y - WHistory.HEIGHT);
        put(schedule, x - 140, y - 250);
        put(start, x - 140, y - 210);
        put(reset, x - 140, y - 170);
        put(pause, x - 140, y - 130);
        put(step, x - 140, y - 90);
        put(leave, x - 140, y - 50);
        if (tutorial != null) put(tutorial, x - WTutorial.WIDTH - 20, 20);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        int sec = game.getSeconds();
        String s = String.format("%02d:%02d", sec / 60, sec % 60);
        p.drawText(s, 0, Painter.fontAscent);
    }

    class Success extends WContainer {
        static final int WIDTH = 300;
        static final int HEIGHT = 100;
        Animator animator = new Animator();

        public Success() {
            put(new WOverlay(WIDTH, HEIGHT), 0, 0);
            List<Integer> aims = game.getAims();
            int len = aims.size() * 80 - 30;
            Runnable animate = () -> {
            };
            for (int i = aims.size() - 1; i >= 0; i--) {
                int a = aims.get(i);
                String s = String.format("%02d:%02d", a / 60, a % 60);
                WText w = new WText(0xFFFFFF, s);
                Runnable r = animate;
                int c = game.getSeconds() > a ? 0xFF8888 : 0x88FF88;
                animate = () -> animator.addColor(0xFFFFFF, c, 500,
                        new Animator.FBezier(0, 0), j -> w.color = j, r);
                put(w, (WIDTH - len) / 2 + i * 80, 75);
            }
            animate.run();
        }

        @Override
        public void onRefresh(Vector2i mouse) {
            animator.tick();
            super.onRefresh(mouse);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            p.drawTextCenter("Level completed", WIDTH / 2, 20 + Painter.fontAscent);
        }
    }
}
