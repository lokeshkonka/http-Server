package http;

import java.util.Map;

public final class HttpResponse {

    public final int status;
    public final String body;
    public final Map<String, String> headers;

    public HttpResponse(
            int status,
            String body,
            Map<String, String> headers
    ) {
        this.status = status;
        this.body = body;
        this.headers = headers;
    }
}
