import concurrency.WorkerPool;
import http.HttpResponse;
import http.Router;
import server.ServerConfig;
import transport.TcpListener;

public final class Main {

    public static void main(String[] args) {

        int port = 8080;
        int workers = Runtime.getRuntime().availableProcessors();
        int queueSize = 100;

        // ---- 1. Build router FIRST ----
        Router router = new Router();

        router.register("GET", "/", req ->
                new HttpResponse(200, "Hello from AegisServer", null)
        );

        router.register("POST", "/echo", req ->
                new HttpResponse(200, new String(req.body), null)
        );

        // ---- 2. Build config ----
        ServerConfig config = new ServerConfig(port, workers, queueSize);

        // ---- 3. Build worker pool ----
        WorkerPool workerPool = new WorkerPool(
                config.workerCount(),
                config.queueSize()
        );

        // ---- 4. Build listener ----
        TcpListener listener = new TcpListener(
                config.port(),
                workerPool,
                router
        );

        // ---- 5. Start server LAST ----
        Thread serverThread = new Thread(listener, "tcp-listener");
        serverThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            listener.shutdown();
            workerPool.shutdown();
        }));
    }
}
