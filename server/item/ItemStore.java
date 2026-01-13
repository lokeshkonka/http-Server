package server.item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ItemStore {

    private final AtomicInteger idSeq = new AtomicInteger(1);
    private final Map<Integer, Item> items = new ConcurrentHashMap<>();

    public Item create(String name) {
        int id = idSeq.getAndIncrement();
        Item item = new Item(id, name);
        items.put(id, item);
        return item;
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public Item findById(int id) {
        return items.get(id);
    }

    public boolean delete(int id) {
        return items.remove(id) != null;
    }
}
