package com.expensetracker.view;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.dao.TransactionDAO;
import com.expensetracker.exception.InvalidAmountException;
import com.expensetracker.model.Category;
import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionType;
import com.expensetracker.model.User;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.ExportService;
import com.expensetracker.service.RecurringService;
import com.expensetracker.service.ReportService;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardFrame extends JFrame {

    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color RED = new Color(220, 38, 38);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color ORANGE = new Color(234, 88, 12);
    private static final String RS = "\u20B9 ";
    private static final String[] MONTH_NAMES =
            {"January", "February", "March", "April", "May", "June",
             "July", "August", "September", "October", "November", "December"};

    private final User user;
    private final TransactionDAO txnDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ReportService reportService = new ReportService();
    private final ExportService exportService = new ExportService();
    private final RecurringService recurringService = new RecurringService();

    private List<Transaction> allTxns = new ArrayList<>();
    private List<Transaction> visibleTxns = new ArrayList<>();
    private Map<Integer, String> catNames = new HashMap<>();
    private boolean loading = false;

    private final JLabel incomeValue = new JLabel("0");
    private final JLabel expenseValue = new JLabel("0");
    private final JLabel balanceValue = new JLabel("0");
    private JPanel balanceCard;
    private BudgetPanel budgetPanel;
    private RecurringPanel recurringPanel;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Date", "Type", "Category", "Amount", "Description"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JComboBox<String> typeFilter = new JComboBox<>(new String[]{"All", "INCOME", "EXPENSE"});
    private final JComboBox<String> monthFilter = new JComboBox<>();
    private final JTextField searchField = new JTextField(14);

    private final JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 12, 12));
    private final DefaultTableModel reportModel = new DefaultTableModel(
            new String[]{"Month", "Income", "Expense", "Savings"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("Expense Tracker - " + user.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        root.add(buildNorth(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        setContentPane(root);

        refresh();

        recurringService.start(user.getUserId(),
                added -> SwingUtilities.invokeLater(this::refresh));

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                recurringService.stop();
            }
        });
    }

    // ---------- UI building ----------

    private JPanel buildNorth() {
        JPanel north = new JPanel(new BorderLayout(0, 12));

        JPanel header = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, " + user.getName());
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 22f));
        JLabel sub = new JLabel("Track your income and expenses");
        sub.setForeground(Color.GRAY);
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.add(welcome);
        left.add(sub);
        header.add(left, BorderLayout.WEST);

        JButton themeBtn = new JButton("Dark / Light");
        JButton logoutBtn = new JButton("Logout");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(themeBtn);
        right.add(logoutBtn);
        header.add(right, BorderLayout.EAST);

        themeBtn.addActionListener(e -> {
            if (FlatLaf.isLafDark()) FlatLightLaf.setup(); else FlatDarkLaf.setup();
            FlatLaf.updateUI();
        });
        logoutBtn.addActionListener(e -> {
            recurringService.stop();
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel cards = new JPanel(new GridLayout(1, 3, 14, 0));
        cards.add(card("Total Income", incomeValue, GREEN));
        cards.add(card("Total Expense", expenseValue, RED));
        balanceCard = card("Balance", balanceValue, BLUE);
        cards.add(balanceCard);

        north.add(header, BorderLayout.NORTH);
        north.add(cards, BorderLayout.CENTER);
        return north;
    }

    private JPanel card(String title, JLabel value, Color color) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 4));
        p.setBackground(color);
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel t = new JLabel(title);
        t.setForeground(new Color(255, 255, 255, 220));
        t.setFont(t.getFont().deriveFont(Font.PLAIN, 13f));
        value.setForeground(Color.WHITE);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 26f));
        p.add(t);
        p.add(value);
        return p;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Transactions", buildTransactionsTab());

        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        tabs.addTab("Charts", chartsPanel);

        JTable reportTable = new JTable(reportModel);
        reportTable.setRowHeight(28);
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        reportPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        tabs.addTab("Monthly Report", reportPanel);

        budgetPanel = new BudgetPanel(user);
        tabs.addTab("Budgets", budgetPanel);

        recurringPanel = new RecurringPanel(user);
        tabs.addTab("Recurring", recurringPanel);
        return tabs;
    }

    private JPanel buildTransactionsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // filters
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.add(new JLabel("Type"));
        filters.add(typeFilter);
        filters.add(new JLabel("Month"));
        filters.add(monthFilter);
        filters.add(new JLabel("Search"));
        filters.add(searchField);
        typeFilter.addActionListener(e -> applyFilters());
        monthFilter.addActionListener(e -> applyFilters());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // table
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(RIGHT); }
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    Object type = t.getValueAt(r, 2);
                    comp.setForeground("INCOME".equals(type) ? GREEN : RED);
                }
                return comp;
            }
        });

        // buttons
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton csvBtn = new JButton("Export CSV");
        JButton pdfBtn = new JButton("Export PDF");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(addBtn);
        buttons.add(editBtn);
        buttons.add(deleteBtn);
        buttons.add(csvBtn);
        buttons.add(pdfBtn);

        addBtn.addActionListener(e -> showTransactionDialog(null));
        editBtn.addActionListener(e -> {
            Transaction t = selectedTransaction();
            if (t != null) showTransactionDialog(t);
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        csvBtn.addActionListener(e -> exportCsv());
        pdfBtn.addActionListener(e -> exportPdf());

        panel.add(filters, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- Data ----------

    private void refresh() {
        try {
            allTxns = txnDAO.findByUser(user.getUserId());
            catNames = categoryDAO.findByUser(user.getUserId()).stream()
                    .collect(Collectors.toMap(Category::getCategoryId, Category::getName));
            updateCards();
            rebuildMonthFilter();
            applyFilters();
            buildCharts();
            buildReport();
            budgetPanel.refresh();
            recurringPanel.refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateCards() {
        BigDecimal bal = reportService.balance(allTxns);
        incomeValue.setText(RS + String.format("%,.2f", reportService.totalIncome(allTxns)));
        expenseValue.setText(RS + String.format("%,.2f", reportService.totalExpense(allTxns)));
        balanceValue.setText(RS + String.format("%,.2f", bal));
        balanceCard.setBackground(bal.signum() < 0 ? ORANGE : BLUE);
    }

    private void rebuildMonthFilter() {
        String selected = (String) monthFilter.getSelectedItem();
        loading = true;
        monthFilter.removeAllItems();
        monthFilter.addItem("All");
        allTxns.stream()
                .map(t -> YearMonth.from(t.getTxnDate()).toString())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .forEach(monthFilter::addItem);
        if (selected != null) monthFilter.setSelectedItem(selected);
        loading = false;
    }

    private void applyFilters() {
        if (loading) return;
        String type = (String) typeFilter.getSelectedItem();
        String month = (String) monthFilter.getSelectedItem();
        String q = searchField.getText().trim().toLowerCase();

        visibleTxns = allTxns.stream()
                .filter(t -> "All".equals(type) || t.getType().name().equals(type))
                .filter(t -> month == null || "All".equals(month)
                        || YearMonth.from(t.getTxnDate()).toString().equals(month))
                .filter(t -> q.isEmpty()
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q))
                        || catNames.getOrDefault(t.getCategoryId(), "").toLowerCase().contains(q))
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        for (Transaction t : visibleTxns) {
            tableModel.addRow(new Object[]{
                    t.getTxnId(), t.getTxnDate().toString(), t.getType().name(),
                    catNames.getOrDefault(t.getCategoryId(), "?"),
                    String.format("%,.2f", t.getAmount()),
                    t.getDescription() == null ? "" : t.getDescription()});
        }
    }

    private void buildCharts() {
        int year = LocalDate.now().getYear();
        BigDecimal[] income = reportService.monthlyTotals(allTxns, year, TransactionType.INCOME);
        BigDecimal[] expense = reportService.monthlyTotals(allTxns, year, TransactionType.EXPENSE);

        chartsPanel.removeAll();
        chartsPanel.add(ChartBuilder.pie("Expenses by Category",
                reportService.totalsByCategory(allTxns, TransactionType.EXPENSE, catNames)));
        chartsPanel.add(ChartBuilder.pie("Income by Category",
                reportService.totalsByCategory(allTxns, TransactionType.INCOME, catNames)));
        chartsPanel.add(ChartBuilder.monthlyBar(year, income, expense));
        chartsPanel.add(ChartBuilder.savingsLine(year, income, expense));
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    private void buildReport() {
        int year = LocalDate.now().getYear();
        BigDecimal[] income = reportService.monthlyTotals(allTxns, year, TransactionType.INCOME);
        BigDecimal[] expense = reportService.monthlyTotals(allTxns, year, TransactionType.EXPENSE);
        BigDecimal ti = BigDecimal.ZERO, te = BigDecimal.ZERO;

        reportModel.setRowCount(0);
        for (int i = 0; i < 12; i++) {
            reportModel.addRow(new Object[]{MONTH_NAMES[i] + " " + year,
                    String.format("%,.2f", income[i]), String.format("%,.2f", expense[i]),
                    String.format("%,.2f", income[i].subtract(expense[i]))});
            ti = ti.add(income[i]);
            te = te.add(expense[i]);
        }
        reportModel.addRow(new Object[]{"TOTAL " + year, String.format("%,.2f", ti),
                String.format("%,.2f", te), String.format("%,.2f", ti.subtract(te))});
    }

    // ---------- Actions ----------

    private Transaction selectedTransaction() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return null;
        }
        int id = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
        return allTxns.stream().filter(t -> t.getTxnId() == id).findFirst().orElse(null);
    }

    private void deleteSelected() {
        Transaction t = selectedTransaction();
        if (t == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Delete this transaction?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            txnDAO.delete(t.getTxnId(), user.getUserId());
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showTransactionDialog(Transaction existing) {
        try {
            List<Category> allCats = categoryDAO.findByUser(user.getUserId());

            JComboBox<TransactionType> typeBox = new JComboBox<>(TransactionType.values());
            JComboBox<Category> catBox = new JComboBox<>();
            JTextField amountField = new JTextField(10);
            JTextField dateField = new JTextField(LocalDate.now().toString(), 10);
            JTextField descField = new JTextField(15);

            Runnable fillCategories = () -> {
                catBox.removeAllItems();
                TransactionType sel = (TransactionType) typeBox.getSelectedItem();
                allCats.stream().filter(c -> c.getType() == sel).forEach(catBox::addItem);
            };
            typeBox.addActionListener(e -> fillCategories.run());
            fillCategories.run();

            if (existing != null) {
                typeBox.setSelectedItem(existing.getType());
                for (int i = 0; i < catBox.getItemCount(); i++) {
                    if (catBox.getItemAt(i).getCategoryId() == existing.getCategoryId()) {
                        catBox.setSelectedIndex(i);
                    }
                }
                amountField.setText(existing.getAmount().toPlainString());
                dateField.setText(existing.getTxnDate().toString());
                descField.setText(existing.getDescription() == null ? "" : existing.getDescription());
            }

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
            form.add(new JLabel("Type")); form.add(typeBox);
            form.add(new JLabel("Category")); form.add(catBox);
            form.add(new JLabel("Amount")); form.add(amountField);
            form.add(new JLabel("Date (YYYY-MM-DD)")); form.add(dateField);
            form.add(new JLabel("Description")); form.add(descField);

            String heading = existing == null ? "Add Transaction" : "Edit Transaction";
            int result = JOptionPane.showConfirmDialog(this, form, heading,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return;

            Category cat = (Category) catBox.getSelectedItem();
            if (cat == null) {
                JOptionPane.showMessageDialog(this, "Please select a category.");
                return;
            }
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

            TransactionType type = (TransactionType) typeBox.getSelectedItem();
            int id = existing == null ? 0 : existing.getTxnId();
            Transaction t = new Transaction(id, user.getUserId(), cat.getCategoryId(),
                    amount, type, date, descField.getText().trim());
            if (existing == null) txnDAO.add(t); else txnDAO.update(t);
            refresh();
            if (type == TransactionType.EXPENSE) {
                String warning = new BudgetService().checkWarning(
                        user.getUserId(), cat.getCategoryId(), cat.getName(), date);
                if (warning != null) {
                    JOptionPane.showMessageDialog(this, warning, "Budget Alert",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (InvalidAmountException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("transactions.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            exportService.toCsv(visibleTxns, catNames, fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "CSV saved:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void exportPdf() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("expense-report.pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            exportService.toPdf(user.getName(), visibleTxns, catNames,
                    reportService.totalIncome(visibleTxns), reportService.totalExpense(visibleTxns),
                    fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, "PDF saved:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}