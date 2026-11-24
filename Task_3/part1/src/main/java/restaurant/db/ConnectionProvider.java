package restaurant.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionProvider {
    Lease borrow() throws SQLException;

    interface Lease extends AutoCloseable {
        Connection connection();

        @Override
        void close() throws SQLException;

        static Lease closing(Connection connection) {
            return new Lease() {
                @Override
                public Connection connection() {
                    return connection;
                }

                @Override
                public void close() throws SQLException {
                    connection.close();
                }
            };
        }
    }
}
