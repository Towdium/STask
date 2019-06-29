package me.towdium.stask.client.pages;

import me.towdium.stask.client.Animator;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Supplier;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public class PWrapper implements Page {
    Animator animator = new Animator();
    Page page = null;
    float transparency = 1;
    int x, y;

    public void display(Supplier<Page> s) {
        Runnable add = () -> {
            page = s.get();
            page.onResize(x, y);
            animator.addFloat(0, 1, 500, new Animator.FBezier(1, 0), i -> transparency = i);
        };
        Runnable remove = () -> {
            page.onRemove();
            add.run();
        };

        if (page != null)
            animator.addFloat(1, 0, 500, new Animator.FBezier(1, 0),
                    i -> transparency = i, remove);
        else add.run();
    }

    @Override
    public void onResize(int x, int y) {
        this.x = x;
        this.y = y;
        if (page != null) page.onResize(x, y);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        animator.tick();
        if (page != null) page.onDraw(p, mouse);
        try (Painter.State ignore1 = p.color(transparency);
             Painter.State ignore2 = p.color(0x161616)) {
            p.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
        return page != null && page.onTooltip(mouse, tooltip);
    }

    @Override
    public boolean onKey(int code) {
        return page != null && page.onKey(code);
    }

    @Override
    public void onMove(Vector2i mouse) {
        if (page != null) page.onMove(mouse);
    }

    @Override
    public void onRemove() {
        if (page != null) page.onRemove();
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        if (page != null) page.onRefresh(mouse);
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left) {
        return page != null && page.onClick(mouse, left);
    }

    @Override
    public boolean onScroll(@Nullable Vector2i mouse, int diff) {
        return page != null && page.onScroll(mouse, diff);
    }

    @Override
    public boolean onDrag(@Nullable Vector2i mouse, boolean left) {
        return page != null && page.onDrag(mouse, left);
    }

    @Override
    public boolean onDrop(boolean left) {
        return page != null && page.onDrop(left);
    }

    @Override
    public boolean onPress(@Nullable Vector2i mouse, boolean left) {
        return page != null && page.onPress(mouse, left);
    }
}
