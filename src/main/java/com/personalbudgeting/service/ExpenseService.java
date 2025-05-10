package com.personalbudgeting.service;

import com.personalbudgeting.dao.ExpenseDAO;
import com.personalbudgeting.model.Expense;
import com.personalbudgeting.model.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class ExpenseService {
    private ExpenseDAO expenseDAO;
    private BudgetService budgetService;
    
    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
        this.budgetService = new BudgetService();
    }
    
    public boolean addExpense(String category, double amount, LocalDate date, String description) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (category == null || category.trim().isEmpty() || amount <= 0 || date == null) {
            return false;
        }
        
        Expense expense = new Expense(currentUser.getId(), category, amount, date, description);
        boolean success = expenseDAO.add(expense);
        
        if (success) {
            // Check if this expense exceeds the budget for the category
            YearMonth period = YearMonth.from(date);
            if (budgetService.isBudgetExceeded(category, period)) {
                // In a real app, we might send a notification here
                System.out.println("ALERT: Budget exceeded for category: " + category);
            }
        }
        
        return success;
    }
    
    public boolean updateExpense(int expenseId, String category, double amount, LocalDate date, String description) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (category == null || category.trim().isEmpty() || amount <= 0 || date == null) {
            return false;
        }
        
        // Get the expense to update
        Expense expense = expenseDAO.findById(expenseId, currentUser.getId());
        if (expense == null) {
            return false;
        }
        
        // Store old values for comparison
        String oldCategory = expense.getCategory();
        LocalDate oldDate = expense.getDate();
        
        // Update the expense
        expense.setCategory(category);
        expense.setAmount(amount);
        expense.setDate(date);
        expense.setDescription(description);
        
        boolean success = expenseDAO.update(expense);
        
        if (success) {
            // Check if this expense exceeds the budget for the new category
            YearMonth newPeriod = YearMonth.from(date);
            if (budgetService.isBudgetExceeded(category, newPeriod)) {
                // In a real app, we might send a notification here
                System.out.println("ALERT: Budget exceeded for category: " + category);
            }
            
            // If the category or date changed, we should also check the old category's budget
            if (!category.equals(oldCategory) || !date.getMonth().equals(oldDate.getMonth()) || date.getYear() != oldDate.getYear()) {
                YearMonth oldPeriod = YearMonth.from(oldDate);
                if (budgetService.isBudgetExceeded(oldCategory, oldPeriod)) {
                    // In a real app, we might send a notification here
                    System.out.println("ALERT: Budget exceeded for category: " + oldCategory);
                }
            }
        }
        
        return success;
    }
    
    public boolean deleteExpense(int expenseId) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Get the expense before deleting to check budgets after deletion
        Expense expense = expenseDAO.findById(expenseId, currentUser.getId());
        if (expense == null) {
            return false;
        }
        
        boolean success = expenseDAO.delete(expenseId, currentUser.getId());
        
        return success;
    }
    
    public List<Expense> getAllExpenses() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return expenseDAO.findAllByUserId(currentUser.getId());
    }
    
    public List<Expense> getExpensesForPeriod(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return expenseDAO.findByPeriod(currentUser.getId(), period);
    }
    
    public List<Expense> getExpensesByCategory(String category, YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return expenseDAO.findByCategory(currentUser.getId(), category, period);
    }
    
    public double getTotalExpensesForPeriod(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return 0.0;
        }
        
        return expenseDAO.getTotalExpenseByPeriod(currentUser.getId(), period);
    }
    
    public Map<String, Double> getExpensesByCategory(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return expenseDAO.getExpensesByCategory(currentUser.getId(), period);
    }
} 