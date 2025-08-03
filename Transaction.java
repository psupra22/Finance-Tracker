/**
 * Transaction - A record that stores all information about a transaction.
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Transaction Category(income, expense, balance), Date-Time, Category, Amount
public record Transaction(TYPE type, LocalDateTime dateTime, String category, BigDecimal amount) {
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    Transaction with(LocalDateTime dateTime, BigDecimal amount){
        return new Transaction(type, dateTime, category, amount);
    }

    /**
     * Method for returning data in a formatted string.
     * 
     * @return  Formatted string of transaction information.
     */
    String formattedString() {
        return String.format("%s | %s | %s | $%.2f",
                             type.toString().toUpperCase(), formatter.format(dateTime), category.toUpperCase(), amount);
    }

    /**
     * Method for returning transaction as a string for writing.
     * 
     * @return  String for a csv file.
     */
    @Override
    public String toString() {
        return type + "," + formatter.format(dateTime) + "," + category + "," + amount;
    }
}

enum TYPE {
    ALL("all"), INCOME("income"), EXPENSE("expense"), BALANCE("balance");
    private final String type;
    TYPE(String type) { this.type = type; }
    
    @Override
    public String toString() {
        return type;
    }
}