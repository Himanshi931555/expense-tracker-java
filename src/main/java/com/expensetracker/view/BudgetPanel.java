package com.expensetracker.view;

import com.expensetracker.dao.BudgetDAO;
import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.service.BudgetService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class BudgetPanel extends JPanel {

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color ORANGE = new Color(234, 88, 12);
    private static final Color RED = new Color(220, 38, 38);

    private final User user;
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final BudgetService budgetService = new BudgetService();

    private final JComboBox<String> monthBox = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Category", "Limit", "Spent", "Remaining", "Used"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel summary = new JLabel(" ");
    private List<BudgetService.Status> statuses = new ArrayList<>();
    private boolean loading = false;

    public BudgetPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // month selector (next month back to 11 months ago)
        YearMonth now = YearMonth.now();
        loading = true;
        for (int i = 1; i >= -11; i--) monthBox.addItem(now.plusMonths(i).toString());
        monthBox.setSelectedItem(now.toString());
        loading = false;
        monthBox.addActionListener(e -> refresh());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Month"));
        top.add(monthBox);
        top.add(summary);

        table.setRowHeight(32);
        table.getColumnModel().getColumn(4).setCellRenderer((t, v, sel, foc, r, c) -> {
            int pct = v == null ? 0 : (Integer) v;
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(Math.min(pct, 100));
            bar.setStringPainted(true);
            bar.setString(pct + "%");
            bar.setForeground(pct > 100 ? RED : (pct >= 80 ? ORANGE : GREEN));
            return bar;
        });

        JButton setBtn = new JButton("Set / Update Budget");
        JButton deleteBtn = new JButton("Delete Budget");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(setBtn);
        buttons.add(deleteBtn);
        setBtn.addActionListener(e -> showSetDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private YearMonth selectedMonth() {
        String s = (String) monthBox.getSelectedItem();
        return s == null ? YearMonth.now() : YearMonth.parse(s);
    }

    public void refresh() {
        if (loading) return;
        try {
            statuses = budgetService.statusesFor(user.getUserId(), selectedMonth());
            model.setRowCount(0);
            int over = 0;
            for (BudgetService.Status s : statuses) {
                model.addRow(new Object[]{
                        s.categoryName(),
                        String.format("%,.2f", s.limit()),
                        String.format("%,.2f", s.spent()),
                        String.format("%,.2f", s.remaining()),
                        s.percentUsed()});
                if (s.spent().compareTo(s.limit()) > 0) over++;
            }
            summary.setText(statuses.isEmpty()
                    ? "No budgets set for this month."
                    : statuses.size() + " budget(s), " + over + " over limit");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showSetDialog() {
        try {
            JComboBox<Category> catBox = new JComboBox<>();
            categoryDAO.findByUser(user.getUserId()).stream()
                    .filter(c -> c.getType() == TransactionType.EXPENSE)
                    .forEach(catBox::addItem);
            JTextField limitField = new JTextField(10);

            // prefill if a row is selected
            int row = table.getSelectedRow();
            if (row >= 0) {
                BudgetService.Status s = statuses.get(row);
                for (int i = 0; i < catBox.getItemCount(); i++) {
                    if (catBox.getItemAt(i).getCategoryId() == s.categoryId()) catBox.setSelectedIndex(i);
                }
                limitField.setText(s.limit().toPlainString());
            }

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
            form.add(new JLabel("Month")); form.add(new JLabel(selectedMonth().toString()));
            form.add(new JLabel("Category")); form.add(catBox);
            form.add(new JLabel("Monthly limit")); form.add(limitField);

            int result = JOptionPane.showConfirmDialog(this, form, "Set Budget",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            Category cat = (Category) catBox.getSelectedItem();
            if (cat == null) return;
            BigDecimal limit;
            try {
                limit = new BigDecimal(limitField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid number for the limit.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            budgetDAO.save(new Budget(0, user.getUserId(), cat.getCategoryId(),
                    selectedMonth().toString(), limit));
            refresh();
        } catch (InvalidAmountException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a budget row first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete this budget?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            budgetDAO.delete(statuses.get(row).budgetId(), user.getUserId());
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}