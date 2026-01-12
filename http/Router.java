package http;

import java.util.HashMap;
import java.util.Map;

public final class Router {

    private final Map<String, HttpHandler> routes = new HashMap<>();

    public void register(String method, String path, HttpHandler handler) {
        String key = key(method, path);
        routes.put(key, handler);
    }

    public HttpHandler match(String method, String path) {
        return routes.get(key(method, path));
    }

    private String key(String method, String path) {
        return method + " " + path;
    }
}
