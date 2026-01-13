package http;

import java.util.HashMap;
import java.util.Map;

public final class Router {

    private final Map<String, HttpHandler> exactRoutes = new HashMap<>();
    private final Map<String, HttpHandler> prefixRoutes = new HashMap<>();

    public void register(String method, String path, HttpHandler handler) {
        if (path.endsWith("/")) {
            prefixRoutes.put(method + " " + path, handler);
        } else {
            exactRoutes.put(method + " " + path, handler);
        }
    }

    public HttpHandler match(String method, String path) {
        HttpHandler handler = exactRoutes.get(method + " " + path);
        if (handler != null) {
            return handler;
        }
        for (Map.Entry<String, HttpHandler> e : prefixRoutes.entrySet()) {
            String key = e.getKey();
            int space = key.indexOf(' ');
            String m = key.substring(0, space);
            String prefix = key.substring(space + 1);

            if (method.equals(m) && path.startsWith(prefix)) {
                return e.getValue();
            }
        }

        return null;
    }
}
