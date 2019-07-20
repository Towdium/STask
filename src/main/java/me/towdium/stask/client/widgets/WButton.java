package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public abstract class WButton extends WTooltip.Impl {
    protected ListenerAction<? super WButton> listener;

    public WButton(int x, int y, @Nullable String text) {
        super(x, y, text);
    }

    public WButton setListener(@Nullable ListenerAction<? super WButton> r) {
        listener = r;
        return this;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        int color;
        if (listener == null) color = 0x333333;
        else color = onTest(mouse) ? 0x444444 : 0x666666;

        try (Painter.State ignore = p.color(color)) {
            p.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left) {
        if (onTest(mouse) && left && listener != null) {
            listener.invoke(this);
            return true;
        } else return false;
    }
}
