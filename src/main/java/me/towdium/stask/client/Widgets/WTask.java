package me.towdium.stask.client.Widgets;

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
public class WTask extends WDragFocus {
    public static final int WIDTH = 30;
    public static final int HEIGHT = 38;
    Graph.Task task;
    Allocation allocation;
    Game game;

    public WTask(Graph.Task t, Allocation a, Game g) {
        super(WIDTH, HEIGHT);
        task = t;
        allocation = a;
        game = g;
    }

    public static void drawTask(Painter p, Graph.Task t, State s, boolean highlight) {
        try (Painter.State ignore = p.color(0x666666)) {
            p.drawRect(0, 0, WTask.WIDTH, WTask.HEIGHT);
        }
        try (Painter.State ignore = p.color(0x888888)) {
            p.drawRect(0, 0, WTask.WIDTH, 19);
        }
        try (Painter.State ignore = p.color(s.color)) {
            p.drawRect(0, 0, WTask.WIDTH, HEIGHT);
        }
        if (highlight) {
            try (Painter.State ignore = p.color(0xAAFFFFFF)) {
                p.drawRect(0, 0, WTask.WIDTH, WTask.HEIGHT);
            }
        }
        p.drawTextRight(Integer.toString(t.getTime()), 26, Painter.fontAscent + 2);
        p.drawTextRight(t.getType(), 26, Painter.fontAscent + 19);
    }

    public static void drawTask(Painter p, Graph.Task t, int x, int y) {
        try (Painter.SMatrix m = p.matrix()) {
            m.translate(x, y);
            drawTask(p, t, State.DEFAULT, false);
        }
    }

    @Override
    protected Graph.Work onFocus() {
        return task;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        boolean b = false;
        if (WFocus.focus instanceof Graph.Comm) {
            Graph.Comm c = (Graph.Comm) WFocus.focus;
            b = c.getDst() == task || c.getSrc() == task;
        }
        drawTask(p, task, state(), WFocus.focus == task || b);
        if (WDrag.sender == drag && WDrag.receiver == null) {
            try (Painter.State ignore = p.priority(true)) {
                drawTask(p, task, mouse.x - WIDTH / 2, mouse.y - HEIGHT / 2);
            }
        }
    }

    @Nullable
    @Override
    public Object onStarting() {
        return task;
    }

    private State state() {
        if (allocation.allocated(task)) return State.ALLOCATED;
        else if (game.finished(task)) return State.FINISHED;
        else if (game.executing(task)) return State.EXECUTING;
        else return State.DEFAULT;
    }

    public enum State {
        FINISHED(0xAA228822), ALLOCATED(0xAA226688),
        EXECUTING(0xAA886622), DEFAULT(0xFF000000);

        int color;

        State(int color) {
            this.color = color;
        }
    }
}
