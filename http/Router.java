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

        // 1. Exact match first
        HttpHandler handler = exactRoutes.get(method + " " + path);
        if (handler != null) {
            return handler;
        }

        // 2. Longest prefix match
        HttpHandler best = null;
        int bestLen = -1;

        for (Map.Entry<String, HttpHandler> e : prefixRoutes.entrySet()) {
            String key = e.getKey();
            int space = key.indexOf(' ');
            String m = key.substring(0, space);
            String prefix = key.substring(space + 1);

            if (method.equals(m) && path.startsWith(prefix)) {
                int len = prefix.length();
                if (len > bestLen) {
                    best = e.getValue();
                    bestLen = len;
                }
            }
        }

        return best;
    }
}
