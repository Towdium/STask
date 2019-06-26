package me.towdium.stask.logic;

import me.towdium.stask.utils.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Author: Towdium
 * Date: 26/06/19
 */
public class Event {
    @SuppressWarnings("unchecked")
    public static class Bus {
        Cache<Class, List<Predicate>> subs = new Cache<>(i -> new ArrayList<>());

        public boolean post(Event e) {
            List<Predicate> l = subs.get(e.getClass());
            if (l == null) return true;
            for (Predicate c : l)
                if (c.test(e)) return false;
            return true;
        }

        public <T extends Event> void subscribe(Class<T> e, Predicate<T> c) {
            subs.get(e).add(c);
        }
    }
}
