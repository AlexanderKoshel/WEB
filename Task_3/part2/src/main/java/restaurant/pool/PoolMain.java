package restaurant.pool;

import restaurant.RestaurantConsole;
import restaurant.db.DatabaseConfig;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PoolMain {
    private static final Logger LOG = Logger.getLogger(PoolMain.class.getName());
    private static final int CAPACITY = 4;
    private static final long WAIT_MILLIS = 3_000;

    public static void main(String[] args) throws IOException {
        if (args.length != 1 || !Set.of("custom", "hikari").contains(args[0])) {
            throw new IllegalArgumentException("Укажите режим пула: custom или hikari.");
        }

        var config = DatabaseConfig.load();
        try {
            if ("custom".equals(args[0])) {
                try (var pool = new MyConnectionPool(config, CAPACITY, WAIT_MILLIS)) {
                    new RestaurantConsole(pool).run("MyConnectionPool");
                }
            } else {
                try (var pool = new DataSource(config, CAPACITY, WAIT_MILLIS)) {
                    new RestaurantConsole(pool).run("HikariCP");
                }
            }
        } catch (RuntimeException failure) {
            LOG.log(Level.SEVERE, "Ошибка запуска или завершения пула.", failure);
            throw failure;
        }
    }
}
