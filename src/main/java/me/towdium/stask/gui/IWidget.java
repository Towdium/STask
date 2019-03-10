package me.towdium.stask.gui;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;

/**
 * Author: Towdium
 * Date: 05/03/19
 */
@NotNull
public interface IWidget {
    static boolean inside(Vector2i mouse, int xs, int ys) {
        return mouse.x >= 0 && mouse.y >= 0 && mouse.x < xs && mouse.y < ys;
    }

    void onDraw(Vector2i mouse);

    default boolean onTooltip(Vector2i mouse, List<String> tooltip) {
        return false;
    }

    default boolean onKey(char ch, int code) {
        return false;
    }

    default boolean onMouse(Vector2i mouse, int button, boolean state) {
        return false;
    }

    default boolean onScroll(Vector2i mouse, int diff) {
        return false;
    }

    @FunctionalInterface
    interface ListenerValue<W extends IWidget, V> {
        void invoke(W widget, V value);
    }

    @FunctionalInterface
    interface ListenerAction<W extends IWidget> {
        void invoke(W widget);
    }
}