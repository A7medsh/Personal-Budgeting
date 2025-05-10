# Personal Budgeting Application

A JavaFX desktop application for personal finance management with features for tracking income, expenses, budgets, and reminders.

## Features

- **User Authentication**: Secure login and registration system
- **Income Tracking**: Record and categorize income sources
- **Expense Management**: Track and categorize expenses
- **Budget Planning**: Set and monitor budgets by category
- **Reminders**: Create and manage financial reminders with notifications
- **Dashboard**: Overview of financial status

## Technical Details

- **Language**: Java 11+
- **UI Framework**: JavaFX
- **Database**: SQLite (embedded)
- **Build Tool**: Maven

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- Maven (optional, wrapper included)

### Running the Application

On Windows:
```
run.bat
```

### Database Reset

If you need to reset the database:
```
reset_db.bat
```

## Project Structure

- `src/main/java/com/personalbudgeting/`
  - `controller/`: JavaFX controllers for UI screens
  - `dao/`: Data Access Objects for database operations
  - `model/`: Domain model classes
  - `service/`: Business logic services
  - `util/`: Utility classes
  - `Main.java`: Application entry point
- `src/main/resources/`
  - `fxml/`: JavaFX view files 