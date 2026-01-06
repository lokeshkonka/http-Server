package transport;

import http.HttpParser;
import http.HttpRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

            StringBuilder raw = new StringBuilder();
            byte[] buffer = new byte[BUFFER_SIZE];

            int read;
            while ((read = in.read(buffer)) != -1) {
                raw.append(new String(buffer, 0, read));

                // HTTP header terminator
                if (raw.indexOf("\r\n\r\n") != -1) {
                    break;
                }
            }

            HttpRequest request = HttpParser.parse(raw.toString());

            // TEMP: log parsed request
            System.out.println(
                    request.method + " " + request.path + " " + request.version
            );

            // Minimal valid HTTP response
            String response =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Length: 2\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    "OK";

            out.write(response.getBytes());
            out.flush();

        } catch (SocketTimeoutException e) {
            // slow client, drop
        } catch (Exception e) {
            // malformed request or IO error
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
