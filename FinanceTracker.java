/**
 * FinanceTracker - Handles all the logic for managing
 * transactions.
 * 
 * The Transaction record must be compiled before this class
 * can be compiled.
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FinanceTracker implements AutoCloseable {
    private List<Transaction> transactions;
    private final String fileName;

    /**
     * Method to make sure the updated list of transactions
     * is written to the file.
     * 
     * @throws IOException  Unhandled exception from updateFile().
     */
    @Override
    public void close() throws IOException {
        this.updateFile();
    }

    /**
     * Class constructor, intializes fileName and transactions list.
     * 
     * @param fileName  The name of the file that stores transactions.
     * @throws IOException   Unhandled exception from loadTransactions().
     */
    FinanceTracker(String fileName) throws IOException {
        this.fileName = fileName;
        this.transactions = new ArrayList<>();
        loadTransactions();
    }

    /**
     * Method for adding a new transaction.
     * 
     * @param type  The type of transaction, income or expense.
     * @param category  The category of the transaction(food, rent, work,...).
     * @param amount  The amount of money the transaction was.
     * @throws IllegalArgumentException  Exception for a invalid type.
     * @throws IllegalStateException  Exception for missing balance.
     */
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

    /**
     * Method for removing a existing transaction.
     * 
     * @param index  The index of the target transaction in the list.
     * @param type  The type of transaction, income or expense.
     * @throws IllegalArgumentException  Exception for a invalid type or index or no transaction.
     * @throws IllegalStateException  Exception for missing balance.
     */
    void removeTransaction(int index, TYPE type) 
        throws IllegalArgumentException, IllegalStateException {
        if (type != TYPE.INCOME && type != TYPE.EXPENSE) 
            throw new IllegalArgumentException("Invalid type: " + type);
        else
            this.hasType(type);

        if (index < 0 || index > (this.transactions.size() - 2) || !this.transactions.get(index).type().equals(type))
            throw new IllegalArgumentException("Invalid index");

        BigDecimal amount = this.transactions.get(index).amount();
        this.transactions.remove(index);  // Remove transaction
        index = this.transactions.size() - 1;  // Balance index

        if (!this.transactions.get(index).type().equals(TYPE.BALANCE))
            throw new IllegalStateException("No balance transaction found");

        BigDecimal oldBalance = this.transactions.get(index).amount();
        BigDecimal newBalance = type.equals(TYPE.EXPENSE) ? oldBalance.add(amount) : oldBalance.subtract(amount);
                
        this.transactions.remove(index);  // Remove old balance
        this.transactions.add(new Transaction(TYPE.BALANCE, LocalDateTime.now(), "balance", newBalance));  // Add new balance
    }

    /**
     * Method for printing balance and when it was last changed. 
     */
    void printBalance() {
        int index = this.transactions.size() - 1;
        if (!this.transactions.get(index).type().equals(TYPE.BALANCE)) {
            System.out.println("No balance found");
            return;
        }

        Transaction balance = transactions.get(index);
        System.out.println("Last updated: " + Transaction.formatter.format(balance.dateTime()));
        System.out.printf("Balance: $%.2f%n%n", balance.amount());
    }

    /**
     * Method for printing transactions of a given type.
     * 
     * @param type  The type of transaction, ALL, INCOME or EXPENSE.
     */
    void printTransactions(TYPE type) {
        if (!type.equals(TYPE.INCOME) && !type.equals(TYPE.EXPENSE)
                && !type.equals(TYPE.ALL)) {
            System.out.println("Invalid type: " + type + "\n");
            return;
        } else 
            this.hasType(type);
        
        if (type.equals(TYPE.ALL)) {
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

    /**
     * Helper method for check if the list has any transactions of a given type.
     * 
     * @param type  The type of transaction, INCOME or EXPENSE.
     * @throws IllegalArgumentException  Exception for if there is no transactions of the type.
     */
    private void hasType(TYPE type) throws IllegalArgumentException {
        if (type == TYPE.INCOME &&
            !this.transactions.stream().anyMatch(t -> t.type() == TYPE.INCOME)) 
            throw new IllegalArgumentException("You have no income\n");
        else if (type.equals(TYPE.EXPENSE) &&
            !this.transactions.stream().anyMatch(t -> t.type().equals(TYPE.EXPENSE)))
            throw new IllegalArgumentException("You have no expenses\n");
    }

    /**
     * Method that overwrites the file with a updated list of transactions.
     * 
     * @throws IOException  Exception for if there is a issue writing to the file.
     */
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

    /**
     * Method reads all the transactions in the given file and stores them in a list.
     * 
     * @throws IOException  Exception for if there is a issue reading the file.
     */
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
