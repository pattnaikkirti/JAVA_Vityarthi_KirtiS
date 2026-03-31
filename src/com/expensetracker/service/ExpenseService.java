package com.expensetracker.service;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.exception.ExpenseTrackerException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseService {

    private final ExpenseDAO expenseDAO;

    public ExpenseService(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    public void addExpense(Expense expense) throws ExpenseTrackerException {
        validateExpense(expense);
        expenseDAO.insert(expense);
    }

    public void deleteExpense(int expenseId) throws ExpenseTrackerException {
        expenseDAO.deleteById(expenseId);
    }

    public List<Expense> getExpensesByUser(int userId) throws ExpenseTrackerException {
        return expenseDAO.getByUserId(userId);
    }

    public List<Expense> getExpensesByCategory(int userId, Category category) throws ExpenseTrackerException {
        return expenseDAO.getByUserAndCategory(userId, category);
    }

    public List<Expense> getMonthlyExpenses(int userId, int year, int month) throws ExpenseTrackerException {
        return expenseDAO.getByUserAndMonth(userId, year, month);
    }

    public double getTotalByUser(int userId) throws ExpenseTrackerException {
        return expenseDAO.getTotalByUser(userId);
    }

    public double getMonthlyTotal(int userId, int year, int month) throws ExpenseTrackerException {
        return expenseDAO.getMonthlyTotal(userId, year, month);
    }

    public Map<Category, Double> getCategoryWiseTotals(int userId) throws ExpenseTrackerException {
        return buildCategoryMap(getExpensesByUser(userId));
    }

    public Map<Category, Double> getMonthlyReport(int userId, int year, int month) throws ExpenseTrackerException {
        return buildCategoryMap(getMonthlyExpenses(userId, year, month));
    }

    private Map<Category, Double> buildCategoryMap(List<Expense> expenses) {
        Map<Category, Double> totals = new HashMap<>();
        for (Expense expense : expenses) {
            totals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }
        return totals;
    }

    private void validateExpense(Expense expense) throws ExpenseTrackerException {
        if (expense == null) {
            throw new ExpenseTrackerException("Expense cannot be null.");
        }
        if (expense.getAmount() <= 0) {
            throw new ExpenseTrackerException("Amount must be greater than zero.");
        }
        if (expense.getCategory() == null) {
            throw new ExpenseTrackerException("Category is required.");
        }
        if (expense.getDate() == null) {
            throw new ExpenseTrackerException("Date is required.");
        }
        if (expense.getUserId() <= 0) {
            throw new ExpenseTrackerException("Must be linked to a valid user.");
        }
    }
}
