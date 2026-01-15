package concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class WorkerPool {

    private final BlockingQueue<Runnable> queue;
    private final Thread[] workers;
    private volatile boolean running = true;

    public WorkerPool(int workerCount, int queueSize) {
        if (workerCount <= 0 || queueSize <= 0) {
            throw new IllegalArgumentException("workerCount and queueSize must be > 0");
        }

        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.workers = new Thread[workerCount];

        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Thread(this::workerLoop, "worker-" + i);
            workers[i].start();
        }
    }

    private void workerLoop() {
        while (running) {
            try {
                Runnable task = queue.take(); // blocks
                task.run();
            } catch (InterruptedException e) {
                // shutdown signal
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                // never let worker die silently
                t.printStackTrace();
            }
        }
    }

    /**
     *@return true if accepted, false if rejected (queue full or shutting down)
     */
    public boolean submit(Runnable task) {
        if (!running) return false;
        return queue.offer(task); // non-blocking, bounded
    }

public void shutdown() {
    running = false;

    for (Thread t : workers) {
        t.interrupt();
    }

    for (Thread t : workers) {
        try {
            t.join();
        } catch (InterruptedException ignored) {
        }
    }
}

}
