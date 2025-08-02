import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class FinanceTracker implements AutoCloseable {
    private ArrayList<Transaction> transactions;
    private final String fileName;

    @Override
    public void close() throws IOException {
        this.updateFile();
    }

    FinanceTracker(String fileName) throws IOException {
        this.fileName = fileName;
        this.transactions = new ArrayList<>();
        loadTransactions();
    }

    void addTransaction(TYPE type, String category, BigDecimal amount)
        throws IllegalArgumentException, IllegalStateException {
        if (type != TYPE.INCOME && type != TYPE.EXPENSE)
            throw new IllegalArgumentException("Invalid type: " + type);

        int index = this.transactions.size() - 1;
        if (!this.transactions.get(index).type().equals(TYPE.BALANCE))
            throw new IllegalStateException("No balance transaction found");

        BigDecimal oldBalance = this.transactions.get(index).amount();
        BigDecimal newBalance = type == TYPE.EXPENSE ? oldBalance.subtract(amount) : oldBalance.add(amount);

        this.transactions.remove(index);  // Remove old balance
        this.transactions.add(new Transaction(TYPE.BALANCE, LocalDateTime.now(), "balance", newBalance));  // Add new balance
        this.transactions.add(index, new Transaction(type, LocalDateTime.now(), category, amount));  // Add transtaction
    }

    void removeTransaction(int index, TYPE type) throws IllegalArgumentException {
        if (type != TYPE.INCOME && type != TYPE.EXPENSE) 
            throw new IllegalArgumentException("Invalid type: " + type);
        else if (type == TYPE.INCOME &&
            !this.transactions.stream().anyMatch(t -> t.type() == TYPE.INCOME)) {
            System.out.println("You have no income\n");
            return;
        } else if (type.equals(TYPE.EXPENSE) &&
            !this.transactions.stream().anyMatch(t -> t.type().equals(TYPE.EXPENSE))) {
            System.out.println("You have no expenses\n");
            return;
        }

        if (index < 0 || index > (this.transactions.size() - 2) || !this.transactions.get(index).type().equals(type))
            throw new IllegalArgumentException("Invalid index");

        BigDecimal amount = this.transactions.get(index).amount();
        this.transactions.remove(index);
        index = this.transactions.size() - 1;

        if (!this.transactions.get(index).type().equals(TYPE.BALANCE))
            throw new IllegalStateException("No balance transaction found");

        BigDecimal oldBalance = this.transactions.get(index).amount();
        BigDecimal newBalance = type.equals(TYPE.EXPENSE) ? oldBalance.add(amount) : oldBalance.subtract(amount);
                
        this.transactions.remove(index);  // Remove old balance
        this.transactions.add(new Transaction(TYPE.BALANCE, LocalDateTime.now(), "balance", newBalance));  // Add new balance
    }

    void printBalance() {
        int index = this.transactions.size() - 1;
        if (!this.transactions.get(index).type().equals(TYPE.BALANCE)) {
            System.err.println("No balance found");
            return;
        }

        Transaction balance = transactions.get(index);
        System.out.println("Last updated: " + Transaction.formatter.format(balance.dateTime()));
        System.out.printf("Balance: $%.2f%n%n", balance.amount());
    }

    void printTransactions(TYPE type) {
        if (!type.equals(TYPE.INCOME) && !type.equals(TYPE.EXPENSE)
                && !type.equals(TYPE.ALL)) {
            System.err.println("Invalid type: " + type + "\n");
            return;
        } else if (type.equals(TYPE.ALL)) {
            for (int i = 0; i < this.transactions.size(); i++)
                if (!this.transactions.get(i).type().equals(TYPE.BALANCE))
                    System.out.println(i + " " + this.transactions.get(i).formattedString());
        } else if (type.equals(TYPE.INCOME) || type.equals(TYPE.EXPENSE)) {
            for (int i = 0; i < this.transactions.size(); i++) {
                if (transactions.get(i).type().equals(type))
                    System.out.println(i + " " + transactions.get(i).formattedString());
            }
        } else
            System.err.println("Unknown type\n");
    }

    private void updateFile() throws IOException {
        try (RandomAccessFile transactionFile = new RandomAccessFile(this.fileName, "rw")) {
            transactionFile.setLength(0);
            transactionFile.seek(0);
            for (Transaction transaction : this.transactions)
                transactionFile.write((transaction + "\n").getBytes());
        } catch (FileNotFoundException e) {
            System.err.println("transactions.csv couldn't be found to read transactions");
        }
    }

    private void loadTransactions() throws IOException {
        try (RandomAccessFile transactionFile = new RandomAccessFile(this.fileName, "rw")){
            if (transactionFile.length() == 0) {
                this.transactions.add(new Transaction(TYPE.BALANCE, LocalDateTime.now(), 
                                                "balance", BigDecimal.ZERO));
                return;
            }

            transactionFile.seek(0);
            
            String line;
            while ((line = transactionFile.readLine()) != null) {
                try {
                    String[] parts = line.split(",", -1);
                    if (parts.length != 4)
                        continue;
                    
                    TYPE type = switch (parts[0].trim().toLowerCase()){
                        case "expense" -> TYPE.EXPENSE;
                        case "income" -> TYPE.INCOME;
                        case "balance" -> TYPE.BALANCE;
                        default -> throw new IllegalArgumentException("Unknown type: " + parts[0]);
                    };
                    LocalDateTime dateTime = LocalDateTime.parse(parts[1].trim(), Transaction.formatter);
                    String category = parts[2].trim();
                    double dAmount = Double.parseDouble(parts[3].trim());
                    BigDecimal amount = new BigDecimal(dAmount).setScale(2, RoundingMode.FLOOR);

                    this.transactions.add(new Transaction(type, dateTime, category, amount));
                } catch (Exception e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(fileName + " couldn't be found to read transactions");
        }
    }
}
