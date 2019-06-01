package me.towdium.stask;

import me.towdium.stask.client.*;
import me.towdium.stask.client.Widgets.WContainer;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 17/05/19
 */
@ParametersAreNonnullByDefault
public class Particle {
    public static void main(String[] args) {
        Page.Simple root = new Page.Simple();
        root.put(new WParticle(), 0, 0);

        try (Window w = new Window("Render", root)) {
            w.display();
            while (!w.isFinished()) w.tick();
        }
    }

    static class WParticle extends WContainer {
        Animator animator = new Animator();

        @Override
        public boolean onClick(@Nullable Vector2i mouse, boolean left, boolean state) {
            if (mouse != null) {
                Animator.FBezier f = new Animator.FBezier(0, 0);
                Batch c = new Batch();
                put(c, 0, 0);
                for (int i = 0; i < 10; i++) {
                    @SuppressWarnings("Convert2Lambda") Widget w = new Widget() {
                        @Override
                        public void onDraw(Painter p, @Nullable Vector2i mouse) {
                            p.drawRect(-5, -5, 10, 10);
                        }
                    };
                    double ran = Math.random() * 2 * Math.PI;
                    double x = Math.sin(ran);
                    double y = Math.cos(ran);
                    animator.add(0, 100, 2000, f,
                            j -> c.put(w, (int) (x * j) + mouse.x, (int) (y * j) + mouse.y),
                            () -> remove(w));
                }
                animator.add(0, 1, 2000, new Animator.FLinear(), i -> c.transparency = i, () -> remove(c));
            }
            return false;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            animator.tick();
            super.onDraw(p, mouse);
        }

        static class Batch extends WContainer {
            float transparency = 0;

            @Override
            public void onDraw(Painter p, Vector2i mouse) {
                try (Painter.State ignore = p.color(transparency)) {
                    super.onDraw(p, mouse);
                }
            }
        }
    }
}
