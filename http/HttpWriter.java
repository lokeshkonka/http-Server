package http;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class HttpWriter {

    public static void write(OutputStream out, HttpResponse response) throws Exception {
        String body = response.body == null ? "" : response.body;
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ")
          .append(response.status)
          .append(" ")
          .append(statusText(response.status))
          .append("\r\n");

        sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n");
        sb.append("Connection: close\r\n");

        if (response.headers != null) {
            for (Map.Entry<String, String> e : response.headers.entrySet()) {
                sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
            }
        }

        sb.append("\r\n");

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.flush();
    }

    private static String statusText(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 400 -> "Bad Request";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }
}
