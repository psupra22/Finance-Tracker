# 💰 Finance Tracker
A simple Java-based personal finance tracker that allows users to add and manage transactions, view summaries, and track their spending over time. The system provides a command-line interface for interaction.

## Features
- ➕ Add income and expense transactions
- ➖ Remove income and expense transactions
- 💰 View current balance
- 📋 View all transactions with categories
- 🗂️ Categorize transactions (e.g., Food, Rent, Utilities)
- 💾 Lightweight file-based storage (no database required)

## Prerequisites
- Java Development Kit (JDK) 8 or higher

## Installation
1. Clone this repository:
   ```sh
   git clone https://github.com/psupra22/Finance-Tracker
   cd Finance-Tracker
   ```

2. Compile the project:
   ```sh
   javac Main.java FinanceTracker.java FinanceUI.java Transaction.java
   ```

## Usage
1. Run the finance tracker:
   ```sh
   java Main
   ```

2. Follow the on-screen menu to:
   - Add transactions
   - Remove transactions
   - View balance
   - View all transactions
   - Exit the program

## Example
```sh
***********MENU***********
1. Add Expense
2. Add Income
3. Remove Expense
4. Remove Income
5. Check Balance
6. View Transactions
7. Exit
**************************

Enter a choice: 1

Enter category: rent
Enter amount: 250
expense added successfully
Press [ENTER] to continue...
```

## License
This project is licensed under the MIT License.

## Contact
For any inquiries, feel free to reach out at psupra521@gmail.com.
