import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class FinanceTracker implements AutoCloseable {
    private RandomAccessFile transactionFile;
    private Scanner userInput;
    private ArrayList<Transaction> transactions;

    @Override
    public void close() throws IOException {
        this.updateFile();
        this.transactionFile.close();
    }

    FinanceTracker(RandomAccessFile transactionFile, Scanner userInput) throws IOException {
        this.transactionFile = transactionFile;
        this.userInput = userInput;
        this.transactions = new ArrayList<>();
        loadTransactions();
    }

    private static void displayMenu() {
        System.out.println("""
                ***********MENU***********
                1. Add Expense
                2. Add Income
                3. Remove Expense
                4. Remove Income
                5. Check Balance
                6. View Transactions
                7. Exit
                **************************
                """);
        System.out.print("Enter a choice: ");
    }

    static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    int getChoice() {
        while (true) {
            try {
                displayMenu();
                int choice = this.userInput.nextInt();
                if (choice < 1 || choice > 7)
                    throw new IllegalArgumentException("You must choose a number 1-7.\n");
                
                return choice;
            } catch (InputMismatchException e) {
                clearConsole();
                System.out.println("Input must be a integer.\n");
            } catch (IllegalArgumentException e) {
                clearConsole();
                System.out.println(e.getMessage());
            } finally {
                this.userInput.nextLine();
            }
        }
    }

    void addTransaction(String type) {
        if (!type.equals("income") && !type.equals("expense")) {
            System.out.println("Invalid type: " + type + "\n");
            return;
        }

        while (true) {
            try {
                System.out.printf("Enter category of %s: ",
                    type.equals("expense") ? "expense(food, rent,...)" : "income(sale, work,...)");
                String category = this.userInput.nextLine();

                System.out.printf("Enter %s amount: ",
                    type.equals("expense") ? "expense" : "income");
                double amount = type.equals("expense") ? 
                                    Math.abs(this.userInput.nextDouble()) : this.userInput.nextDouble();
                amount = Math.floor(amount * 100.0) / 100.0;  // Round down to 2 decimals

                if (type.equals("income") && amount <= 0)
                    throw new IllegalArgumentException("Income must be positive");

                int index = this.transactions.size() - 1;
                if (!this.transactions.get(index).getType().equals("balance"))
                    throw new IllegalStateException("No balance transaction found");

                double oldBalance = this.transactions.get(index).getAmount();
                double newBalance = type.equals("expense") ? oldBalance - amount : oldBalance + amount;

                this.transactions.get(index).setAmount(newBalance);  // Update balance
                this.transactions.get(index).setDateTime(LocalDateTime.now());  // Update balance date
                this.transactions.add(index, new Transaction(type, LocalDateTime.now(), category, amount));  // Add transtaction
                break;
            } catch (InputMismatchException e) {
                clearConsole();
                System.out.println("Invalid input");
            } catch (IllegalArgumentException e) {
                clearConsole();
                System.out.println(e.getMessage());
            } catch (IllegalStateException e) {
                clearConsole();
                System.out.println(e.getMessage());
            } finally {
                userInput.nextLine();
            }
        }

        System.out.printf("%s added%n%n", type.equals("expense") ? "Expense" : "Income");
    }

    void removeTransaction(String type) {
        if (!type.equals("income") && !type.equals("expense")) {
            System.out.println("Invalid type: " + type + "\n");
            return;
        } else if (type.equals("income") &&
            !this.transactions.stream().anyMatch(t -> t.getType().equals("income"))) {
            System.out.println("You have no income\n");
            return;
        } else if (type.equals("expense") &&
            !this.transactions.stream().anyMatch(t -> t.getType().equals("expense"))) {
            System.out.println("You have no expenses\n");
            return;
        }

        while (true) {
            try {
                System.out.printf("************************%s************************%n",
                                   type.equals("income") ? "*INCOME*" : "EXPENSES");
                this.printTransactions(type);
                System.out.println("********************************************************\n");
                System.out.print("Enter the date-time in this format(yyyy-mm-dd HH:MM): ");
                String dTime = this.userInput.nextLine();
                LocalDateTime dateTime = LocalDateTime.parse(dTime, Transaction.formatter);

                System.out.print("Enter the transaction category: ");
                String category = this.userInput.nextLine().toLowerCase();

                System.out.print("Enter the transaction amount: ");
                double amount = Math.abs(this.userInput.nextDouble());

                if (!(this.transactions.remove(new Transaction(type, dateTime, category, amount))))
                    throw new IllegalArgumentException("Transaction not found");
                
                int index = this.transactions.size() - 1;
                if (!this.transactions.get(index).getType().equals("balance"))
                    throw new IllegalStateException("No balance transaction found");

                double oldBalance = this.transactions.get(index).getAmount();
                double newBalance = type.equals("expense") ? oldBalance + amount : oldBalance - amount;
                this.transactions.get(index).setAmount(newBalance);  // Update balance amount
                this.transactions.get(index).setDateTime(LocalDateTime.now());  // Update balance date
                break;
            } catch (IllegalArgumentException e) {
                clearConsole();
                System.out.println(e.getMessage());
            } catch (IllegalStateException e) {
                clearConsole();
                System.out.println(e.getMessage());
            } catch(DateTimeParseException e) {
                clearConsole();
                System.out.println("Invalid date-time format");
            } catch (InputMismatchException e) {
                clearConsole();
                System.out.println("Invalid type");
            } finally {
                this.userInput.nextLine();
            }
        }
        
        clearConsole();
        System.out.printf("%s removed%n%n", type.equals("expense") ? "Expense" : "Income");
    }

    void printBalance() {
        int index = this.transactions.size() - 1;
        if (!this.transactions.get(index).getType().equals("balance")) {
            System.out.println("No balance found");
            return;
        }

        Transaction balance = transactions.get(index);
        clearConsole();
        System.out.println("Last updated: " + Transaction.formatter.format(balance.getDateTime()));
        System.out.printf("Balance: $%.2f%n%n", balance.getAmount());
    }

    void printTransactions(String type) {
        if (!type.equals("income") && !type.equals("expense")
                && !type.equals("all")) {
            System.out.println("Invalid type: " + type + "\n");
            return;
        } else if (type.equals("all")) {
            for (Transaction transaction : this.transactions)
                if (!transaction.getType().equals("balance"))
                    System.out.println(transaction.formattedString());
        } else if (type.equals("income") || type.equals("expense")) {
            for (Transaction transaction : this.transactions) {
                if (transaction.getType().equals(type))
                    System.out.println(transaction.formattedString());
            }
        } else
            System.out.println("Unknown type\n");
    }

    private void updateFile() throws IOException {
        this.transactionFile.setLength(0);
        this.transactionFile.seek(0);
        for (Transaction transaction : this.transactions)
            this.transactionFile.write((transaction + "\n").getBytes());
    }

    private void loadTransactions() throws IOException {
        if (this.transactionFile.length() == 0) {
            this.transactions.add(new Transaction("balance", LocalDateTime.now(), 
                                             "balance", 0.00));
            return;
        }

        this.transactionFile.seek(0);
        
        String line;
        while ((line = this.transactionFile.readLine()) != null) {
            try {
                String[] parts = line.split(",", -1);
                if (parts.length != 4)
                    continue;
                
                String type = parts[0].trim();
                LocalDateTime dateTime = LocalDateTime.parse(parts[1].trim(), Transaction.formatter);
                String category = parts[2].trim();
                double amount = Double.parseDouble(parts[3].trim());

                this.transactions.add(new Transaction(type, dateTime, category, amount));
            } catch (Exception e) {
                System.out.println("Skipping malformed line: " + line);
            }
        }
    }
}
