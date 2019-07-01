package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Animator;
import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Allocation;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 11/06/19
 */
@ParametersAreNonnullByDefault
public class WTask extends WCompose {
    public static final int WIDTH = 80;
    public static final int HEIGHT = 56;
    Graph.Task task;
    Allocation allocation;
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
                return task;
            }
        });
        task = t;
        allocation = g.getAllocation();
        game = g;
        state = State.DEFAULT;
        color = state.color;
    }

    public static void drawTask(Painter p, Graph.Task t, int color, boolean highlight) {
        try (Painter.State ignore = p.color(0x666666)) {
            p.drawRect(0, 0, WTask.WIDTH / 2, WTask.HEIGHT / 2);
        }
        try (Painter.State ignore = p.color(0x777777)) {
            p.drawRect(WTask.WIDTH / 2, 0, WTask.WIDTH / 2, WTask.HEIGHT / 2);
        }
        try (Painter.State ignore = p.color(0x555555)) {
            p.drawRect(0, WTask.HEIGHT / 2, WTask.WIDTH, WTask.HEIGHT / 2);
        }
        try (Painter.State ignore = p.color(color)) {
            p.drawRect(0, 0, WTask.WIDTH, HEIGHT);
        }
        if (highlight) {
            try (Painter.State ignore = p.color(0xAAFFFFFF)) {
                p.drawRect(0, 0, WTask.WIDTH, WTask.HEIGHT);
            }
        }
        p.drawTextRight(Integer.toString(t.getTime()), WTask.WIDTH / 2 - 6, Painter.fontAscent + 1);
        p.drawTextRight(t.getType(), WTask.WIDTH - 6, Painter.fontAscent);
        p.drawTextRight(t.getName(), WTask.WIDTH - 6, Painter.fontAscent + WTask.HEIGHT / 2 + 1);
    }

    public static void drawTask(Painter p, Graph.Task t, int x, int y) {
        try (Painter.SMatrix m = p.matrix()) {
            m.translate(x, y);
            drawTask(p, t, State.DEFAULT.color, false);
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        boolean b = false;
        for (Graph.Comm c : task.getAfter().values())
            if (WFocus.isFocused(c)) b = true;
        for (Graph.Comm c : task.getBefore().values())
            if (WFocus.isFocused(c)) b = true;

        drawTask(p, task, color, WFocus.isFocused(task) || b);
        if (WDrag.isSending(this) && !WDrag.isReceiving()) {
            try (Painter.State ignore = p.priority(true)) {
                drawTask(p, task, mouse.x - WIDTH / 2, mouse.y - HEIGHT / 2);
            }
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
        if (allocation.allocated(task)) return State.ALLOCATED;
        else if (game.finished(task)) return State.FINISHED;
        else if (game.executing(task)) return State.EXECUTING;
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
