package me.towdium.stask.client.pages;

import me.towdium.stask.client.*;
import me.towdium.stask.client.widgets.*;
import me.towdium.stask.logic.Algorithm;
import me.towdium.stask.logic.Cluster;
import me.towdium.stask.logic.Event.EGame;
import me.towdium.stask.logic.Event.EGame.*;
import me.towdium.stask.logic.Event.ETask;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 18/06/19
 */
@ParametersAreNonnullByDefault
public class PGame extends Page.Impl {
    public static final int CONTROL_WIDTH = 200;

    PWrapper root;
    Page parent;
    Game game;
    WGraphs graphs;
    Widget floating;
    WText timer = new WText(0xFFFFFF, "");
    WText comm;
    Tutorial tutorial;
    WList algorithms;

    public PGame(PWrapper r, Page p, Game g) {
        this(r, p, g, false);
    }

    public PGame(PWrapper r, Page p, Game g, boolean algos) {
        root = r;
        parent = p;
        game = g;
        graphs = new WGraphs(game, 0, 0);
        Cluster cluster = game.getCluster();
        comm = new WText(0xFFFFFF, cluster.getComm() == 0 ? "" : "x" + cluster.getComm());
        tutorial = game.getTutorial();
        if (tutorial != null) {
            tutorial.activate();
            floating = tutorial.widget();
        }
        if (algos) {
            Map<String, Algorithm> as = Algorithm.Registry.get();
            List<String> ss = new ArrayList<>(as.keySet());
            Collections.sort(ss);
            algorithms = new WList(ss, 200, 400);
            algorithms.setListener((w, o, n) -> {
                Algorithm a = Algorithm.Registry.get(w.get(n));
                game.reset();
                BUS.post(new EGame.Reset());
                a.run(game.getGraphs(), game.getCluster(), game.getSchedule());
            });
        }
        BUS.gate(ETask.Pick.class, this, i -> !game.isStatic() || game.getCount() == 0);
        BUS.subscribe(EGame.Finish.class, this, i -> {
            game.pause();
            if (game.getAims() == null) return;
            Widget.page().overlay(new Center(new Complete(), Complete.WIDTH, Complete.HEIGHT));
        });
    }

    @Override
    public void onRemove() {
        super.onRemove();
        BUS.cancel(this);
        if (tutorial != null) tutorial.deactivate();
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        game.tick();
        super.onRefresh(mouse);
        int sec = game.getSeconds();
        timer.str = String.format("%02d:%02d", sec / 60, sec % 60);
    }

    @Override
    protected void onLayout(int x, int y) {
        WStatus.text = "";
        clear();
        graphs.setX(x - 300);
        graphs.setY(y - WSchedule.HEIGHT - WHistory.HEIGHT - 10);
        put(graphs, WCluster.WIDTH + 45, 0);
        put(new WSchedule(x - CONTROL_WIDTH - 5, game), 0, y - WSchedule.HEIGHT - WHistory.HEIGHT - 5);
        put(new WCluster(game), 20, (y - WHistory.HEIGHT - WSchedule.HEIGHT - 92 - WCluster.HEIGHT) / 2 + 82);
        put(new WHistory(x - CONTROL_WIDTH - 5, game), 0, y - WHistory.HEIGHT);
        put(new Control(), x - CONTROL_WIDTH - 5, y - 190);
        put(new WStatus(), x - CONTROL_WIDTH + 5, y - Painter.fontHeight - 3);
        put(timer, 35, 5);
        put(comm, 35, 46);
        put(new WTooltip.Impl(WCluster.WIDTH + 40, 36, "Game timer"), 0, 0);
        put(new WTooltip.Impl(WCluster.WIDTH + 40, 36, "Comm. speed"), 0, 41);
        put(new WRectangle(WCluster.WIDTH + 40, 5, Colour.SLICE), 0, 36);
        put(new WRectangle(WCluster.WIDTH + 40, 5, Colour.SLICE), 0, 77);
        put(new WRectangle(CONTROL_WIDTH, 5, Colour.SLICE), x - CONTROL_WIDTH, y - 33);
        put(new WRectangle(5, y - WSchedule.HEIGHT - WHistory.HEIGHT - 10, Colour.SLICE), WCluster.WIDTH + 40, 0);
        put(new WRectangle(x - CONTROL_WIDTH - 5, 5, Colour.SLICE), 0, y - WHistory.HEIGHT - 5);
        put(new WRectangle(x, 5, Colour.SLICE), 0, y - WHistory.HEIGHT - WSchedule.HEIGHT - 10);
        put(new WRectangle(5, WHistory.HEIGHT + WSchedule.HEIGHT + 10, Colour.SLICE), x - CONTROL_WIDTH - 5, y - WHistory.HEIGHT - WSchedule.HEIGHT - 10);
        if (floating != null) put(floating, x - WTutorial.WIDTH - 20, 20);
        if (algorithms != null) put(algorithms, x - 240, 30);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        p.drawResource(Resource.CLOCK, 2, 2);
        p.drawResource(Resource.COMM, 2, 44);
        if (game.getCluster().getComm() == 0)
            p.drawResource(Resource.INFINITY, 20, 45);
    }

    class Complete extends WContainer {
        static final int WIDTH = 300;
        static final int HEIGHT = 100;
        Animator animator = new Animator();

        public Complete() {
            put(new WOverlay(WIDTH, HEIGHT), 0, 0);
            List<Integer> aims = game.getAims();
            Objects.requireNonNull(aims, "Internal error");
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
                put(w, (WIDTH - len) / 2 + i * 80, 55);
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

    class Control extends WContainer {
        boolean step = false;
        WButton start = new WButtonIcon(48, 48, Resource.START, "Start");
        WButton pause = new WButtonIcon(48, 48, Resource.PAUSE, "Pause");
        WButton plus = new WButtonIcon(36, 36, Resource.PLUS, "Speed up");
        WButton minus = new WButtonIcon(36, 36, Resource.MINUS, "Speed down");
        WButton leave = new WButtonIcon(48, 48, Resource.CLOSE, "Quit").setListener(i -> {
            BUS.post(new Leave());
            root.display(() -> parent);
        });
        WButton reset = new WButtonIcon(48, 48, Resource.RESET, "Reset").setListener(i -> {
            if (!BUS.attempt(new Reset())) return;
            game.reset();
            BUS.post(new Reset());
        });

        public Control() {
            put(reset, 80, 50);
            put(leave, 140, 50);
            put(plus, 100, 0);
            put(minus, 150, 0);
            put(new WRectangle(65, 36, Colour.DISABLED), 20, 0);
            refresh();

            BUS.subscribe(EGame.Finish.class, this, i -> finish());
            BUS.subscribe(EGame.Failed.class, this, i -> finish());
            BUS.subscribe(EGame.class, this, i -> refresh());
            BUS.subscribe(EGame.Reset.class, this, i -> {
                start.setListener(this::start);
                if (game.isStatic()) pause.setListener(this::pause);
                remove(pause);
                put(start, 20, 50);
            });

            start.setListener(this::start);
            if (game.isStatic()) pause.setListener(this::pause);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            int speed = game.getSpeed();
            String str = speed >= 4 ? Integer.toString(speed / 4) : "1/" + (4 / speed);
            p.drawText("x" + (step ? 0 : str), 27, 5 + Painter.fontAscent);
        }

        private void minus(WButton i) {
            if (!BUS.attempt(new Speed())) return;
            int s = game.getSpeed();
            if (s != 1) game.setSpeed(s / 2);
            else step = true;
            if (step) {
                game.pause();
                BUS.post(new Pause());
            }
            BUS.post(new Speed());
        }

        private void plus(WButton i) {
            if (!BUS.attempt(new Speed())) return;
            if (step) step = false;
            else game.setSpeed(2 * game.getSpeed());
            BUS.post(new Speed());

        }

        private void start(WButton i) {
            if (step) {
                if (!BUS.attempt(new Step())) return;
                game.start();
                game.tick();
                game.pause();
                BUS.post(new Step());
            } else {
                if (!BUS.attempt(new Start())) return;
                game.start();
                BUS.post(new Start());
            }
        }

        private void pause(WButton i) {
            if (!BUS.attempt(new Pause())) return;
            game.pause();
            BUS.post(new Pause());
        }

        private void finish() {
            start.setListener(null);
            pause.setListener(null);
        }

        private void refresh() {
            plus.setListener(game.getSpeed() < 64 ? this::plus : null);
            minus.setListener((game.isStatic() ? !step : game.getSpeed() > 1) ? this::minus : null);
            if (game.isRunning()) {
                remove(start);
                put(pause, 20, 50);
            } else {
                remove(pause);
                put(start, 20, 50);
            }
        }
    }
}
