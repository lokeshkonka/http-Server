package server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private static final String URL = "jdbc:sqlite:aegis.db";

    static {
        init();
    }

    private Database() {}

    public static void init() {
        try {
            
            Class.forName("org.sqlite.JDBC");

            try (Connection c = DriverManager.getConnection(URL);
                 Statement s = c.createStatement()) {
                s.execute("PRAGMA journal_mode=WAL");
                s.execute("""
                    CREATE TABLE IF NOT EXISTS items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        created_at TEXT NOT NULL
                    )
                """);
            }

        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
