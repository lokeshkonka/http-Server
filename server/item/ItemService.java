package server.item;

import http.BadRequestException;

import java.util.List;

public final class ItemService {

    private final ItemStore store;

    public ItemService(ItemStore store) {
        this.store = store;
    }

    public Item create(String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException("Item name required");
        }
        return store.create(body.trim());
    }

    public List<Item> list() {
        return store.findAll();
    }

    public Item get(int id) {
        return store.findById(id);
    }

    public boolean delete(int id) {
        return store.delete(id);
    }
}
