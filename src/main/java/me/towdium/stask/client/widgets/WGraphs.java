package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Animator;
import me.towdium.stask.client.Animator.FBezier;
import me.towdium.stask.client.Animator.FLinear;
import me.towdium.stask.client.Animator.FQuadratic;
import me.towdium.stask.client.Colour;
import me.towdium.stask.client.Painter;
import me.towdium.stask.logic.Event.EGame;
import me.towdium.stask.logic.Event.EGraph;
import me.towdium.stask.logic.Game;
import me.towdium.stask.logic.Graph;
import me.towdium.stask.utils.wrap.Pair;
import org.joml.Vector2i;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static me.towdium.stask.logic.Event.Bus.BUS;

/**
 * Author: Towdium
 * Date: 29/06/19
 */
@ParametersAreNonnullByDefault
public class WGraphs extends WContainer {
    Animator animator = new Animator();
    Queue<Graph> add = new LinkedList<>();
    Queue<Graph> remove = new LinkedList<>();
    Map<Graph, WGraph> graphs = new HashMap<>();
    Map<WGraph, Pair<Float, Float>> temp = new HashMap<>(); // background, transparency
    Game game;
    boolean moving = false;
    int x, y;

    public WGraphs(Game g, int x, int y) {
        this.x = x;
        this.y = y;
        game = g;
        reset();
        BUS.subscribe(EGraph.Append.class, this, i -> add.add(i.graph));
        BUS.subscribe(EGraph.Complete.class, this, i -> {
            if (g.isStatic()) return;
            Pair<Float, Float> p = new Pair<>(1f, 1f);
            temp.put(graphs.get(i.graph), p);
            animator.addFloat(1f, 0f, 800, new FLinear(), j -> p.a = j);
            animator.addFloat(1f, 0f, 800, new FLinear(), j -> p.b = j, () -> remove.add(i.graph));
        });
        BUS.subscribe(EGame.Reset.class, this, i -> reset());
    }

    @Override
    public void onRemove() {
        super.onRemove();
        BUS.cancel(this);
    }

    public void setY(int y) {
        this.y = y;
        for (WGraph i : graphs.values()) i.setY(y);
    }

    private void reset() {
        clear();
        int offset = 0;
        for (Graph i : game.getInitials()) {
            WGraph w = new WGraph(y, game, i);
            put(w, offset, 0);
            graphs.put(i, w);
            offset += w.getWidth();
        }
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        super.onDraw(p, mouse);
        for (Map.Entry<WGraph, Pair<Float, Float>> i : temp.entrySet()) {
            try (Painter.State ignore1 = p.color(i.getValue().a);
                 Painter.State ignore2 = p.color(0x55BB33)) {
                p.drawRect(find(i.getKey()).x, 0, i.getKey().getWidth(), y);
            }
        }
        for (Map.Entry<WGraph, Pair<Float, Float>> i : temp.entrySet()) {
            try (Painter.State ignore1 = p.color(i.getValue().b);
                 Painter.State ignore2 = p.color(Colour.BACKGROUND)) {
                p.drawRect(find(i.getKey()).x, 0, i.getKey().getWidth(), y);
            }
        }
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        if (!moving) {
            if (!remove.isEmpty()) {
                Graph g = Objects.requireNonNull(remove.poll(), "Internal error");
                WGraph out = graphs.get(g);
                WidgetMap.Entry e = widgets.head.next;
                boolean active = false;
                while (e.next != null) {
                    if (e.wgt == out) active = true;
                    else if (active) {
                        Vector2i v = e.vec;
                        animator.addFloat((float) v.x, (float) v.x - out.getWidth(), out.getWidth() * 3,
                                new FBezier(1, 0), i -> v.x = i.intValue(), () -> moving = false);
                    }
                    e = e.next;
                }
                remove(out);
                temp.remove(out);
            } else if (!add.isEmpty()) {
                Graph g = Objects.requireNonNull(add.poll(), "Internal error");
                WGraph in = new WGraph(y, game, g);
                graphs.put(g, in);
                WidgetMap.Entry e = widgets.tail.last;
                WGraph last = (WGraph) e.wgt;
                int dst = last == null ? 0 : e.vec.x + last.getWidth();
                animator.addFloat((float) x, (float) dst, FQuadratic.unify(1000, 800, x - dst),
                        new FQuadratic(true), j -> put(in, j.intValue(), 0), () -> moving = false);
            }
        }
        animator.tick();
        super.onRefresh(mouse);
    }

    public void setX(int x) {
        this.x = x;
    }
}
