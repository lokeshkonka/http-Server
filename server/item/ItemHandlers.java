package server.item;

import http.HttpHandler;
import http.HttpRequest;
import http.HttpResponse;

import java.util.List;
import java.util.Map;

public final class ItemHandlers {

    private ItemHandlers() {}

    public static HttpHandler create(ItemService service) {
        return req -> {
            String name = new String(req.body);
            Item item = service.create(name);

            String json =
                    "{\"id\":" + item.id +
                    ",\"name\":\"" + escape(item.name) + "\"}";

            return new HttpResponse(
                    201,
                    json,
                    Map.of("Content-Type", "application/json")
            );
        };
    }

    public static HttpHandler list(ItemService service) {
        return req -> {
            List<Item> items = service.list();

            StringBuilder json = new StringBuilder();
            json.append("[");

            for (int i = 0; i < items.size(); i++) {
                Item it = items.get(i);
                json.append("{")
                    .append("\"id\":").append(it.id).append(",")
                    .append("\"name\":\"").append(escape(it.name)).append("\"")
                    .append("}");
                if (i < items.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]");

            return new HttpResponse(
                    200,
                    json.toString(),
                    Map.of("Content-Type", "application/json")
            );
        };
    }

    public static HttpHandler get(ItemService service) {
        return req -> {
            int id = parseId(req.path);
            Item item = service.get(id);

            if (item == null) {
                return new HttpResponse(404, "Not Found", null);
            }

            String json =
                    "{\"id\":" + item.id +
                    ",\"name\":\"" + escape(item.name) + "\"}";

            return new HttpResponse(
                    200,
                    json,
                    Map.of("Content-Type", "application/json")
            );
        };
    }

    public static HttpHandler delete(ItemService service) {
        return req -> {
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

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}
