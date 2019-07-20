package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 20/07/19
 */
@ParametersAreNonnullByDefault
public abstract class WTooltip implements WArea {
    protected String text;

    public WTooltip(@Nullable String text) {
        this.text = text;
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        if (text != null && onTest(mouse)) WStatus.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static class Impl extends WTooltip implements WArea {
        int x, y;

        public Impl(int x, int y, @Nullable String text) {
            super(text);
            this.x = x;
            this.y = y;
        }

        public boolean onTest(@Nullable Vector2i mouse) {
            return Quad.inside(mouse, x, y);
        }

        @Override
        public void onDraw(Painter p, Vector2i mouse) {
        }
    }
}
