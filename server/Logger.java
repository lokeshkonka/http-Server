package server;

public final class Logger {

    private Logger() {}

    public static void request(
            String method,
            String path,
            int status,
            long timeMs
    ) {
        System.out.println(
                "[REQ] method=" + method +
                " path=" + path +
                " status=" + status +
                " time_ms=" + timeMs
        );
    }

    public static void error(int status, String message) {
        System.err.println(
                "[ERR] status=" + status +
                " message=\"" + message + "\""
        );
    }
}
