import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Transaction {
    // Transaction Category(income, expense, balance), Date-Time, Category, Amount
    private String type, category;
    private LocalDateTime dateTime;
    private double amount;
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    Transaction(String type, LocalDateTime dateTime, String category, double amount) {
        this.type = type;
        this.dateTime = dateTime.truncatedTo(ChronoUnit.MINUTES);
        this.category = category;
        this.amount = amount;
    }

    String getType() { return this.type; }
    LocalDateTime getDateTime() { return this.dateTime; }
    void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    String getCategory() { return this.category; }
    double getAmount() { return this.amount; }
    void setAmount(double amount) { this.amount = amount; }

    String formattedString() {
        return String.format("%s | %s | %s | $%.2f",
                             type.toUpperCase(), formatter.format(dateTime), category.toUpperCase(), amount);
    }

    @Override
    public String toString() {
        return type + "," + formatter.format(dateTime) + "," + category + "," + amount;
    }

    @Override
    public boolean equals(Object transaction) {
        if (this == transaction) return true;
        if (transaction == null || getClass() != transaction.getClass()) return false;

        Transaction that = (Transaction) transaction;
        return this.type.equals(that.type) &&
            formatter.format(this.dateTime).equals(formatter.format(that.dateTime)) &&
            this.category.equals(that.category) &&
            Double.compare(this.amount, that.amount) == 0;
    }
}
