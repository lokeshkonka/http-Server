package http;

import java.net.http.HttpResponse;

public interface HttpHandler {
    http.HttpResponse handle(HttpRequest request);
}
