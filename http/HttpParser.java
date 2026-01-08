package http;

import java.util.HashMap;
import java.util.Map;

public final class HttpParser {

    public static HttpRequest parse(
            String headersPart,
            byte[] body
    ) {
        String[] lines = headersPart.split("\r\n");

        if (lines.length == 0) {
            throw new IllegalArgumentException("Empty request");
        }

        String[] start = lines[0].split(" ");
        if (start.length != 3) {
            throw new IllegalArgumentException("Invalid start line");
        }

        String method = start[0];
        String path = start[1];
        String version = start[2];

        Map<String, String> headers = new HashMap<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) break;

            int idx = line.indexOf(':');
            if (idx <= 0) {
                throw new IllegalArgumentException("Malformed header: " + line);
            }

            headers.put(
                    line.substring(0, idx).trim(),
                    line.substring(idx + 1).trim()
            );
        }

        return new HttpRequest(method, path, version, headers, body);
    }
}
