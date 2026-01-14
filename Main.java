import concurrency.WorkerPool;
import http.HttpResponse;
import http.Router;
import server.ServerConfig;
import transport.TcpListener;

import server.item.*;
import server.db.Database;

public final class Main {

    public static void main(String[] args) {

        int port = 8080;
        int workers = Runtime.getRuntime().availableProcessors();
        int queueSize = 100;

        Database.init(); 
        
        // ---- 1. Build router FIRST ----
        Router router = new Router();

        router.register("GET", "/", req ->
                new HttpResponse(200, "Hello from AegisServer \n", null)
        );

        router.register("POST", "/echo", req ->
                new HttpResponse(200, new String(req.body), null)
        );

        // ---- 2. Build domain services ----
        ItemRepository repo = new ItemRepository();
        ItemService service = new ItemService(repo);

        router.register("POST", "/items", ItemHandlers.create(service));
        router.register("GET", "/items", ItemHandlers.list(service));
        router.register("GET", "/items/", ItemHandlers.get(service));
        router.register("DELETE", "/items/", ItemHandlers.delete(service));

        // ---- 3. Build config ----
        ServerConfig config = new ServerConfig(port, workers, queueSize);

        // ---- 4. Build worker pool ----
        WorkerPool workerPool = new WorkerPool(
                config.workerCount(),
                config.queueSize()
        );

        // ---- 5. Build listener ----
        TcpListener listener = new TcpListener(
                config.port(),
                workerPool,
                router
        );

        // ---- 6. Start server LAST ----
        Thread serverThread = new Thread(listener, "tcp-listener");
        serverThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            listener.shutdown();
            workerPool.shutdown();
        }));
    }
}
