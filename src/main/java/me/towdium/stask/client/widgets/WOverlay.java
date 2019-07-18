package me.towdium.stask.client.widgets;

import me.towdium.stask.client.Widget;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: Towdium
 * Date: 17/07/19
 */
@ParametersAreNonnullByDefault
public class WOverlay extends WPanel {
    boolean fixed;

    public WOverlay(int xs, int ys) {
        this(xs, ys, false);
    }

    public WOverlay(int xs, int ys, boolean fixed) {
        super(xs, ys);
        this.fixed = fixed;
    }

    @Override
    public boolean onKey(int code) {
        if (!fixed && code == GLFW.GLFW_KEY_ESCAPE) {
            Widget.page().overlay(null);
            return true;
        } else return super.onKey(code);
    }

    @Override
    public boolean onPress(@Nullable Vector2i mouse, boolean left) {
        if (!fixed && !onTest(mouse)) Widget.page().overlay(null);
        return super.onPress(mouse, left);
    }

    @Override
    public boolean onClick(@Nullable Vector2i mouse, boolean left) {
        if (fixed && !onTest(mouse)) return true;
        return super.onClick(mouse, left);
    }
}
