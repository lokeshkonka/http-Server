package transport;

import http.*;
import server.Logger;
import server.ratelimit.RateLimiter;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ConnectionHandler implements Runnable {

    private static final int READ_TIMEOUT_MS = 5000;
    private static final int BUFFER_SIZE = 1024;

    private final Socket socket;
    private final Router router;
    private final RateLimiter rateLimiter;

    public ConnectionHandler(
            Socket socket,
            Router router,
            RateLimiter rateLimiter
    ) {
        this.socket = socket;
        this.router = router;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void run() {
        OutputStream out = null;
        long startTime = System.nanoTime();

        try {
            socket.setSoTimeout(READ_TIMEOUT_MS);

            InputStream in = socket.getInputStream();
            out = socket.getOutputStream();

            // ---- RATE LIMIT (EARLY EXIT) ----
            String clientIp = socket.getInetAddress().getHostAddress();
            if (!rateLimiter.allow(clientIp)) {
                Logger.error(429, "Rate limit exceeded for ip=" + clientIp);
                safeWrite(out, new HttpResponse(429, "Too Many Requests", null));
                return;
            }

            // ---- 1. Read headers ----
            StringBuilder raw = new StringBuilder();
            byte[] buffer = new byte[BUFFER_SIZE];

            int read;
            while ((read = in.read(buffer)) != -1) {
                raw.append(new String(buffer, 0, read, StandardCharsets.ISO_8859_1));
                if (raw.indexOf("\r\n\r\n") != -1) break;
            }

            int headerEnd = raw.indexOf("\r\n\r\n");
            if (headerEnd == -1) {
                throw new BadRequestException("Invalid HTTP request");
            }

            int bodyStart = headerEnd + 4;
            String headerPart = raw.substring(0, headerEnd);
            byte[] leftover =
                    raw.substring(bodyStart).getBytes(StandardCharsets.ISO_8859_1);

            // ---- 2. Parse headers ----
            HttpRequest temp = HttpParser.parse(headerPart, new byte[0]);

            // ---- 3. Content-Length ----
            int contentLength = 0;
            Map<String, String> h = temp.headers;

            if (h.containsKey("content-length")) {
                contentLength = Integer.parseInt(h.get("content-length"));
                if (contentLength < 0 || contentLength > 1_000_000) {
                    throw new BadRequestException("Invalid Content-Length");
                }
            }

            // ---- 4. Read body ----
            byte[] body = new byte[contentLength];
            int copied = Math.min(leftover.length, contentLength);
            System.arraycopy(leftover, 0, body, 0, copied);

            int totalRead = copied;
            while (totalRead < contentLength) {
                int r = in.read(body, totalRead, contentLength - totalRead);
                if (r == -1) break;
                totalRead += r;
            }

            if (totalRead != contentLength) {
                throw new BadRequestException("Body truncated");
            }

            HttpRequest request = HttpParser.parse(headerPart, body);

            // ---- 5. Routing ----
            HttpHandler handler =
                    router.match(request.method, request.path);

            HttpResponse response =
                    (handler == null)
                            ? new HttpResponse(404, "Not Found", null)
                            : handler.handle(request);

            // ---- 6. Logging ----
            long elapsedMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            Logger.request(
                    request.method,
                    request.path,
                    response.status,
                    elapsedMs
            );

            // ---- 7. Write response ----
            HttpWriter.write(out, response);

        } catch (BadRequestException e) {
            Logger.error(400, e.getMessage());
            safeWrite(out, new HttpResponse(400, "Bad Request", null));

        } catch (SocketTimeoutException ignored) {

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(500, "Internal Server Error");
            safeWrite(out, new HttpResponse(500, "Internal Server Error", null));

        } finally {
            closeQuietly();
        }
    }

    private void safeWrite(OutputStream out, HttpResponse response) {
        if (out == null) return;
        try {
            HttpWriter.write(out, response);
        } catch (Exception ignored) {
        }
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (Exception ignored) {
        }
    }
}
