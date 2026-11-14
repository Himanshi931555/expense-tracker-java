package com.expensetracker.service;

import com.expensetracker.model.Transaction;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ExportService {

    public void toCsv(List<Transaction> list, Map<Integer, String> cats, File file) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("Date,Type,Category,Amount,Description");
            for (Transaction t : list) {
                out.println(t.getTxnDate() + "," + t.getType() + ","
                        + quote(cats.getOrDefault(t.getCategoryId(), "?")) + ","
                        + t.getAmount().toPlainString() + ","
                        + quote(t.getDescription()));
            }
        }
    }

    public void toPdf(String userName, List<Transaction> list, Map<Integer, String> cats,
                      BigDecimal income, BigDecimal expense, File file) throws Exception {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

        doc.add(new Paragraph("Expense Report", title));
        doc.add(new Paragraph("User: " + userName + "     Date: " + LocalDate.now(), normal));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Total Income:  Rs. " + String.format("%,.2f", income), bold));
        doc.add(new Paragraph("Total Expense: Rs. " + String.format("%,.2f", expense), bold));
        doc.add(new Paragraph("Balance:       Rs. " + String.format("%,.2f", income.subtract(expense)), bold));
        doc.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 2f, 3f, 2.5f, 4f});
        for (String h : new String[]{"Date", "Type", "Category", "Amount", "Description"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, bold));
            cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }
        for (Transaction t : list) {
            table.addCell(new Phrase(t.getTxnDate().toString(), normal));
            table.addCell(new Phrase(t.getType().name(), normal));
            table.addCell(new Phrase(cats.getOrDefault(t.getCategoryId(), "?"), normal));
            table.addCell(new Phrase(String.format("%,.2f", t.getAmount()), normal));
            table.addCell(new Phrase(t.getDescription() == null ? "" : t.getDescription(), normal));
        }
        doc.add(table);
        doc.close();
    }

    private String quote(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}