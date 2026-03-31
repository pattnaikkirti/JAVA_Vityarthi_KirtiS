package com.expensetracker.model;

import java.time.LocalDate;
import java.util.Objects;

public class Expense {

    private int id;
    private int userId;
    private Category category;
    private double amount;
    private LocalDate date;

    public Expense(int userId, Category category, double amount, LocalDate date) {
        this.userId = userId;
        setCategory(category);
        setAmount(amount);
        setDate(date);
    }

    public Expense(int id, int userId, Category category, double amount, LocalDate date) {
        this.id = id;
        this.userId = userId;
        setCategory(category);
        setAmount(amount);
        setDate(date);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Category getCategory() { return category; }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Expense category cannot be null.");
        }
        this.category = category;
    }

    public double getAmount() { return amount; }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Expense amount must be positive.");
        }
        this.amount = amount;
    }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Expense date cannot be null.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Expense date cannot be in the future.");
        }
        this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Expense other = (Expense) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Expense{id=" + id + ", userId=" + userId + ", category=" + category.name()
                + ", amount=Rs." + String.format("%.2f", amount) + ", date=" + date + "}";
    }
}
