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
    private ServerSocket serverSocket;

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
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;

            while (running) {
                try {
                    Socket socket = ss.accept();

                    boolean accepted = workerPool.submit(
                            new ConnectionHandler(socket, router, rateLimiter)
                    );

                    if (!accepted) {
                        socket.close();
                    }

                } catch (IOException e) {
                    if (running) {
                        throw e;
                    }
                    // shutdown path → ignore
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("TCP listener failed", e);
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close(); // unblocks accept()
            }
        } catch (IOException ignored) {
        }
    }
}
