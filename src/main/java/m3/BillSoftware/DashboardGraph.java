package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class DashboardGraph extends JPanel {  // Changed from JFrame to JPanel
    private JComboBox<String> chartTypeComboBox;
    private JComboBox<String> timePeriodComboBox;
    private JButton btnRefresh;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private JPanel chartContainer;

    public DashboardGraph() {
        setLayout(new BorderLayout());
        setBackground(new Color(241, 242, 246));

        initializeDatabaseConnection();
        createUI();
        loadData();
    }

    private void initializeDatabaseConnection() {
        try {
            mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/");
            database = mongoClient.getDatabase("testDB");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database connection failed: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void createUI() {
        // Filter panel at the top
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Chart type selection
        String[] chartTypes = {"Pie Chart", "Bar Chart", "Line Chart"};
        chartTypeComboBox = new JComboBox<>(chartTypes);
        chartTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chartTypeComboBox.addActionListener(e -> loadData());
        filterPanel.add(new JLabel("Chart Type:"));
        filterPanel.add(chartTypeComboBox);

        // Time period selection
        String[] timePeriods = {"All Time", "Last Month", "Last 3 Months", "Last 6 Months", "Last Year"};
        timePeriodComboBox = new JComboBox<>(timePeriods);
        timePeriodComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timePeriodComboBox.addActionListener(e -> loadData());
        filterPanel.add(new JLabel("Time Period:"));
        filterPanel.add(timePeriodComboBox);

        // Refresh button
        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnRefresh.addActionListener(e -> loadData());
        filterPanel.add(btnRefresh);

        add(filterPanel, BorderLayout.NORTH);

        // Chart container
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartContainer.add(new JLabel("Loading chart...", SwingConstants.CENTER), BorderLayout.CENTER);
        add(chartContainer, BorderLayout.CENTER);
    }

    private void loadData() {
        try {
            if (database == null) {
                JOptionPane.showMessageDialog(this, "Database connection not established", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MongoCollection<Document> salesCollection = database.getCollection("Sales");
            Map<String, Double> productSalesAmount = new HashMap<>();

            for (Document sale : salesCollection.find()) {
                String productName = sale.getString("productName");
                double totalPrice = sale.getDouble("totalPrice");
                productSalesAmount.put(productName, productSalesAmount.getOrDefault(productName, 0.0) + totalPrice);
            }

            // Update the chart based on selected type
            String chartType = (String) chartTypeComboBox.getSelectedItem();
            updateChart(productSalesAmount, chartType);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void updateChart(Map<String, Double> productSales, String chartType) {
        chartContainer.removeAll();
        
        try {
            if ("Bar Chart".equals(chartType)) {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                productSales.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        dataset.addValue(entry.getValue(), "Sales", entry.getKey());
                    });

                JFreeChart chart = ChartFactory.createBarChart(
                    "Top Selling Products by Revenue",
                    "Product",
                    "Amount (₹)",
                    dataset
                );
                customizeChart(chart);
                chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
                
            } else if ("Line Chart".equals(chartType)) {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                productSales.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        dataset.addValue(entry.getValue(), "Sales", entry.getKey());
                    });

                JFreeChart chart = ChartFactory.createLineChart(
                    "Top Selling Products by Revenue",
                    "Product",
                    "Amount (₹)",
                    dataset
                );
                customizeChart(chart);
                chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
                
            } else {
                // Default to Pie Chart
                DefaultPieDataset dataset = new DefaultPieDataset();
                productSales.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        dataset.setValue(entry.getKey() + " (₹" + String.format("%.2f", entry.getValue()) + ")", 
                                      entry.getValue());
                    });

                JFreeChart chart = ChartFactory.createPieChart(
                    "Top Selling Products by Revenue",
                    dataset,
                    true,
                    true,
                    false
                );
                customizeChart(chart);
                chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
            }
            
        } catch (Exception e) {
            chartContainer.add(new JLabel("Error creating chart: " + e.getMessage(), SwingConstants.CENTER));
        }
        
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        
        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        } else if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
            plot.setLabelLinkPaint(Color.BLACK);
            plot.setLabelBackgroundPaint(Color.WHITE);
        }
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}