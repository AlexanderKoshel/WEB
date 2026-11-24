package restaurant.db;

import java.sql.DriverManager;
import java.sql.SQLException;

public final class JdbcConnector implements ConnectionProvider {
    private final DatabaseConfig config;

    public JdbcConnector(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public Lease borrow() throws SQLException {
        return Lease.closing(DriverManager.getConnection(config.url(), config.user(), config.password()));
    }
}
