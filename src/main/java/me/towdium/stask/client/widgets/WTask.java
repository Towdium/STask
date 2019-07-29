package me.towdium.stask.client.widgets;

import me.towdium.stask.client.*;
import me.towdium.stask.logic.Event.ETask;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.logic.Schedule;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 11/06/19
 */
@ParametersAreNonnullByDefault
public class WTask extends WCompose {
    public static final int WIDTH = 100;
    public static final int HEIGHT = 56;
    Graph.Task task;
    Schedule schedule;
    Animator animator = new Animator();
    Animator.Entry entry = null;
    Game game;
    int color;
    State state;

    public WTask(Graph.Task t, Game g) {
        compose(new WFocus.Impl(WIDTH, HEIGHT) {
            @Nullable
            @Override
            public Graph.Work onFocus() {
                return task;
            }

            @Override
            public boolean onTest(@Nullable Vector2i mouse) {
                return WDrag.isSending(WTask.this) || super.onTest(mouse);
            }
        });
        compose(new WDrag.Impl(WIDTH, HEIGHT) {
            @Nullable
            @Override
            public Object onStarting() {
                if (BUS.attempt(new ETask.Pick(task, WTask.this))) {
                    BUS.post(new ETask.Pick(task, WTask.this));
                    return task;
                } else return null;
            }
        });
        task = t;
        schedule = g.getSchedule();
        game = g;
        state = State.DEFAULT;
        color = state.color;
    }

    public static void drawTask(Painter p, Graph.Task t, int color, boolean highlight) {
        try (Painter.State ignore = p.color(Colour.ACTIVE2)) {
            p.drawRect(0, 0, WTask.WIDTH / 2, WTask.HEIGHT / 2);
        }
        try (Painter.State ignore = p.color(Colour.ACTIVE3)) {
            p.drawRect(WTask.WIDTH / 2, 0, WTask.WIDTH / 2, WTask.HEIGHT / 2);
        }
        try (Painter.State ignore = p.color(Colour.ACTIVE1)) {
            p.drawRect(0, WTask.HEIGHT / 2, WTask.WIDTH, WTask.HEIGHT / 2);
        }
        try (Painter.State ignore = p.color(color)) {
            p.drawRect(0, 0, WTask.WIDTH, HEIGHT);
        }

        p.drawTextRight(Integer.toString(t.getTime()), WTask.WIDTH / 2 - 6, Painter.fontAscent + 1);
        p.drawTextRight(t.getType(), WTask.WIDTH - 6, Painter.fontAscent);
        p.drawTextRight(t.getName(), WTask.WIDTH - 6, Painter.fontAscent + WTask.HEIGHT / 2 + 1);
        try (Painter.State ignore = p.color(0xFFFFFF)) {
            p.drawResource(Resource.CLOCK, -1, -1);
            p.drawResource(Resource.CLASS, 50, -1);
            p.drawResource(Resource.CURSOR, 1, 27);
        }

        if (highlight) {
            try (Painter.State ignore = p.color(0xAAFFFFFF)) {
                p.drawRect(0, 0, WTask.WIDTH, WTask.HEIGHT);
            }
        }
    }

    public static void drawTask(Painter p, Graph.Task t, int x, int y) {
        x -= WIDTH / 2;
        y -= HEIGHT / 2;
        try (Painter.SMatrix m = p.matrix()) {
            m.translate(x, y);
            drawTask(p, t, State.DEFAULT.color, false);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        boolean b = Stream.of(task.getPredecessor(), task.getSuccessor())
                .flatMap(i -> i.values().stream()).anyMatch(WFocus::isFocused);
        drawTask(p, task, color, WFocus.isFocused(task) || b);
        if (WDrag.isSending(this) && !WDrag.isReceiving()) {
            Widget.page().overlay(new Page.Once((a, m) -> drawTask(a, task, m.x, m.y)));
        }
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        super.onRefresh(mouse);
        State s = state();
        if (s != state) {
            if (entry != null) entry.cancel();
            entry = animator.addColor(color, s.color, 500, new Animator.FLinear(), i -> color = i);
            state = s;
        }
        animator.tick();
    }

    private State state() {
        if (game.finished(task)) return State.FINISHED;
        else if (game.executing(task)) return State.EXECUTING;
        else if (schedule.allocated(task)) return State.ALLOCATED;
        else return State.DEFAULT;
    }

    public enum State {
        FINISHED(0xAA228822), ALLOCATED(0xAA226688),
        EXECUTING(0xAA886622), DEFAULT(0xFF226688);

        int color;

        State(int color) {
            this.color = color;
        }
    }
}
