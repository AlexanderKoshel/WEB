package restaurant.pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import restaurant.db.ConnectionProvider;
import restaurant.db.DatabaseConfig;

import java.sql.SQLException;

public final class DataSource implements ConnectionProvider, AutoCloseable {
    private final HikariDataSource source;

    public DataSource(DatabaseConfig database, int capacity, long waitMillis) {
        var config = new HikariConfig();
        config.setPoolName("RestaurantHikari");
        config.setJdbcUrl(database.url());
        config.setUsername(database.user());
        config.setPassword(database.password());
        config.setMaximumPoolSize(capacity);
        config.setConnectionTimeout(waitMillis);
        source = new HikariDataSource(config);
    }

    @Override
    public Lease borrow() throws SQLException {
        return Lease.closing(source.getConnection());
    }

    @Override
    public void close() {
        source.close();
    }
}
