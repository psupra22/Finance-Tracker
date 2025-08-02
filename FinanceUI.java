import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.InputMismatchException;
import java.util.Scanner;

public class FinanceUI {
    private final Scanner scanner;
    private final FinanceTracker tracker;

    public FinanceUI(Scanner scanner, FinanceTracker tracker) {
        this.scanner = scanner;
        this.tracker = tracker;
    }

    public void run() {
        System.out.println("Welcome to your personal finance tracker.\n");
        boolean running = true;
        while (running) {
            clearConsole();
            int choice = getChoice();

            switch (choice) {
                case 1 -> handleAdd(TYPE.EXPENSE);
                case 2 -> handleAdd(TYPE.INCOME);
                case 3 -> handleRemove(TYPE.EXPENSE);
                case 4 -> handleRemove(TYPE.INCOME);
                case 5 -> { 
                    clearConsole();
                    tracker.printBalance();
                }
                case 6 -> {
                    clearConsole();
                    System.out.println("**********************TRANSACTIONS**********************");
                    tracker.printTransactions(TYPE.ALL);
                    System.out.println("********************************************************\n");
                }
                case 7 -> running = false;
                default -> System.out.println("Invalid choice.");
            }

            System.out.println("Press [ENTER] to continue...");
            scanner.nextLine();
        }
    }

    private int getChoice() {
        while (true) {
            try {
                displayMenu();
                int choice = scanner.nextInt();
                if (choice < 1 || choice > 7)
                    throw new IllegalArgumentException("Choice must be between 1-7");
                return choice;
            } catch (InputMismatchException e) {
                clearConsole();
                System.err.println("Input must be an integer.");
            } catch (IllegalArgumentException e) {
                clearConsole();
                System.err.println(e.getMessage());
            } finally {
                scanner.nextLine();
            }
        }
    }

    private void handleAdd(TYPE type) {
        try {
            clearConsole();
            System.out.print("Enter category: ");
            String category = scanner.nextLine();

            System.out.print("Enter amount: ");
            double rawAmount = scanner.nextDouble();
            BigDecimal amount = new BigDecimal(Math.abs(rawAmount)).setScale(2, RoundingMode.FLOOR);

            if (type == TYPE.INCOME && amount.doubleValue() <= 0)
                throw new IllegalArgumentException("Income must be positive");

            tracker.addTransaction(type, category, amount);
            System.out.printf("%s added successfully%n", type);
        } catch (InputMismatchException e) {
            System.err.println("Amount must be a number.");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } finally {
            scanner.nextLine();
        }
    }

    private void handleRemove(TYPE type) {
        clearConsole();
        System.out.printf("************************%s************************%n",
                                   type.equals(TYPE.INCOME) ? "*INCOME*" : "EXPENSES");
        tracker.printTransactions(type);
        System.out.println("********************************************************\n");
        System.out.print("Enter transaction index to remove: ");
        try {
            int index = scanner.nextInt();
            tracker.removeTransaction(index, type);
            System.out.println("Transaction removed.");
        } catch (Exception e) {
            System.err.println("Could not remove: " + e.getMessage());
        } finally {
            scanner.nextLine();
        }
    }

    private void displayMenu() {
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
        System.out.print("Enter your choice: ");
    }

    private void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
