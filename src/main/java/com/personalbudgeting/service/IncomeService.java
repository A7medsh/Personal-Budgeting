package com.personalbudgeting.service;

import com.personalbudgeting.dao.IncomeDAO;
import com.personalbudgeting.model.Income;
import com.personalbudgeting.model.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class IncomeService {
    private IncomeDAO incomeDAO;
    
    public IncomeService() {
        this.incomeDAO = new IncomeDAO();
    }
    
    public boolean addIncome(String source, double amount, LocalDate date, String description) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (source == null || source.trim().isEmpty() || amount <= 0 || date == null) {
            return false;
        }
        
        Income income = new Income(currentUser.getId(), source, amount, date, description);
        return incomeDAO.add(income);
    }
    
    public boolean updateIncome(int incomeId, String source, double amount, LocalDate date, String description) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Validate input
        if (source == null || source.trim().isEmpty() || amount <= 0 || date == null) {
            return false;
        }
        
        // Get the income to update
        Income income = incomeDAO.findById(incomeId, currentUser.getId());
        if (income == null) {
            return false;
        }
        
        // Update the income
        income.setSource(source);
        income.setAmount(amount);
        income.setDate(date);
        income.setDescription(description);
        
        return incomeDAO.update(income);
    }
    
    public boolean deleteIncome(int incomeId) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        return incomeDAO.delete(incomeId, currentUser.getId());
    }
    
    public List<Income> getAllIncome() {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return incomeDAO.findAllByUserId(currentUser.getId());
    }
    
    public List<Income> getIncomeForPeriod(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        
        return incomeDAO.findByPeriod(currentUser.getId(), period);
    }
    
    public double getTotalIncomeForPeriod(YearMonth period) {
        User currentUser = UserService.getCurrentUser();
        if (currentUser == null) {
            return 0.0;
        }
        
        return incomeDAO.getTotalIncomeByPeriod(currentUser.getId(), period);
    }
} 