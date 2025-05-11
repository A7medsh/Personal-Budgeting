package com.personalbudgeting.service;

import com.personalbudgeting.dao.BudgetDAO;
import com.personalbudgeting.dao.ExpenseDAO;
import com.personalbudgeting.dao.IncomeDAO;
import com.personalbudgeting.model.Budget;
import com.personalbudgeting.model.User;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private BudgetDAO budgetDAO;
    private ExpenseDAO expenseDAO;
    private IncomeDAO incomeDAO;
    
    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.expenseDAO = new ExpenseDAO();
        this.incomeDAO = new IncomeDAO();
    }
    
    public boolean setBudget(String category, double amount, YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (category == null || category.trim().isEmpty() || amount <= 0) {
            return false;
        }
        
        // Check if budget already exists for this category and period
        Budget existingBudget = budgetDAO.findByCategory(currentUser.getId(), category, period);
        
        if (existingBudget != null) {
            // Update existing budget
            existingBudget.setAmount(amount);
            return budgetDAO.update(existingBudget);
        } else {
            // Create new budget
            Budget budget = new Budget(currentUser.getId(), category, amount, period);
            return budgetDAO.add(budget);
        }
    }
    
    public boolean deleteBudget(int budgetId) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        return budgetDAO.delete(budgetId, currentUser.getId());
    }
    
    public List<Budget> getBudgetsForPeriod(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return budgetDAO.findByPeriod(currentUser.getId(), period);
    }
    
    public Map<String, Double> getBudgetVsActualSpending(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        //We need to save the budget in my data base to control the data
        // Get budgets by category
        Map<String, Double> budgets = budgetDAO.getBudgetsByCategory(currentUser.getId(), period);
        
        // Get expenses by category
        Map<String, Double> expenses = expenseDAO.getExpensesByCategory(currentUser.getId(), period);
        
        // Combine into a result map showing budget vs. actual and percentage
        Map<String, Double> result = new HashMap<>();
        
        for (String category : budgets.keySet()) {
            double budgetAmount = budgets.get(category);
            double expenseAmount = expenses.getOrDefault(category, 0.0);
            double remainingAmount = budgetAmount - expenseAmount;
            
            result.put(category + "_budget", budgetAmount);
            result.put(category + "_spent", expenseAmount);
            result.put(category + "_remaining", remainingAmount);
            
            // Calculate percentage spent
            double percentSpent = (expenseAmount / budgetAmount) * 100;
            result.put(category + "_percent_spent", percentSpent);
        }
        
        // Add total income, total budget, and total expenses
        double totalIncome = incomeDAO.getTotalIncomeByPeriod(currentUser.getId(), period);
        double totalBudget = budgetDAO.getTotalBudget(currentUser.getId(), period);
        double totalExpenses = expenseDAO.getTotalExpenseByPeriod(currentUser.getId(), period);
        
        result.put("total_income", totalIncome);
        result.put("total_budget", totalBudget);
        result.put("total_expenses", totalExpenses);
        result.put("remaining_income", totalIncome - totalExpenses);
        
        return result;
    }
    
    public boolean isBudgetExceeded(String category, YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        Budget budget = budgetDAO.findByCategory(currentUser.getId(), category, period);
        if (budget == null) {
            return false; // No budget set, can't exceed
        }
        
        double expenses = 0.0;
        List<com.personalbudgeting.model.Expense> categoryExpenses = 
            expenseDAO.findByCategory(currentUser.getId(), category, period);
            
        for (com.personalbudgeting.model.Expense expense : categoryExpenses) {
            expenses += expense.getAmount();
        }
        
        return expenses > budget.getAmount();
    }
    
    public Map<String, Object> getBudgetSummary(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        Map<String, Object> summary = new HashMap<>();
        
        // Get total income
        double totalIncome = incomeDAO.getTotalIncomeByPeriod(currentUser.getId(), period);
        summary.put("totalIncome", totalIncome);
        
        // Get total expenses
        double totalExpenses = expenseDAO.getTotalExpenseByPeriod(currentUser.getId(), period);
        summary.put("totalExpenses", totalExpenses);
        
        // Get total budget
        double totalBudget = budgetDAO.getTotalBudget(currentUser.getId(), period);
        summary.put("totalBudget", totalBudget);
        
        // Calculate savings
        double savings = totalIncome - totalExpenses;
        summary.put("savings", savings);
        
        // Calculate budget utilization
        double budgetUtilization = totalBudget > 0 ? (totalExpenses / totalBudget) * 100 : 0;
        summary.put("budgetUtilization", budgetUtilization);
        
        // Get category breakdown
        Map<String, Double> expensesByCategory = expenseDAO.getExpensesByCategory(currentUser.getId(), period);
        summary.put("expensesByCategory", expensesByCategory);
        
        // Get budgets by category
        Map<String, Double> budgetsByCategory = budgetDAO.getBudgetsByCategory(currentUser.getId(), period);
        summary.put("budgetsByCategory", budgetsByCategory);
        
        return summary;
    }
} 
