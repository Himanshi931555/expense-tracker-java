package com.expensetracker.service;

import com.expensetracker.dao.RecurringDAO;
import com.expensetracker.dao.TransactionDAO;
import com.expensetracker.model.RecurringTransaction;
import com.expensetracker.model.Transaction;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecurringService {

    private final RecurringDAO recurringDAO = new RecurringDAO();
    private final TransactionDAO txnDAO = new TransactionDAO();
    private ScheduledExecutorService scheduler;

    // Starts a background thread that runs every 60 seconds.
    // onAdded receives the number of transactions created (use it to refresh the UI).
    public void start(int userId, java.util.function.IntConsumer onAdded) {
        stop();
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "recurring-worker");
            t.setDaemon(true);   // thread will not block the app from closing
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int added = processDue(userId);
                if (added > 0) onAdded.accept(added);
            } catch (Exception e) {
                System.err.println("Recurring job failed: " + e.getMessage());
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
    }

    // Adds every due transaction (including missed ones) and moves next_date forward.
    // synchronized so two runs can never process the same item at the same time.
    public synchronized int processDue(int userId) throws Exception {
        int count = 0;
        LocalDate today = LocalDate.now();
        for (RecurringTransaction r : recurringDAO.findByUser(userId)) {
            LocalDate next = r.getNextDate();
            boolean changed = false;
            while (!next.isAfter(today)) {
                txnDAO.add(new Transaction(0, userId, r.getCategoryId(), r.getAmount(),
                        r.getType(), next,
                        (r.getDescription() == null ? "" : r.getDescription()) + " (auto)"));
                next = "WEEKLY".equals(r.getFrequency()) ? next.plusWeeks(1) : next.plusMonths(1);
                changed = true;
                count++;
            }
            if (changed) recurringDAO.updateNextDate(r.getRecId(), next);
        }
        return count;
    }
}