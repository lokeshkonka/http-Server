package transport;

import http.HttpParser;
import http.HttpRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;

public final class ConnectionHandler implements Runnable {

    private static final int READ_TIMEOUT_MS = 5000;
    private static final int BUFFER_SIZE = 1024;

    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(READ_TIMEOUT_MS);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // ---- 1. Read headers ----
            StringBuilder headers = new StringBuilder();
            byte[] buffer = new byte[BUFFER_SIZE];

            int read;
            while ((read = in.read(buffer)) != -1) {
                headers.append(new String(buffer, 0, read));
                if (headers.indexOf("\r\n\r\n") != -1) {
                    break;
                }
            }

            int headerEnd = headers.indexOf("\r\n\r\n");
            if (headerEnd == -1) {
                throw new IllegalArgumentException("Invalid HTTP request");
            }

            String headerPart = headers.substring(0, headerEnd);

            // ---- 2. Parse headers first ----
            HttpRequest temp =
                    HttpParser.parse(headerPart, new byte[0]);

            // ---- 3. Read body if Content-Length exists ----
            int contentLength = 0;
            Map<String, String> h = temp.headers;

            if (h.containsKey("Content-Length")) {
                contentLength = Integer.parseInt(h.get("Content-Length"));
                if (contentLength < 0 || contentLength > 1_000_000) {
                    throw new IllegalArgumentException("Invalid Content-Length");
                }
            }

            byte[] body = new byte[contentLength];
            int totalRead = 0;

            while (totalRead < contentLength) {
                int r = in.read(body, totalRead, contentLength - totalRead);
                if (r == -1) break;
                totalRead += r;
            }

            if (totalRead != contentLength) {
                throw new IllegalArgumentException("Body truncated");
            }

            HttpRequest request =
                    HttpParser.parse(headerPart, body);

            // ---- TEMP LOG ----
            System.out.println(
                    request.method + " " + request.path +
                    " bodyBytes=" + request.body.length
            );

            // ---- Response ----
            String response =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: 2\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "OK";

            out.write(response.getBytes());
            out.flush();

        } catch (SocketTimeoutException e) {
            // slow client
        } catch (Exception e) {
            // malformed or IO error
        } finally {
            closeQuietly();
        }
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }
}
