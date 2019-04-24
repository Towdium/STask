package me.towdium.stask.utils;

import java.util.Iterator;

/**
 * Author: Towdium
 * Date: 24/04/19
 */
public class Circulator<T> {
    Node<T> head = new Node<>(null);
    Node<T> tail = head;
    int size = 0;

    public void add(T t) {
        tail = tail.next = new Node<>(t);
        size++;
    }

    public Iterator<T> iterator() {
        return new Itr();
    }

    public int size() {
        return size;
    }

    static class Node<T> {
        T t;
        Node<T> next;

        public Node(T t) {
            this.t = t;
        }
    }

    class Itr implements Iterator<T> {
        Node<T> last = null;
        Node<T> crr = head;

        @Override
        public boolean hasNext() {
            return head.next != null;
        }

        @Override
        public T next() {
            if (crr.next == null) crr = head;
            last = crr;
            crr = crr.next;
            return crr.t;
        }

        public void remove() {
            if (last != null) {
                last.next = crr.next;
                crr = last;
                last = null;
                size--;
            } else throw new IllegalStateException();
        }
    }
}
