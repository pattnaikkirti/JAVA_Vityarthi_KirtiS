package com.expensetracker.main;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.dao.UserDAO;
import com.expensetracker.exception.ExpenseTrackerException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.service.BudgetMonitor;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.UserService;
import com.expensetracker.util.DBConnection;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    private static UserService userService;
    private static ExpenseService expenseService;
    private static User currentUser = null;
    private static BudgetMonitor budgetMonitor;
    private static Thread monitorThread;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==========================================");
        System.out.println("   Expense Tracker with Budget Alerts");
        System.out.println("==========================================\n");

        try {
            DBConnection.initializeDatabase();

            ExpenseDAO expenseDAO = new ExpenseDAO();
            UserDAO userDAO = new UserDAO();
            expenseService = new ExpenseService(expenseDAO);
            userService = new UserService(userDAO, expenseService);

            budgetMonitor = new BudgetMonitor(userService, expenseService, 30);

            loginOrRegister(scanner);

            boolean running = true;
            while (running) {
                printMenu();
                int choice = readInt(scanner, "Enter choice: ");

                switch (choice) {
                    case 1: addExpense(scanner); break;
                    case 2: viewAllExpenses(); break;
                    case 3: viewByCategory(scanner); break;
                    case 4: monthlyReport(scanner); break;
                    case 5: categoryWiseReport(); break;
                    case 6: budgetStatus(); break;
                    case 7: deleteExpense(scanner); break;
                    case 8: updateBudget(scanner); break;
                    case 9: switchUser(scanner); break;
                    case 10:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (ExpenseTrackerException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            stopMonitor();
            DBConnection.closeConnection();
            scanner.close();
            System.out.println("\nGoodbye!");
        }
    }

    private static void printMenu() {
        System.out.println("\n------ MENU [" + currentUser.getName() + "] ------");
        System.out.println(" 1. Add Expense");
        System.out.println(" 2. View All Expenses");
        System.out.println(" 3. View by Category");
        System.out.println(" 4. Monthly Report");
        System.out.println(" 5. Category-wise Report");
        System.out.println(" 6. Budget Status");
        System.out.println(" 7. Delete Expense");
        System.out.println(" 8. Update Budget");
        System.out.println(" 9. Switch User");
        System.out.println("10. Exit");
        System.out.println("-------------------------------");
    }

    private static void loginOrRegister(Scanner scanner) throws ExpenseTrackerException {
        while (currentUser == null) {
            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Name cannot be empty.");
                continue;
            }

            Optional<User> existing = userService.getUserByName(name);
            if (existing.isPresent()) {
                currentUser = existing.get();
                System.out.println("Welcome back, " + currentUser.getName() + "! " + currentUser);
            } else {
                double budget = readDouble(scanner, "New user! Set your monthly budget (Rs.): ");
                currentUser = userService.registerUser(name, budget);
                System.out.println("Registered! " + currentUser);
            }
        }
        startMonitor();
    }

    private static void addExpense(Scanner scanner) {
        try {
            System.out.println("\nCategories:");
            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                System.out.println("  " + (i + 1) + ". " + cats[i].getDisplayName());
            }

            int catChoice = readInt(scanner, "Select category (1-" + cats.length + "): ");
            if (catChoice < 1 || catChoice > cats.length) {
                System.out.println("Invalid category.");
                return;
            }
            Category category = cats[catChoice - 1];

            double amount = readDouble(scanner, "Enter amount (Rs.): ");
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }

            System.out.print("Date (YYYY-MM-DD, or press Enter for today): ");
            String dateStr = scanner.nextLine().trim();
            LocalDate date;
            if (dateStr.isEmpty()) {
                date = LocalDate.now();
            } else {
                try {
                    date = LocalDate.parse(dateStr);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Use YYYY-MM-DD.");
                    return;
                }
            }

            Expense expense = new Expense(currentUser.getId(), category, amount, date);
            expenseService.addExpense(expense);
            System.out.println("Expense added: " + category.getDisplayName() + " Rs." + String.format("%.2f", amount));

            checkBudgetAfterExpense();

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void checkBudgetAfterExpense() {
        try {
            double remaining = userService.getRemainingBudget(currentUser.getId());
            double budget = currentUser.getMonthlyBudget();
            if (budget <= 0) return;

            double percentUsed = ((budget - remaining) / budget) * 100;

            if (remaining < 0) {
                System.out.println("[ALERT] You have EXCEEDED your monthly budget by Rs."
                        + String.format("%.2f", Math.abs(remaining)) + "!");
            } else if (percentUsed >= 80) {
                System.out.println("[WARNING] " + String.format("%.1f", percentUsed)
                        + "% of budget used. Only Rs." + String.format("%.2f", remaining) + " remaining.");
            }
        } catch (ExpenseTrackerException e) {
            // don't interrupt the user flow for a budget check failure
        }
    }

    private static void viewAllExpenses() {
        try {
            List<Expense> expenses = expenseService.getExpensesByUser(currentUser.getId());
            if (expenses.isEmpty()) {
                System.out.println("No expenses found.");
                return;
            }

            System.out.println("\n--- All Expenses ---");
            System.out.printf("%-5s %-20s %10s %12s%n", "ID", "Category", "Amount", "Date");
            System.out.println("-".repeat(50));
            for (Expense e : expenses) {
                System.out.printf("%-5d %-20s %10.2f %12s%n",
                        e.getId(), e.getCategory().getDisplayName(), e.getAmount(), e.getDate());
            }

            double total = expenseService.getTotalByUser(currentUser.getId());
            System.out.println("-".repeat(50));
            System.out.printf("%-25s %10.2f%n", "TOTAL", total);

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewByCategory(Scanner scanner) {
        try {
            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                System.out.println("  " + (i + 1) + ". " + cats[i].getDisplayName());
            }

            int catChoice = readInt(scanner, "Select category: ");
            if (catChoice < 1 || catChoice > cats.length) {
                System.out.println("Invalid category.");
                return;
            }

            Category category = cats[catChoice - 1];
            List<Expense> expenses = expenseService.getExpensesByCategory(currentUser.getId(), category);

            if (expenses.isEmpty()) {
                System.out.println("No expenses in " + category.getDisplayName() + ".");
                return;
            }

            System.out.println("\n--- " + category.getDisplayName() + " Expenses ---");
            double total = 0;
            for (Expense e : expenses) {
                System.out.printf("  [%d] Rs.%.2f on %s%n", e.getId(), e.getAmount(), e.getDate());
                total += e.getAmount();
            }
            System.out.printf("  Total: Rs.%.2f%n", total);

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void monthlyReport(Scanner scanner) {
        try {
            LocalDate now = LocalDate.now();
            System.out.println("(Press 0 for current year/month)");

            int year = readInt(scanner, "Year (" + now.getYear() + "): ");
            if (year == 0) year = now.getYear();

            int month = readInt(scanner, "Month (1-12): ");
            if (month == 0) month = now.getMonthValue();

            if (month < 1 || month > 12) {
                System.out.println("Invalid month.");
                return;
            }

            Map<Category, Double> report = expenseService.getMonthlyReport(currentUser.getId(), year, month);
            double monthlyTotal = expenseService.getMonthlyTotal(currentUser.getId(), year, month);

            System.out.printf("%n--- Monthly Report for %d-%02d ---%n", year, month);
            if (report.isEmpty()) {
                System.out.println("No expenses for this month.");
                return;
            }

            System.out.printf("%-25s %10s %8s%n", "Category", "Amount", "Share");
            System.out.println("-".repeat(45));
            for (Map.Entry<Category, Double> entry : report.entrySet()) {
                double percent = (entry.getValue() / monthlyTotal) * 100;
                System.out.printf("%-25s %10.2f %7.1f%%%n",
                        entry.getKey().getDisplayName(), entry.getValue(), percent);
            }
            System.out.println("-".repeat(45));
            System.out.printf("%-25s %10.2f%n", "TOTAL", monthlyTotal);

            double budget = currentUser.getMonthlyBudget();
            if (budget > 0) {
                double percentOfBudget = (monthlyTotal / budget) * 100;
                System.out.printf("%-25s %10.2f (%.1f%% of budget)%n", "BUDGET", budget, percentOfBudget);
            }

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void categoryWiseReport() {
        try {
            Map<Category, Double> totals = expenseService.getCategoryWiseTotals(currentUser.getId());
            if (totals.isEmpty()) {
                System.out.println("No expenses recorded yet.");
                return;
            }

            double grandTotal = 0;
            for (double v : totals.values()) {
                grandTotal += v;
            }

            System.out.println("\n--- Category-wise Report (All Time) ---");
            System.out.printf("%-25s %10s %8s%n", "Category", "Amount", "Share");
            System.out.println("-".repeat(45));
            for (Map.Entry<Category, Double> entry : totals.entrySet()) {
                double percent = (entry.getValue() / grandTotal) * 100;
                System.out.printf("%-25s %10.2f %7.1f%%%n",
                        entry.getKey().getDisplayName(), entry.getValue(), percent);
            }
            System.out.println("-".repeat(45));
            System.out.printf("%-25s %10.2f%n", "TOTAL", grandTotal);

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void budgetStatus() {
        try {
            double budget = currentUser.getMonthlyBudget();
            double spent = userService.getCurrentMonthSpending(currentUser.getId());
            double remaining = budget - spent;
            double percentUsed = budget > 0 ? ((spent / budget) * 100) : 0;

            LocalDate now = LocalDate.now();
            int dayOfMonth = now.getDayOfMonth();
            int daysInMonth = now.lengthOfMonth();

            System.out.printf("%n--- Budget Status (%d-%02d) ---%n", now.getYear(), now.getMonthValue());
            System.out.printf("  Monthly Budget   : Rs.%.2f%n", budget);
            System.out.printf("  Spent This Month : Rs.%.2f%n", spent);
            System.out.printf("  Remaining        : Rs.%.2f%n", remaining);
            System.out.printf("  Used             : %.1f%%%n", percentUsed);
            System.out.printf("  Day               : %d of %d%n", dayOfMonth, daysInMonth);

            if (budget > 0) {
                double dailyAvg = spent / dayOfMonth;
                double safeDailyLimit = remaining / Math.max(1, daysInMonth - dayOfMonth);
                System.out.printf("  Daily Average    : Rs.%.2f%n", dailyAvg);
                if (remaining > 0) {
                    System.out.printf("  Safe Daily Limit : Rs.%.2f%n", safeDailyLimit);
                }
            }

            System.out.println();
            if (remaining < 0) {
                System.out.println("  >> OVER BUDGET by Rs." + String.format("%.2f", Math.abs(remaining)));
            } else if (percentUsed >= 80) {
                System.out.println("  >> WARNING: Less than 20% of budget remaining!");
            } else {
                System.out.println("  >> On track.");
            }

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deleteExpense(Scanner scanner) {
        try {
            viewAllExpenses();
            int id = readInt(scanner, "Enter expense ID to delete (0 to cancel): ");
            if (id == 0) return;

            expenseService.deleteExpense(id);
            System.out.println("Expense #" + id + " deleted.");

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateBudget(Scanner scanner) {
        try {
            System.out.printf("Current budget: Rs.%.2f%n", currentUser.getMonthlyBudget());
            double newBudget = readDouble(scanner, "Enter new budget (Rs.): ");

            userService.updateBudget(currentUser.getId(), newBudget);
            currentUser.setMonthlyBudget(newBudget);
            System.out.println("Budget updated to Rs." + String.format("%.2f", newBudget));

        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void switchUser(Scanner scanner) {
        try {
            stopMonitor();
            currentUser = null;
            budgetMonitor.resetForNewUser();
            loginOrRegister(scanner);
        } catch (ExpenseTrackerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void startMonitor() {
        budgetMonitor.setUserId(currentUser.getId());
        monitorThread = new Thread(budgetMonitor, "BudgetMonitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private static void stopMonitor() {
        if (budgetMonitor != null) {
            budgetMonitor.stop();
        }
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            scanner.nextLine();
            System.out.print("Please enter a number: ");
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private static double readDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextDouble()) {
            scanner.nextLine();
            System.out.print("Please enter a valid amount: ");
        }
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }
}
