package restaurant;

import restaurant.db.DatabaseConfig;
import restaurant.db.JdbcConnector;

import java.io.IOException;

public final class JdbcMain {
    public static void main(String[] args) throws IOException {
        new RestaurantConsole(new JdbcConnector(DatabaseConfig.load())).run("JdbcConnector");
    }
}
