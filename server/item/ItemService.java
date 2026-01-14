package server.item;

import http.BadRequestException;

import java.util.List;

public final class ItemService {

    private final ItemRepository repo;

    public ItemService(ItemRepository repo) {
        this.repo = repo;
    }

    public Item create(String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException("Item name required");
        }
        return repo.save(body.trim());
    }

    public List<Item> list() {
        return repo.findAll();
    }

    public Item get(int id) {
        return repo.findById(id);
    }

    public boolean delete(int id) {
        return repo.delete(id);
    }
}
