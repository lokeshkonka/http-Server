import concurrency.WorkerPool;
import http.HttpResponse;
import http.Router;
import server.ServerConfig;
import transport.TcpListener;

import server.item.*;
import server.db.Database;
import server.ratelimit.RateLimiter;

import server.staticfiles.StaticFileHandler;

public final class Main {

    public static void main(String[] args) {

        int port = 8080;
        int workers = Runtime.getRuntime().availableProcessors();
        int queueSize = 100;

        // ---- DB init ----
        Database.init();

        // ---- Router ----
        Router router = new Router();

        router.register("GET", "/static/", StaticFileHandler.assets());
        router.register("GET", "/", StaticFileHandler.index());
        router.register("POST", "/echo", req ->
                new HttpResponse(200, new String(req.body), null)
        );

        ItemRepository repo = new ItemRepository();
        ItemService service = new ItemService(repo);

        router.register("POST", "/items", ItemHandlers.create(service));
        router.register("GET", "/items", ItemHandlers.list(service));
        router.register("GET", "/items/", ItemHandlers.get(service));
        router.register("DELETE", "/items/", ItemHandlers.delete(service));

        RateLimiter rateLimiter = new RateLimiter(60_000, 60);
        ServerConfig config = new ServerConfig(port, workers, queueSize);

        WorkerPool workerPool = new WorkerPool(
                config.workerCount(),
                config.queueSize()
        );
        TcpListener listener = new TcpListener(
                config.port(),
                workerPool,
                router,
                rateLimiter
        );

        Thread serverThread = new Thread(listener, "tcp-listener");
        serverThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
            listener.shutdown();
            workerPool.shutdown();
            System.out.println("Server shut down. Complete");
        }));
    }
}
