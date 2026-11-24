package restaurant.dao;

import restaurant.db.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class CatalogDao {
    private static final String SELECT_MENU = "SELECT id, name, price FROM menu_items ORDER BY id";
    private static final String SELECT_CUSTOMERS = "SELECT id, name FROM customers ORDER BY id";
    private final ConnectionProvider connections;

    public CatalogDao(ConnectionProvider connections) {
        this.connections = connections;
    }

    public record MenuItem(int id, String name, BigDecimal price) {}
    public record Customer(int id, String name) {}

    public List<MenuItem> menu() throws SQLException {
        var items = new ArrayList<MenuItem>();
        try (var lease = connections.borrow();
             var statement = lease.connection().prepareStatement(SELECT_MENU);
             var rows = statement.executeQuery()) {
            while (rows.next()) {
                items.add(new MenuItem(rows.getInt("id"), rows.getString("name"), rows.getBigDecimal("price")));
            }
        }
        return items;
    }

    public List<Customer> customers() throws SQLException {
        var customers = new ArrayList<Customer>();
        try (var lease = connections.borrow();
             var statement = lease.connection().prepareStatement(SELECT_CUSTOMERS);
             var rows = statement.executeQuery()) {
            while (rows.next()) {
                customers.add(new Customer(rows.getInt("id"), rows.getString("name")));
            }
        }
        return customers;
    }
}
