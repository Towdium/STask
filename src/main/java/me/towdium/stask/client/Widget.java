package me.towdium.stask.client;

import me.towdium.stask.client.Window.Mouse;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.List;

/**
 * Author: Towdium
 * Date: 05/03/19
 */
public interface Widget {
    void onDraw(Painter p, @Nullable Vector2i mouse);

    default boolean onTooltip(@Nullable Vector2i mouse, List<String> tooltip) {
        return false;
    }

    default boolean onKey(char ch, int code) {
        return false;
    }

    default boolean onMouse(@Nullable Vector2i mouse, Mouse button, boolean state) {
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