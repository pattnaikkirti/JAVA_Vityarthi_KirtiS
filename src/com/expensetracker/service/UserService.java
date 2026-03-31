package com.expensetracker.service;

import com.expensetracker.dao.UserDAO;
import com.expensetracker.exception.ExpenseTrackerException;
import com.expensetracker.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;
    private final ExpenseService expenseService;

    public UserService(UserDAO userDAO, ExpenseService expenseService) {
        this.userDAO = userDAO;
        this.expenseService = expenseService;
    }

    public User registerUser(String name, double monthlyBudget) throws ExpenseTrackerException {
        if (name == null || name.trim().isEmpty()) {
            throw new ExpenseTrackerException("User name cannot be null or empty.");
        }
        if (monthlyBudget < 0) {
            throw new ExpenseTrackerException("Monthly budget cannot be negative.");
        }

        Optional<User> existing = userDAO.getByName(name.trim());
        if (existing.isPresent()) {
            throw new ExpenseTrackerException("User '" + name.trim() + "' already exists.");
        }

        User user = new User(name.trim(), monthlyBudget);
        return userDAO.insert(user);
    }

    public Optional<User> getUserById(int id) throws ExpenseTrackerException {
        return userDAO.getById(id);
    }

    public Optional<User> getUserByName(String name) throws ExpenseTrackerException {
        return userDAO.getByName(name);
    }

    public void updateBudget(int userId, double newBudget) throws ExpenseTrackerException {
        if (newBudget < 0) {
            throw new ExpenseTrackerException("Budget cannot be negative.");
        }
        userDAO.updateBudget(userId, newBudget);
    }

    public double getRemainingBudget(int userId) throws ExpenseTrackerException {
        User user = userDAO.getById(userId)
                .orElseThrow(() -> new ExpenseTrackerException("User with ID " + userId + " not found."));

        LocalDate now = LocalDate.now();
        double monthlySpent = expenseService.getMonthlyTotal(userId, now.getYear(), now.getMonthValue());
        return user.getMonthlyBudget() - monthlySpent;
    }

    public double getCurrentMonthSpending(int userId) throws ExpenseTrackerException {
        LocalDate now = LocalDate.now();
        return expenseService.getMonthlyTotal(userId, now.getYear(), now.getMonthValue());
    }

    public List<User> getAllUsers() throws ExpenseTrackerException {
        return userDAO.getAll();
    }
}
