package com.expensetracker.view;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ChartBuilder {

    private static final String[] MONTHS =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color RED = new Color(220, 38, 38);
    private static final Color INDIGO = new Color(79, 70, 229);

    private ChartBuilder() { }

    public static ChartPanel pie(String title, Map<String, BigDecimal> data) {
        DefaultPieDataset ds = new DefaultPieDataset();
        if (data.isEmpty()) {
            ds.setValue("No data yet", 1);
        } else {
            data.forEach((k, v) -> ds.setValue(k, v));
        }
        JFreeChart chart = ChartFactory.createPieChart(title, ds, true, true, false);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        return wrap(chart);
    }

    public static ChartPanel monthlyBar(int year, BigDecimal[] income, BigDecimal[] expense) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (int i = 0; i < 12; i++) {
            ds.addValue(income[i], "Income", MONTHS[i]);
            ds.addValue(expense[i], "Expense", MONTHS[i]);
        }
        JFreeChart chart = ChartFactory.createBarChart("Income vs Expense (" + year + ")",
                "Month", "Amount", ds, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setBarPainter(new StandardBarPainter());
        r.setShadowVisible(false);
        r.setSeriesPaint(0, GREEN);
        r.setSeriesPaint(1, RED);
        return wrap(chart);
    }

    public static ChartPanel savingsLine(int year, BigDecimal[] income, BigDecimal[] expense) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        BigDecimal running = BigDecimal.ZERO;
        for (int i = 0; i < 12; i++) {
            running = running.add(income[i]).subtract(expense[i]);
            ds.addValue(running, "Cumulative savings", MONTHS[i]);
        }
        JFreeChart chart = ChartFactory.createLineChart("Savings Trend (" + year + ")",
                "Month", "Amount", ds, PlotOrientation.VERTICAL, true, true, false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        LineAndShapeRenderer r = (LineAndShapeRenderer) plot.getRenderer();
        r.setSeriesPaint(0, INDIGO);
        r.setSeriesStroke(0, new BasicStroke(3f));
        r.setSeriesShapesVisible(0, true);
        return wrap(chart);
    }

    private static ChartPanel wrap(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 15));
        return new ChartPanel(chart);
    }
}