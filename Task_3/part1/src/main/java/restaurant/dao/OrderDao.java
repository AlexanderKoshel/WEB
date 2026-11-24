package restaurant.dao;

import restaurant.db.ConnectionProvider;
import restaurant.model.Order;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class OrderDao {
    private static final String SELECT_ORDERS = """
            SELECT o.id, c.name AS customer, o.created_at, o.confirmed_at, i.issued_at, i.paid_at,
                   m.name AS dish, oi.quantity, oi.unit_price
            FROM restaurant_orders o
            JOIN customers c ON c.id = o.customer_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN menu_items m ON m.id = oi.menu_item_id
            LEFT JOIN invoices i ON i.order_id = o.id
            """;
    private static final String SELECT_CUSTOMER_ORDERS = SELECT_ORDERS
            + "WHERE o.customer_id = ? ORDER BY o.id, oi.menu_item_id";
    private static final String SELECT_CONFIRMED_ORDERS = SELECT_ORDERS
            + "WHERE o.confirmed_at IS NOT NULL ORDER BY o.id, oi.menu_item_id";
    private static final String INSERT_ORDER = "INSERT INTO restaurant_orders (customer_id) VALUES (?)";
    private static final String INSERT_LINE = """
            INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price)
            SELECT ?, id, ?, price FROM menu_items WHERE id = ?
            """;
    private static final String CONFIRM_ORDER = """
            UPDATE restaurant_orders SET confirmed_at = CURRENT_TIMESTAMP
            WHERE id = ? AND confirmed_at IS NULL
            """;
    private static final String ISSUE_INVOICE = "INSERT INTO invoices (order_id) VALUES (?)";
    private static final String PAY_INVOICE = """
            UPDATE invoices SET paid_at = CURRENT_TIMESTAMP WHERE order_id = ? AND paid_at IS NULL
            """;

    private final ConnectionProvider connections;

    public OrderDao(ConnectionProvider connections) {
        this.connections = connections;
    }

    public List<Order> customerOrders(int customerId) throws SQLException {
        return find(SELECT_CUSTOMER_ORDERS, customerId);
    }

    public List<Order> confirmedOrders() throws SQLException {
        return find(SELECT_CONFIRMED_ORDERS, null);
    }

    private List<Order> find(String sql, Integer customerId) throws SQLException {
        try (var lease = connections.borrow();
             var statement = lease.connection().prepareStatement(sql)) {
            if (customerId != null) {
                statement.setInt(1, customerId);
            }
            try (var rows = statement.executeQuery()) {
                return readOrders(rows);
            }
        }
    }

    private List<Order> readOrders(ResultSet rows) throws SQLException {
        var orders = new ArrayList<Order>();
        Order order = null;
        while (rows.next()) {
            long id = rows.getLong("id");
            if (order == null || order.id() != id) {
                order = new Order(id, rows.getString("customer"), rows.getTimestamp("created_at"),
                        rows.getTimestamp("confirmed_at"), rows.getTimestamp("issued_at"),
                        rows.getTimestamp("paid_at"), new ArrayList<>());
                orders.add(order);
            }
            order.lines().add(new Order.Line(
                    rows.getString("dish"), rows.getInt("quantity"), rows.getBigDecimal("unit_price")));
        }
        return orders;
    }

    public long place(int customerId, Map<Integer, Integer> items) throws SQLException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Добавьте хотя бы одно блюдо.");
        }
        return inTransaction(connection -> {
            long orderId;
            try (var order = connection.prepareStatement(INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {
                order.setInt(1, customerId);
                order.executeUpdate();
                try (var keys = order.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("База не вернула номер заказа");
                    }
                    orderId = keys.getLong(1);
                }
            }
            try (var line = connection.prepareStatement(INSERT_LINE)) {
                line.setLong(1, orderId);
                for (var item : items.entrySet()) {
                    line.setInt(2, item.getValue());
                    line.setInt(3, item.getKey());
                    if (line.executeUpdate() == 0) {
                        throw new IllegalArgumentException("Блюдо №" + item.getKey() + " не найдено.");
                    }
                }
            }
            return orderId;
        });
    }

    public boolean confirm(long orderId) throws SQLException {
        return inTransaction(connection -> {
            if (updateOrder(connection, CONFIRM_ORDER, orderId) == 0) {
                return false;
            }
            updateOrder(connection, ISSUE_INVOICE, orderId);
            return true;
        });
    }

    public boolean pay(long orderId) throws SQLException {
        try (var lease = connections.borrow()) {
            return updateOrder(lease.connection(), PAY_INVOICE, orderId) == 1;
        }
    }

    private int updateOrder(Connection connection, String sql, long orderId) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            return statement.executeUpdate();
        }
    }

    private <T> T inTransaction(Transaction<T> transaction) throws SQLException {
        try (var lease = connections.borrow()) {
            var connection = lease.connection();
            connection.setAutoCommit(false);
            try {
                T result = transaction.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException failure) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        }
    }

    @FunctionalInterface
    private interface Transaction<T> {
        T run(Connection connection) throws SQLException;
    }
}
