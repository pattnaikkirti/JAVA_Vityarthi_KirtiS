package com.expensetracker.dao;

import com.expensetracker.exception.ExpenseTrackerException;
import com.expensetracker.model.User;
import com.expensetracker.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    public User insert(User user) throws ExpenseTrackerException {
        String sql = "INSERT INTO users (name, monthly_budget) VALUES (?, ?)";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            pstmt.setDouble(2, user.getMonthlyBudget());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }
            return user;

        } catch (SQLException e) {
            throw new ExpenseTrackerException("Failed to insert user: " + e.getMessage(), e);
        }
    }

    public Optional<User> getById(int id) throws ExpenseTrackerException {
        String sql = "SELECT id, name, monthly_budget FROM users WHERE id = ?";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new ExpenseTrackerException("Failed to fetch user: " + e.getMessage(), e);
        }
    }

    public Optional<User> getByName(String name) throws ExpenseTrackerException {
        String sql = "SELECT id, name, monthly_budget FROM users WHERE LOWER(name) = LOWER(?)";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new ExpenseTrackerException("Failed to fetch user by name: " + e.getMessage(), e);
        }
    }

    public List<User> getAll() throws ExpenseTrackerException {
        String sql = "SELECT id, name, monthly_budget FROM users";
        List<User> users = new ArrayList<>();

        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;

        } catch (SQLException e) {
            throw new ExpenseTrackerException("Failed to fetch users: " + e.getMessage(), e);
        }
    }

    public void updateBudget(int userId, double newBudget) throws ExpenseTrackerException {
        String sql = "UPDATE users SET monthly_budget = ? WHERE id = ?";

        try (PreparedStatement pstmt = DBConnection.getConnection().prepareStatement(sql)) {
            pstmt.setDouble(1, newBudget);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new ExpenseTrackerException("No user found with id " + userId);
            }

        } catch (SQLException e) {
            throw new ExpenseTrackerException("Failed to update budget: " + e.getMessage(), e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("monthly_budget")
        );
    }
}
