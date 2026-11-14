package com.expensetracker.view;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.RecurringDAO;
import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.model.Category;
import com.expensetracker.model.RecurringTransaction;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecurringPanel extends JPanel {

    private final User user;
    private final RecurringDAO recurringDAO = new RecurringDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Type", "Category", "Amount", "Description", "Frequency", "Next Date"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<RecurringTransaction> items;

    public RecurringPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel info = new JLabel("A background thread adds these automatically when they fall due.");
        info.setForeground(Color.GRAY);

        table.setRowHeight(30);
        JButton addBtn = new JButton("Add Recurring");
        JButton deleteBtn = new JButton("Delete");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(addBtn);
        buttons.add(deleteBtn);
        addBtn.addActionListener(e -> showAddDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        add(info, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        try {
            Map<Integer, String> names = new HashMap<>();
            for (Category c : categoryDAO.findByUser(user.getUserId())) names.put(c.getCategoryId(), c.getName());
            items = recurringDAO.findByUser(user.getUserId());
            model.setRowCount(0);
            for (RecurringTransaction r : items) {
                model.addRow(new Object[]{r.getType(), names.getOrDefault(r.getCategoryId(), "?"),
                        String.format("%,.2f", r.getAmount()),
                        r.getDescription() == null ? "" : r.getDescription(),
                        r.getFrequency(), r.getNextDate()});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showAddDialog() {
        try {
            List<Category> allCats = categoryDAO.findByUser(user.getUserId());
            JComboBox<TransactionType> typeBox = new JComboBox<>(TransactionType.values());
            JComboBox<Category> catBox = new JComboBox<>();
            JTextField amountField = new JTextField(10);
            JTextField descField = new JTextField(15);
            JComboBox<String> freqBox = new JComboBox<>(new String[]{"MONTHLY", "WEEKLY"});
            JTextField dateField = new JTextField(LocalDate.now().toString(), 10);

            Runnable fill = () -> {
                catBox.removeAllItems();
                TransactionType sel = (TransactionType) typeBox.getSelectedItem();
                allCats.stream().filter(c -> c.getType() == sel).forEach(catBox::addItem);
            };
            typeBox.addActionListener(e -> fill.run());
            fill.run();

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
            form.add(new JLabel("Type")); form.add(typeBox);
            form.add(new JLabel("Category")); form.add(catBox);
            form.add(new JLabel("Amount")); form.add(amountField);
            form.add(new JLabel("Description")); form.add(descField);
            form.add(new JLabel("Frequency")); form.add(freqBox);
            form.add(new JLabel("First date (YYYY-MM-DD)")); form.add(dateField);

            int result = JOptionPane.showConfirmDialog(this, form, "Add Recurring Transaction",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            Category cat = (Category) catBox.getSelectedItem();
            if (cat == null) return;
            BigDecimal amount;
            LocalDate date;
            try {
                amount = new BigDecimal(amountField.getText().trim());
                date = LocalDate.parse(dateField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid amount and date (YYYY-MM-DD).",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            recurringDAO.add(new RecurringTransaction(0, user.getUserId(), cat.getCategoryId(), amount,
                    (TransactionType) typeBox.getSelectedItem(), descField.getText().trim(),
                    (String) freqBox.getSelectedItem(), date));
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
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete this recurring item?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            recurringDAO.delete(items.get(row).getRecId(), user.getUserId());
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