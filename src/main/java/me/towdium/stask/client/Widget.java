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

    default boolean onKey(int code) {
        return false;
    }

    default void onMove(Vector2i mouse) {
    }

    default void onRemove() {
    }

    default void onRefresh(Vector2i mouse) {
    }

    default boolean onClick(@Nullable Vector2i mouse, boolean left) {
        return false;
    }

    default boolean onDrag(@Nullable Vector2i mouse, boolean left) {
        return false;
    }

    default boolean onDrop(boolean left) {
        return false;
    }

    default boolean onPress(@Nullable Vector2i mouse, boolean left) {
        return false;
    }

    static Page page() {
        return Page.Ref.getPage();
    }

    static Window window() {
        return Window.getCurrent();
    }

    @FunctionalInterface
    interface ListenerAction<W> {
        void invoke(W widget);
    }

    @FunctionalInterface
    interface ListenerValue<W, V> {
        void invoke(W widget, V old, V neu);
    }
}