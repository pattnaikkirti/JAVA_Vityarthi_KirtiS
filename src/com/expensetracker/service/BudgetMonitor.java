package com.expensetracker.service;

import com.expensetracker.exception.ExpenseTrackerException;

public class BudgetMonitor implements Runnable {

    private final UserService userService;
    private final ExpenseService expenseService;
    private int userId;
    private volatile boolean running = true;
    private final int checkIntervalSeconds;

    public BudgetMonitor(UserService userService, ExpenseService expenseService, int checkIntervalSeconds) {
        this.userService = userService;
        this.expenseService = expenseService;
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void stop() {
        this.running = false;
    }

    public void resetForNewUser() {
        this.running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(checkIntervalSeconds * 1000L);

                if (userId <= 0) continue;

                double remaining = userService.getRemainingBudget(userId);
                double budget = userService.getUserById(userId)
                        .map(u -> u.getMonthlyBudget())
                        .orElse(0.0);

                if (budget <= 0) continue;

                double percentUsed = ((budget - remaining) / budget) * 100;

                if (remaining < 0) {
                    System.out.println("\n[BUDGET ALERT] You have EXCEEDED your budget by Rs."
                            + String.format("%.2f", Math.abs(remaining)) + "!");
                    System.out.print("Enter choice: ");
                } else if (percentUsed >= 80) {
                    System.out.println("\n[BUDGET WARNING] " + String.format("%.1f", percentUsed)
                            + "% of budget used. Only Rs." + String.format("%.2f", remaining) + " left.");
                    System.out.print("Enter choice: ");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ExpenseTrackerException e) {
                // DB error during background check, skip this cycle
            }
        }
    }
}
