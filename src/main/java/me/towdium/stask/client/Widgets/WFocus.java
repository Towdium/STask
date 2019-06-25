package me.towdium.stask.client.Widgets;

import me.towdium.stask.client.Painter;
import me.towdium.stask.utils.Quad;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Towdium
 * Date: 28/05/19
 */
@ParametersAreNonnullByDefault
public abstract class WFocus implements WArea {
    static Map<WFocus, Object> owners = new HashMap<>();
    static Map<Object, Set<WFocus>> focuses = new HashMap<>();

    public static boolean isFocused(Object o) {
        return focuses.containsKey(o);
    }

    private static void put(WFocus w, Object f) {
        remove(w);
        owners.put(w, f);
        focuses.computeIfAbsent(f, i -> new HashSet<>()).add(w);
    }

    private static void remove(WFocus w) {
        Object old = owners.remove(w);
        if (old == null) return;
        Set<WFocus> set = focuses.get(old);
        set.remove(w);
        if (set.isEmpty()) focuses.remove(old);
    }

    @Override
    public void onRefresh(Vector2i mouse) {
        Object f = onTest(mouse) ? onFocus() : null;
        if (f != null) put(this, f);
        else remove(this);
    }

    @Override
    public void onRemove() {
        remove(this);
    }

    @Override
    public void onDraw(Painter p, Vector2i mouse) {
    }

    @Nullable
    public abstract Object onFocus();

    public abstract static class Impl extends WFocus {
        int x, y;

        public Impl(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean onTest(@Nullable Vector2i mouse) {
            return Quad.inside(mouse, x, y);
        }
    }
}
