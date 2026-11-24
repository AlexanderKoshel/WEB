package restaurant.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public record Order(long id, String customer, Timestamp createdAt, Timestamp confirmedAt,
                    Timestamp issuedAt, Timestamp paidAt, List<Line> lines) {
    public record Line(String name, int quantity, BigDecimal unitPrice) {}

    public BigDecimal total() {
        return lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String status() {
        if (paidAt != null) {
            return "Оплачен";
        }
        return confirmedAt == null ? "Новый" : "Подтверждён";
    }
}
