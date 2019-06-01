package me.towdium.stask.client;

import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Author: Towdium
 * Date: 05/03/19
 */
@ParametersAreNonnullByDefault
public interface Widget {
    void onDraw(Painter p, Vector2i mouse);

    default boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
        return false;
    }

    default boolean onKey(char ch, int code) {
        return false;
    }

    default void onMove(Vector2i mouse) {
    }

    default boolean onClick(@Nullable Vector2i mouse, boolean left, boolean state) {
        return false;
    }

    default boolean onScroll(@Nullable Vector2i mouse, int diff) {
        return false;
    }

    @FunctionalInterface
    interface ListenerValue<W extends Widget, V> {
        void invoke(W widget, V value);
    }

    @FunctionalInterface
    interface ListenerAction<W extends Widget> {
        void invoke(W widget);
    }


}