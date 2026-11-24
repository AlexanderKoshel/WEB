package restaurant;

import restaurant.dao.CatalogDao;
import restaurant.dao.OrderDao;
import restaurant.db.ConnectionProvider;
import restaurant.model.Order;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RestaurantConsole {
    private static final Logger LOG = Logger.getLogger(RestaurantConsole.class.getName());
    private final Scanner input = new Scanner(System.in, StandardCharsets.UTF_8);
    private final CatalogDao catalog;
    private final OrderDao orders;

    public RestaurantConsole(ConnectionProvider connections) {
        catalog = new CatalogDao(connections);
        orders = new OrderDao(connections);
    }

    public void run(String mode) {
        System.out.println("Ресторан · " + mode);
        System.out.println("""
                1. Меню
                2. Заказы клиента
                3. Подтверждённые заказы
                4. Оформить заказ
                5. Подтвердить заказ и выставить счёт
                6. Оплатить счёт
                7. Список клиентов
                0. Выход
                """);

        while (true) {
            try {
                String action = read("Действие: ");
                switch (action) {
                    case "0" -> { return; }
                    case "1" -> showMenu();
                    case "2" -> showOrders(orders.customerOrders(positiveInt(read("ID клиента: "))));
                    case "3" -> showOrders(orders.confirmedOrders());
                    case "4" -> placeOrder();
                    case "5" -> confirmOrder();
                    case "6" -> payOrder();
                    case "7" -> catalog.customers().forEach(
                            customer -> System.out.printf("%d. %s%n", customer.id(), customer.name()));
                    default -> System.out.println("Введите номер действия от 0 до 7.");
                }
            } catch (NoSuchElementException endOfInput) {
                return;
            } catch (IllegalArgumentException failure) {
                System.out.println(failure.getMessage());
            } catch (SQLException failure) {
                LOG.log(Level.WARNING, "Ошибка JDBC в операции ресторана", failure);
                if ("23000".equals(failure.getSQLState()) && failure.getErrorCode() == 1452) {
                    System.out.println("Не найдена связанная запись. Проверьте ID клиента.");
                } else {
                    System.out.println("Ошибка базы данных. Подробности в журнале.");
                }
            }
        }
    }

    private void showMenu() throws SQLException {
        for (var dish : catalog.menu()) {
            System.out.printf("%d. %s — %s BYN%n", dish.id(), dish.name(), dish.price());
        }
    }

    private void showOrders(List<Order> result) {
        if (result.isEmpty()) {
            System.out.println("Заказов нет.");
        }
        for (var order : result) {
            System.out.printf("Заказ №%d · %s · %s · %s%n",
                    order.id(), order.customer(), order.createdAt(), order.status());
            for (var line : order.lines()) {
                System.out.printf("  %s × %d по %s BYN%n", line.name(), line.quantity(), line.unitPrice());
            }
            System.out.printf("  Итого: %s BYN%n", order.total());
            if (order.issuedAt() != null) {
                System.out.println("  Счёт №" + order.id() + " от " + order.issuedAt());
            }
            if (order.paidAt() != null) {
                System.out.println("  Оплата: " + order.paidAt());
            }
        }
    }

    private void placeOrder() throws SQLException {
        int customerId = positiveInt(read("ID клиента: "));
        showMenu();
        var items = readItems(read("Блюда в формате ID:количество через пробел: "));
        System.out.println("Создан заказ №" + orders.place(customerId, items));
    }

    private void confirmOrder() throws SQLException {
        long id = positiveLong(read("Номер заказа: "));
        System.out.println(orders.confirm(id)
                ? "Заказ передан на кухню. Выставлен счёт №" + id
                : "Заказ не найден или уже подтверждён.");
    }

    private void payOrder() throws SQLException {
        long id = positiveLong(read("Номер счёта: "));
        System.out.println(orders.pay(id) ? "Оплата зарегистрирована." : "Счёт не найден или уже оплачен.");
    }

    private Map<Integer, Integer> readItems(String value) {
        var items = new LinkedHashMap<Integer, Integer>();
        for (String token : value.split("\\s+")) {
            String[] pair = token.split(":", -1);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Укажите блюда в формате 1:2 3:1.");
            }
            int id = positiveInt(pair[0]);
            if (items.putIfAbsent(id, positiveInt(pair[1])) != null) {
                throw new IllegalArgumentException("Укажите каждое блюдо одной позицией.");
            }
        }
        return items;
    }

    private int positiveInt(String value) {
        long number = positiveLong(value);
        if (number > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Введите число от 1 до " + Integer.MAX_VALUE + ".");
        }
        return (int) number;
    }

    private long positiveLong(String value) {
        try {
            long number = Long.parseLong(value);
            if (number > 0) {
                return number;
            }
        } catch (NumberFormatException ignored) {
            // Invalid signs, characters and overflow use the same message.
        }
        throw new IllegalArgumentException("Введите целое положительное число допустимого размера.");
    }

    private String read(String prompt) {
        System.out.print(prompt);
        return input.nextLine().strip();
    }
}
