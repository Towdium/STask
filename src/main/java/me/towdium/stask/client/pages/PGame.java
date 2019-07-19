package me.towdium.stask.client.pages;

import me.towdium.stask.client.Animator;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.Painter.Resource;
import me.towdium.stask.client.Widget;
import me.towdium.stask.client.widgets.*;
import me.towdium.stask.logic.Event;
import me.towdium.stask.logic.Event.EGame.*;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Tutorial;
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
    public static final int CONTROL_WIDTH = 200;

    PWrapper root;
    Page parent;
    Game game;
    WGraphs graphs;
    Widget tutorial;

    public PGame(PWrapper r, Page p, Game g) {
        root = r;
        parent = p;
        game = g;
        graphs = new WGraphs(game, 0, 0);
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
        graphs.setY(y - WSchedule.HEIGHT - WHistory.HEIGHT - 10);
        put(graphs, 300, 0);
        put(new WSchedule(x - CONTROL_WIDTH - 5, game), 0, y - WSchedule.HEIGHT - WHistory.HEIGHT - 5);
        put(new WCluster(game), 20, 80);
        put(new WHistory(x - CONTROL_WIDTH - 5, game), 0, y - WHistory.HEIGHT);
        put(new Control(), x - CONTROL_WIDTH - 5, y - 170);
        put(new WRectangle(5, y - WSchedule.HEIGHT - WHistory.HEIGHT - 10, 0xCCCCCC), WCluster.WIDTH + 40, 0);
        put(new WRectangle(x - CONTROL_WIDTH - 5, 5, 0xCCCCCC), 0, y - WHistory.HEIGHT - 5);
        put(new WRectangle(x, 5, 0xCCCCCC), 0, y - WHistory.HEIGHT - WSchedule.HEIGHT - 10);
        put(new WRectangle(5, WHistory.HEIGHT + WSchedule.HEIGHT + 10, 0xCCCCCC), x - CONTROL_WIDTH - 5, y - WHistory.HEIGHT - WSchedule.HEIGHT - 10);
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

    class Control extends WContainer {
        boolean step = false;
        WButton start = new WButtonIcon(48, 48, Resource.START);
        WButton reset = new WButtonIcon(48, 48, Resource.RESET).setListener(i -> {
            if (!BUS.attempt(new Reset())) return;
            game.reset();
            BUS.post(new Reset());
        });
        WButton pause = new WButtonIcon(48, 48, Resource.PAUSE);
        WButton minus = new WButtonIcon(36, 36, Resource.MINUS).setListener(game.isStatic() ? this::minus : null);
        WButton plus = new WButtonIcon(36, 36, Resource.PLUS).setListener(game.isStatic() ? this::plus : null);
        WButton leave = new WButtonIcon(48, 48, Resource.CLOSE).setListener(i -> {
            BUS.post(new Leave());
            root.display(() -> parent);
        });

        public Control() {
            put(start, 20, 50);
            put(reset, 80, 50);
            put(leave, 140, 50);
            put(plus, 100, 0);
            put(minus, 150, 0);
            put(new WRectangle(65, 36, 0x333333), 20, 0);

            start.setListener(i -> {
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
                    remove(i);
                    put(pause, 20, 50);
                }
            });

            pause.setListener(i -> {
                if (!BUS.attempt(new Pause())) return;
                game.pause();
                BUS.post(new Pause());
                remove(i);
                put(start, 20, 50);
            });
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            super.onDraw(p, mouse);
            p.drawText("x" + (step ? 0 : game.getSpeed()), 30, 5 + Painter.fontAscent);
        }

        private void minus(WButton i) {
            if (!BUS.attempt(new SpeedDown())) return;
            int s = game.getSpeed();
            if (s != 1) game.setSpeed(s / 2);
            else step = true;
            BUS.post(new SpeedDown());
            if (step) i.setListener(null);
            if (game.getSpeed() < 16) plus.setListener(this::plus);
        }

        private void plus(WButton i) {
            if (!BUS.attempt(new SpeedUp())) return;
            if (step) step = false;
            else game.setSpeed(2 * game.getSpeed());
            BUS.post(new SpeedUp());
            if (game.getSpeed() >= 16) i.setListener(null);
            if (!step) minus.setListener(this::minus);
        }
    }
}
