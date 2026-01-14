package server.item;

import server.db.Database;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class ItemRepository {

    public Item save(String name) {
        String sql = "INSERT INTO items(name, created_at) VALUES(?, ?)";

        try (Connection c = Database.get()) {
            c.setAutoCommit(false);

            try (PreparedStatement ps =
                         c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, name);
                ps.setString(2, Instant.now().toString());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        c.commit();
                        return new Item(rs.getInt(1), name);
                    }
                }

                throw new SQLException("No ID returned");

            } catch (Exception e) {
                c.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Item> findAll() {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT id, name FROM items ORDER BY id";

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(
                        new Item(
                                rs.getInt("id"),
                                rs.getString("name")
                        )
                );
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Item findById(int id) {
        String sql = "SELECT id, name FROM items WHERE id = ?";

        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Item(
                            rs.getInt("id"),
                            rs.getString("name")
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection c = Database.get()) {
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, id);
                boolean removed = ps.executeUpdate() > 0;
                c.commit();
                return removed;

            } catch (Exception e) {
                c.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
