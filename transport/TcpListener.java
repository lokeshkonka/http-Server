package transport;

import concurrency.WorkerPool;
import http.Router;
import server.ratelimit.RateLimiter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class TcpListener implements Runnable {

    private final int port;
    private final WorkerPool workerPool;
    private final Router router;
    private final RateLimiter rateLimiter;
    private volatile boolean running = true;

    public TcpListener(
            int port,
            WorkerPool workerPool,
            Router router,
            RateLimiter rateLimiter
    ) {
        this.port = port;
        this.workerPool = workerPool;
        this.router = router;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (running) {
                Socket socket = serverSocket.accept();

                boolean accepted = workerPool.submit(
                        new ConnectionHandler(socket, router, rateLimiter)
                );

                if (!accepted) {
                    closeQuietly(socket);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("TCP listener failed", e);
        }
    }

    private void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public void shutdown() {
        running = false;
    }
}
