package com.expensetracker.service;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ReportService {

    public BigDecimal totalIncome(List<Transaction> list) {
        return sum(list, TransactionType.INCOME);
    }

    public BigDecimal totalExpense(List<Transaction> list) {
        return sum(list, TransactionType.EXPENSE);
    }

    public BigDecimal balance(List<Transaction> list) {
        return totalIncome(list).subtract(totalExpense(list));
    }

    private BigDecimal sum(List<Transaction> list, TransactionType type) {
        return list.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Category name -> total (used for pie charts)
    public Map<String, BigDecimal> totalsByCategory(List<Transaction> list, TransactionType type,
                                                    Map<Integer, String> names) {
        return list.stream()
                .filter(t -> t.getType() == type)
                .collect(Collectors.groupingBy(
                        t -> names.getOrDefault(t.getCategoryId(), "Other"),
                        TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
    }

    // 12 values (Jan..Dec) for the given year and type
    public BigDecimal[] monthlyTotals(List<Transaction> list, int year, TransactionType type) {
        BigDecimal[] result = new BigDecimal[12];
        Arrays.fill(result, BigDecimal.ZERO);
        for (Transaction t : list) {
            if (t.getType() == type && t.getTxnDate().getYear() == year) {
                int m = t.getTxnDate().getMonthValue() - 1;
                result[m] = result[m].add(t.getAmount());
            }
        }
        return result;
    }
}