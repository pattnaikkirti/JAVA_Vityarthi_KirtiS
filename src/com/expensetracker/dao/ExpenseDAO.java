package com.expensetracker.dao;

import com.expensetracker.exception.ExpenseTrackerException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public Expense insert(Expense expense) throws ExpenseTrackerException {
        String sql = "INSERT INTO expenses (user_id, category, amount, date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, expense.getUserId());
            pstmt.setString(2, expense.getCategory().name());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.setString(4, expense.getDate().toString());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                expense.setId(keys.getInt(1));
            }
            return expense;

        } catch (SQLException e) {
            throw new ExpenseTrackerException("Failed to insert expense: " + e.getMessage(), e);
        }
    }

    public void deleteById(int id) throws ExpenseTrackerException {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new ExpenseTrackerException("No expense found with id " + id);
            }
        } catch (SQLException e) {
            throw new ExpenseTrackerException("Failed to delete expense: " + e.getMessage(), e);
        }
    }

    public List<Expense> getByUserId(int userId) throws ExpenseTrackerException {
        String sql = "SELECT id, user_id, category, amount, date FROM expenses WHERE user_id = ?";
        return queryList(sql, pstmt -> pstmt.setInt(1, userId));
    }

    public List<Expense> getByUserAndCategory(int userId, Category category) throws ExpenseTrackerException {
        String sql = "SELECT id, user_id, category, amount, date FROM expenses "
                + "WHERE user_id = ? AND category = ?";
        return queryList(sql, pstmt -> {
            pstmt.setInt(1, userId);
            pstmt.setString(2, category.name());
        });
    }

    public List<Expense> getByUserAndMonth(int userId, int year, int month) throws ExpenseTrackerException {
        String sql = "SELECT id, user_id, category, amount, date FROM expenses "
                + "WHERE user_id = ? AND date BETWEEN ? AND ?";

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return queryList(sql, pstmt -> {
            pstmt.setInt(1, userId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
        });
    }

    public double getTotalByUser(int userId) throws ExpenseTrackerException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses WHERE user_id = ?";
        return queryTotal(sql, pstmt -> pstmt.setInt(1, userId));
    }

    public double getMonthlyTotal(int userId, int year, int month) throws ExpenseTrackerException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses "
                + "WHERE user_id = ? AND date BETWEEN ? AND ?";

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return queryTotal(sql, pstmt -> {
            pstmt.setInt(1, userId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
        });
    }

    private List<Expense> queryList(String sql, ParamSetter setter) throws ExpenseTrackerException {
        List<Expense> expenses = new ArrayList<>();
        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            setter.setParams(pstmt);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                expenses.add(mapRow(rs));
            }
            return expenses;
        } catch (SQLException e) {
            throw new ExpenseTrackerException("Query failed: " + e.getMessage(), e);
        }
    }

    private double queryTotal(String sql, ParamSetter setter) throws ExpenseTrackerException {
        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            setter.setParams(pstmt);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0.0;
        } catch (SQLException e) {
            throw new ExpenseTrackerException("Query failed: " + e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface ParamSetter {
        void setParams(PreparedStatement pstmt) throws SQLException;
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt("id"),
                rs.getInt("user_id"),
                Category.valueOf(rs.getString("category")),
                rs.getDouble("amount"),
                LocalDate.parse(rs.getString("date"))
        );
    }
}
