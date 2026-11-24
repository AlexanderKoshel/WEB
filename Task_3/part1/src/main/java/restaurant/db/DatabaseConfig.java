package restaurant.db;

import java.io.IOException;
import java.util.Properties;

public record DatabaseConfig(String url, String user, String password) {
    public static DatabaseConfig load() throws IOException {
        var properties = new Properties();
        try (var input = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (input == null) {
                throw new IOException("Не найден файл database.properties");
            }
            properties.load(input);
        }
        return new DatabaseConfig(
                System.getenv().getOrDefault("DB_URL", properties.getProperty("db.url")),
                System.getenv().getOrDefault("DB_USER", properties.getProperty("db.user")),
                System.getenv().getOrDefault("DB_PASSWORD", properties.getProperty("db.password")));
    }
}
