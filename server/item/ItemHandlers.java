package server.item;

import http.HttpHandler;
import http.HttpRequest;
import http.HttpResponse;

import java.util.stream.Collectors;

public final class ItemHandlers {

    private ItemHandlers() {}

    public static HttpHandler create(ItemService service) {
        return (HttpRequest req) -> {
            String body = new String(req.body);
            Item item = service.create(body);
            return new HttpResponse(201, item.id + ":" + item.name, null);
        };
    }

    public static HttpHandler list(ItemService service) {
        return (HttpRequest req) -> {
            String result = service.list().stream()
                    .map(i -> i.id + ":" + i.name)
                    .collect(Collectors.joining("\n"));
            return new HttpResponse(200, result, null);
        };
    }

    public static HttpHandler get(ItemService service) {
        return (HttpRequest req) -> {
            int id = parseId(req.path);
            Item item = service.get(id);
            if (item == null) {
                return new HttpResponse(404, "Not Found", null);
            }
            return new HttpResponse(200, item.id + ":" + item.name, null);
        };
    }

    public static HttpHandler delete(ItemService service) {
        return (HttpRequest req) -> {
            int id = parseId(req.path);
            boolean removed = service.delete(id);
            if (!removed) {
                return new HttpResponse(404, "Not Found", null);
            }
            return new HttpResponse(204, "", null);
        };
    }

    private static int parseId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }
}
