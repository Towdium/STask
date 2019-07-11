package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.utils.Utilities.ListenerAction;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

/**
 * Author: Towdium
 * Date: 08/04/19
 */
@ParametersAreNonnullByDefault
public abstract class WButton extends WArea.Impl {
    protected ListenerAction<? super WButton> listener;
    protected Supplier<Boolean> gate;

    public WButton(int x, int y) {
        this(x, y, null);
    }

    public WButton(int x, int y, @Nullable Supplier<Boolean> gate) {
        super(x, y);
        this.gate = gate;
    }

    public WButton setListener(ListenerAction<? super WButton> r) {
        listener = r;
        return this;
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
        try (Painter.State ignore = p.color(onTest(mouse) ? 0x444444 : 0x666666)) {
            p.drawRect(0, 0, x, y);
        }
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left) {
        if (onTest(mouse) && left && listener != null && (gate == null || gate.get())) {
            listener.invoke(this);
            return true;
        } else return false;
    }
}
