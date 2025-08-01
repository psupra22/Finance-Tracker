import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to your personal finance tracker.\n");

        // Transaction Category(income, expense, balance), Date-Time, Category, Amount
        try (RandomAccessFile transactionFile = new RandomAccessFile("transactions.csv", "rw");
             Scanner userInput = new Scanner(System.in);
             FinanceTracker tracker = new FinanceTracker(transactionFile, userInput)) {
            boolean running = true;
            while (running) {
                int choice = tracker.getChoice();
                FinanceTracker.clearConsole();
                // Enchanced switch that calls appropriate method
                switch (choice) {
                    case 1 -> tracker.addTransaction("expense");
                    case 2 -> tracker.addTransaction("income");
                    case 3 -> tracker.removeTransaction("expense");
                    case 4 -> tracker.removeTransaction("income");
                    case 5 -> tracker.printBalance();
                    case 6 -> {
                        System.out.println("**********************TRANSACTIONS**********************");
                        tracker.printTransactions("all");
                        System.out.println("********************************************************\n");
                    }
                    case 7 -> running = false;
                    default -> System.out.println("Invalid Choice");
                }
            }
        } catch (IOException e) {
            System.out.println("Error with transactions.csv");
        }
    }
}
