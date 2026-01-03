package transport;

import concurrency.WorkerPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class TcpListener implements Runnable {

    private final int port;
    private final WorkerPool workerPool;
    private volatile boolean running = true;

    public TcpListener(int port, WorkerPool workerPool) {
        this.port = port;
        this.workerPool = workerPool;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (running) {
                Socket socket = serverSocket.accept();

                boolean accepted = workerPool.submit(() -> handle(socket));

                if (!accepted) {
                    closeQuietly(socket);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("TCP listener failed", e);
        }
    }

    private void handle(Socket socket) {
        // TEMP handler (Day 3 only)
        // Proves lifecycle correctness
        closeQuietly(socket);
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
