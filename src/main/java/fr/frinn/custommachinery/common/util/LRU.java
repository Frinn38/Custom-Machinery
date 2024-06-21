package fr.frinn.custommachinery.common.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Taken from <a href="https://github.com/mekanism/Mekanism/blob/release/1.20.x/src/main/java/mekanism/common/lib/collection/LRU.java">mekanism lib</a>
 */
public class LRU<T> extends AbstractCollection<T> {

    private final Map<T, LRUEntry<T>> lookupMap = new Object2ObjectOpenHashMap<>();

    private final LRUEntry<T> head, tail;
    private int size;

    public LRU() {
        head = new LRUEntry<>(null);
        tail = new LRUEntry<>(null);
        head.next = tail;
        tail.prev = head;
    }

    private void removeEntry(LRUEntry<T> entry) {
        entry.next.prev = entry.prev;
        entry.prev.next = entry.next;
        size--;
        lookupMap.remove(entry.value);
    }

    private void addFirst(LRUEntry<T> entry) {
        entry.prev = head;
        entry.next = head.next;
        entry.next.prev = entry;
        head.next = entry;
        size++;
        lookupMap.put(entry.value, entry);
    }

    @Override
    public boolean add(T element) {
        addFirst(new LRUEntry<>(element));
        return true;
    }

    public void moveUp(T element) {
        LRUEntry<T> entry = lookupMap.get(element);
        if (entry == null) {
            return;
        }
        removeEntry(entry);
        addFirst(entry);
    }

    @Override
    public boolean remove(Object element) {
        LRUEntry<T> entry = lookupMap.get(element);
        if (entry == null) {
            return false;
        }
        removeEntry(entry);
        return true;
    }

    @Override
    public boolean contains(Object element) {
        return lookupMap.containsKey(element);
    }

    @Override
    public int size() {
        return size;
    }

    public void reverseIterate(Consumer<T> callback) {
        LRUEntry<T> ptr = tail.prev;
        while (ptr != head) {
            callback.accept(ptr.value);
            ptr = ptr.prev;
        }
    }

    private static class LRUEntry<T> {

        private final T value;
        private LRUEntry<T> prev, next;

        private LRUEntry(T value) {
            this.value = value;
        }
    }

    @NotNull
    @Override
    public LRUIterator iterator() {
        return new LRUIterator();
    }

    public LRUIterator descendingIterator() {
        return new LRUIterator().reverse();
    }

    public class LRUIterator implements Iterator<T> {

        boolean reverse = false;
        LRUEntry<T> curEntry = head;

        @Override
        public boolean hasNext() {
            return reverse ? curEntry.prev != head : curEntry.next != tail;
        }

        @Override
        public T next() {
            if (reverse) {
                curEntry = curEntry.prev;
                if (curEntry == head) {
                    throw new NoSuchElementException("Reached beginning of LRU");
                }
            } else {
                curEntry = curEntry.next;
                if (curEntry == tail) {
                    throw new NoSuchElementException("Reached end of LRU");
                }
            }
            return curEntry == null ? null : curEntry.value;
        }

        public LRUIterator reverse() {
            reverse = true;
            curEntry = tail;
            return this;
        }
    }
}
