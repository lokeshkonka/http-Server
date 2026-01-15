package server.staticfiles;

import http.HttpHandler;
import http.HttpResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class StaticFileHandler {

    private static final Path ROOT = Path.of("static");

    public static HttpHandler index() {
        return req -> serveFile("index.html");
    }

    public static HttpHandler assets() {
        return req -> {
            String path = req.path.substring("/static/".length());
            return serveFile(path);
        };
    }

    private static HttpResponse serveFile(String file) {
        try {
            Path p = ROOT.resolve(file).normalize();

            if (!p.startsWith(ROOT) || !Files.exists(p)) {
                return new HttpResponse(404, "Not Found", null);
            }

            String data = Files.readString(p);

            return new HttpResponse(
                    200,
                    data,
                    Map.of("Content-Type", contentType(file))
            );

        } catch (IOException e) {
            return new HttpResponse(500, "Internal Server Error", null);
        }
    }

    private static String contentType(String file) {
        if (file.endsWith(".html")) return "text/html; charset=utf-8";
        if (file.endsWith(".css"))  return "text/css; charset=utf-8";
        if (file.endsWith(".js"))   return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }
}
