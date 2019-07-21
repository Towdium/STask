package me.towdium.stask.client.pages;

import me.towdium.stask.client.Animator;
import me.towdium.stask.client.Colour;
import me.towdium.stask.client.Page;
import me.towdium.stask.client.Painter;
import me.towdium.stask.client.widgets.WContainer;
import me.towdium.stask.utils.Log;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

/**
 * Author: Towdium
 * Date: 25/06/19
 */
@ParametersAreNonnullByDefault
public class PWrapper extends WContainer implements Page {
    Animator animator = new Animator();
    Page page = null;
    Animator.Entry entry = null;
    float transparency = 1;
    int x, y;

    public void display(Supplier<Page> s) {
        Runnable add = () -> {
            page = s.get();
            put(page, 0, 0);
            page.onResize(x, y);
            animator.addFloat(0, 1, 500, new Animator.FBezier(1, 0), i -> transparency = i);
        };
        Runnable remove = () -> {
            page.onRemove();
            remove(page);
            add.run();
        };

        if (page != null)
            entry = animator.addFloat(1, 0, 500, new Animator.FBezier(1, 0),
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
        super.onDraw(p, mouse);
        animator.tick();
        try (Painter.State ignore1 = p.color(transparency);
             Painter.State ignore2 = p.color(Colour.BACKGROUND)) {
            p.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onPress(@Nullable Vector2i mouse, boolean left) {
        Log.client.info("Press, " + animator.isActive());
        return (entry != null && !entry.finished()) || super.onPress(mouse, left);
    }
}
