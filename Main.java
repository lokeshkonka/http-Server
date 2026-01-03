import concurrency.WorkerPool;
import server.ServerConfig;
import transport.TcpListener;

public final class Main {

    public static void main(String[] args) {
        
        int port = 8080;
        int workers = Runtime.getRuntime().availableProcessors();
        int queueSize = 100;

        ServerConfig config = new ServerConfig(port, workers, queueSize);

        WorkerPool workerPool = new WorkerPool(
                config.workerCount(),
                config.queueSize()
        );

        TcpListener listener = new TcpListener(
                config.port(),
                workerPool
        );

        Thread serverThread = new Thread(listener, "tcp-listener");
        serverThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            listener.shutdown();
            workerPool.shutdown();
        }));
    }
}
