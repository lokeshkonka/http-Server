package server.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiter {

    private static final class Bucket {
        volatile long windowStartMs;
        volatile int count;

        Bucket(long now) {
            this.windowStartMs = now;
            this.count = 0;
        }
    }

    private final long windowMs;
    private final int maxRequests;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(long windowMs, int maxRequests) {
        this.windowMs = windowMs;
        this.maxRequests = maxRequests;
    }

    /** @return true if allowed, false if rate-limited */
    public boolean allow(String key) {
        long now = System.currentTimeMillis();

        Bucket b = buckets.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStartMs >= windowMs) {
                return new Bucket(now);
            }
            return existing;
        });

        synchronized (b) {
            if (b.count >= maxRequests) {
                return false;
            }
            b.count++;
            return true;
        }
    }
}
