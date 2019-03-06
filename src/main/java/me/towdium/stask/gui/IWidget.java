package me.towdium.stask.gui;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: Towdium
 * Date: 05/03/19
 */
@NotNull
public interface IWidget {
    default void onDraw() {
    }

    default boolean onTooltip(List<String> tooltip) {
        return false;
    }

    default boolean onClicked(int button) {
        return false;
    }

    default boolean onKey(char ch, int code) {
        return false;
    }

    default boolean onScroll(int diff) {
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