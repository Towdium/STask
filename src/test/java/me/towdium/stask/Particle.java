package me.towdium.stask;

import me.towdium.stask.client.*;
import me.towdium.stask.client.Widgets.WContainer;
import me.towdium.stask.client.Window.Mouse;
import org.joml.Vector2i;

/**
 * Author: Towdium
 * Date: 17/05/19
 */
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
        public boolean onMouse(Vector2i mouse, Mouse button, boolean state) {
            if (state) {
                Animator.FBezier f = new Animator.FBezier(0, 0);
                WContainer c = new WContainer();
                put(c, 0, 0);
                for (int i = 0; i < 10; i++) {
                    @SuppressWarnings("Convert2Lambda") Widget w = new Widget() {
                        @Override
                        public void onDraw(Painter p, Vector2i mouse) {
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
                animator.add(0, 1, 2000, new Animator.FLinear(), c::setTransparency, () -> remove(c));
            }
            return false;
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
            animator.tick();
            super.onDraw(p, mouse);
        }
    }
}
