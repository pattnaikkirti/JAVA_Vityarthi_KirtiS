# 💸 Expense Tracker with Budget Alerts

# Kirti Sonam Pattnaik
# 24BAI10611

A Java CLI application for hostel students to track daily expenses, set monthly budgets, and get real-time budget alerts.

# 📋 Project Overview
The Expense Tracker with Budget Alerts is a lightweight, command-line interface (CLI) application designed to help hostel students actively manage their personal finances. It provides tools to track daily spending, establish monthly financial limits, and receive proactive warnings before overspending occurs.

## Features

- Register / login by name (persisted in SQLite)
- Add, view, and delete expenses with categories
- Filter expenses by category
- Monthly spending report with category-wise breakdown and percentage share
- All-time category-wise report
- Set and update monthly budget
- Budget status with daily average and safe daily spending limit
- Immediate budget alerts when adding expenses (warns at 80% usage)
- Background budget monitoring via a dedicated thread
- Data persists across sessions

## Tech Stack

- Java 17+
- SQLite via JDBC (`sqlite-jdbc-3.46.1.3.jar`)
- Manual compilation (no Maven/Gradle)

# 📁File Structure
```
JAVA_Vityarthi_KirtiS/
├── lib/
├── out/
│   └── com/
│       └── expensetracker/
│           ├── dao/
│           │   ├── ExpenseDAO$ParamSetter.class
│           │   ├── ExpenseDAO.class
│           │   └── UserDAO.class
│           ├── exception/
│           │   └── ExpenseTrackerException.class
│           ├── main/
│           │   └── Main.class
│           ├── model/
│           │   ├── Category.class
│           │   ├── Expense.class
│           │   └── User.class
│           ├── service/
│           │   ├── ExpenseService.class
│           │   └── UserService.class
│           └── util/
│               └── DBConnection.class
├── src/
│   └── com/
│       └── expensetracker/
│           ├── dao/
│           │   ├── ExpenseDAO.java
│           │   └── UserDAO.java
│           ├── exception/
│           │   └── ExpenseTrackerException.java
│           ├── main/
│           │   └── Main.java
│           ├── model/
│           │   ├── Category.java
│           │   ├── Expense.java
│           │   └── User.java
│           ├── service/
│           │   ├── ExpenseService.java
│           │   └── UserService.java
│           └── util/
│               └── DBConnection.java
├── LICENSE
├── README.md
└── run.bat
```

## Project Structure

```
src/com/expensetracker/
  model/       User, Expense, Category (enum)
  service/     ExpenseService, UserService, BudgetMonitor
  dao/         UserDAO, ExpenseDAO
  util/        DBConnection
  exception/   ExpenseTrackerException
  main/        Main
```

## How to Build & Run

### Windows

Double-click `run.bat`, or from the terminal:

```
javac -encoding UTF-8 -cp "lib\sqlite-jdbc-3.46.1.3.jar" -d out ^
  src/com/expensetracker/model/*.java ^
  src/com/expensetracker/exception/*.java ^
  src/com/expensetracker/util/*.java ^
  src/com/expensetracker/dao/*.java ^
  src/com/expensetracker/service/*.java ^
  src/com/expensetracker/main/*.java

java -cp "out;lib\sqlite-jdbc-3.46.1.3.jar" com.expensetracker.main.Main
```

### Linux / Mac

```
javac -encoding UTF-8 -cp "lib/sqlite-jdbc-3.46.1.3.jar" -d out \
  src/com/expensetracker/model/*.java \
  src/com/expensetracker/exception/*.java \
  src/com/expensetracker/util/*.java \
  src/com/expensetracker/dao/*.java \
  src/com/expensetracker/service/*.java \
  src/com/expensetracker/main/*.java

java -cp "out:lib/sqlite-jdbc-3.46.1.3.jar" com.expensetracker.main.Main
```

## Usage

```
==========================================
   Expense Tracker with Budget Alerts
==========================================

Enter your name: Rahul
New user! Set your monthly budget (Rs.): 5000
Registered! User{id=1, name='Rahul', budget=Rs.5000.00}

------ MENU [Rahul] ------
 1. Add Expense
 2. View All Expenses
 3. View by Category
 4. Monthly Report
 5. Category-wise Report
 6. Budget Status
 7. Delete Expense
 8. Update Budget
 9. Switch User
10. Exit
-------------------------------
Enter choice:
```

## Database

SQLite — stored in `expensetracker.db` (auto-created on first run).

| Table    | Columns                                |
|----------|----------------------------------------|
| users    | id, name, monthly_budget               |
| expenses | id, user_id, category, amount, date    |

## Architecture

```
Main (CLI + Scanner)
  │
  ▼
Service Layer (validation, business rules, BudgetMonitor thread)
  │
  ▼
DAO Layer (JDBC, PreparedStatement, SQL)
  │
  ▼
SQLite Database
```

Each layer has a single responsibility — Main handles user interaction, Services handle logic and validation, DAOs handle SQL queries. This separation means changing the database only requires modifying the DAO layer.

## Java Concepts Demonstrated

| Concept              | Where                                                                 |
|----------------------|-----------------------------------------------------------------------|
| OOP                  | Encapsulation, enums, constructor overloading, Singleton (DBConnection), equals/hashCode |
| Collections          | ArrayList, HashMap, Optional, Map.Entry iteration                     |
| Exception Handling   | Custom checked exception with cause chaining, try-with-resources      |
| JDBC                 | DriverManager, PreparedStatement, ResultSet, generated keys           |
| Concurrency          | Thread, Runnable, volatile, daemon threads, InterruptedException, synchronized |
| I/O                  | Scanner for input, formatted output with printf                       |
| Functional Interface | ParamSetter in ExpenseDAO, lambda expressions, method references      |


# Author 
Kirti Sonam Pattnaik
24BAI10611
