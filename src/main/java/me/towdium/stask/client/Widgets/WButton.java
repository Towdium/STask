package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public abstract class WButton extends WArea {
    protected ListenerAction<? super WButton> listener;

    public WButton(int x, int y) {
        super(x, y);
    }

    public WButton setListener(ListenerAction<? super WButton> r) {
        listener = r;
        return this;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(onTest(mouse) ? 0xCCCCCC : 0xAAAAAA)) {
            p.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left, boolean state) {
        if (onTest(mouse) && left && !state && listener != null) {
            listener.invoke(this);
            return true;
        } else return false;
    }
}
