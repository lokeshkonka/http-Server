package http;

import java.util.Map;

public final class HttpRequest {

    public final String method;
    public final String path;
    public final String version;
    public final Map<String, String> headers;
    public final byte[] body;
    public HttpRequest(
            String method,
            String path,
            String version,
            Map<String, String> headers,
            byte[] body 
    ) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.headers = headers;
        this.body = body;
    }
}
