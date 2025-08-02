import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String fileName = (args.length == 1) ? args[0] : "transactions.csv";
        Scanner userInput = new Scanner(System.in);

        try (FinanceTracker tracker = new FinanceTracker(fileName)) {
            FinanceUI ui = new FinanceUI(userInput, tracker);
            ui.run();
        } catch (IOException e) {
            System.err.println("Error loading file: " + fileName);
        }

        userInput.close();
    }
}
