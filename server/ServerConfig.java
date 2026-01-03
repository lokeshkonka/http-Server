package server;

public final class ServerConfig {

    private final int port;
    private final int workerCount;
    private final int queueSize;

    public ServerConfig(int port, int workerCount, int queueSize) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount must be > 0");
        }
        if (queueSize <= 0) {
            throw new IllegalArgumentException("queueSize must be > 0");
        }

        this.port = port;
        this.workerCount = workerCount;
        this.queueSize = queueSize;
    }

    public int port() {
        return port;
    }

    public int workerCount() {
        return workerCount;
    }

    public int queueSize() {
        return queueSize;
    }
}
