package transport;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public final class ConnectionHandler implements Runnable {

    private static final int READ_TIMEOUT_MS = 5_000;
    private static final int BUFFER_SIZE = 1024;

    private final Socket socket;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(READ_TIMEOUT_MS);

            InputStream in = socket.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];

            int read;
            while ((read = in.read(buffer)) != -1) {
                // TEMP: just prove bytes are received
                System.out.write(buffer, 0, read);
                System.out.flush();
            }

        } catch (SocketTimeoutException e) {
            // client too slow — drop connection
        } catch (IOException e) {
            // connection error
        } finally {
            closeQuietly();
        }
    }

    private void closeQuietly() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
