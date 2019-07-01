package me.towdium.stask.logic;

import me.towdium.stask.utils.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Author: Towdium
 * Date: 26/06/19
 */
public class Event {
    @SuppressWarnings("unchecked")
    public static class Bus {
        Cache<Class, List<Consumer>> subs = new Cache<>(i -> new ArrayList<>());
        Cache<Class, List<Predicate>> gates = new Cache<>(i -> new ArrayList<>());

        public boolean attempt(Event e) {
            List<Predicate> ps = gates.get(e.getClass());
            if (ps != null) {
                for (Predicate p : ps) {
                    if (p.test(e)) return false;
                }
            }
            return true;
        }

        public void post(Event e) {
            List<Consumer> cs = subs.get(e.getClass());
            if (cs != null) {
                for (Consumer c : cs) c.accept(e);
            }
        }

        public <T extends Event> void subscribe(Class<T> e, Consumer<T> c) {
            subs.get(e).add(c);
        }

        public <T extends Event> void gate(Class<T> e, Predicate<T> p) {
            gates.get(e).add(p);
        }
    }
}
