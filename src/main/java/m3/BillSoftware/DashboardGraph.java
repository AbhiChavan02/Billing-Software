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

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardGraph extends JPanel {
    private JComboBox<String> chartTypeComboBox;
    private JComboBox<String> timePeriodComboBox;
    private JButton btnRefresh;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private JPanel chartContainer;
    private JLabel statusLabel;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DashboardGraph() {
        setLayout(new BorderLayout());
        setBackground(new Color(241, 242, 246));
        initializeDatabaseConnection();
        createUI();
        loadData();
    }

    private void initializeDatabaseConnection() {
        try {
            mongoClient = MongoClients.create("");
            database = mongoClient.getDatabase("testDB");
        } catch (Exception e) {
            showError("Database connection failed: " + e.getMessage());
        }
    }

    private void createUI() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 58, 82));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Sales Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Status label
        statusLabel = new JLabel("Loading data...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(Color.WHITE);
        headerPanel.add(statusLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Chart type selection
        String[] chartTypes = {"Pie Chart", "Bar Chart", "Line Chart"};
        chartTypeComboBox = new JComboBox<>(chartTypes);
        styleComboBox(chartTypeComboBox);
        chartTypeComboBox.addActionListener(e -> loadData());
        filterPanel.add(createFilterLabel("Chart Type:"));
        filterPanel.add(chartTypeComboBox);

        // Time period selection
        String[] timePeriods = {"All Time", "Last Month", "Last 3 Months", "Last 6 Months", "Last Year"};
        timePeriodComboBox = new JComboBox<>(timePeriods);
        styleComboBox(timePeriodComboBox);
        timePeriodComboBox.addActionListener(e -> loadData());
        filterPanel.add(createFilterLabel("Time Period:"));
        filterPanel.add(timePeriodComboBox);

        // Refresh button
        btnRefresh = new JButton("Refresh Data");
        styleButton(btnRefresh, new Color(52, 152, 219));
        btnRefresh.addActionListener(e -> loadData());
        filterPanel.add(btnRefresh);

        add(filterPanel, BorderLayout.CENTER);

        // Chart container with card layout for smooth transitions
        chartContainer = new JPanel(new CardLayout());
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Loading panel
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        JLabel loadingLabel = new JLabel("Loading chart data...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        chartContainer.add(loadingPanel, "loading");
        
        add(chartContainer, BorderLayout.SOUTH);
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }

    private void loadData() {
        executorService.execute(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Loading data...");
                    btnRefresh.setEnabled(false);
                });

                if (database == null) {
                    throw new Exception("Database connection not established");
                }

                MongoCollection<Document> salesCollection = database.getCollection("Sales");
                Map<String, Double> productSalesAmount = new HashMap<>();

                Date startDate = getStartDateForPeriod((String) timePeriodComboBox.getSelectedItem());
                
                for (Document sale : salesCollection.find()) {
                    // Check if sale is within the selected time period
                    if (startDate != null) {
                        Date saleDate = sale.getDate("timestamp");
                        if (saleDate == null || saleDate.before(startDate)) {
                            continue;
                        }
                    }

                    String productName = sale.getString("productName");
                    Double totalPrice = safeGetDouble(sale, "totalPrice");
                    Double finalPrice = safeGetDouble(sale, "finalPrice");
                    
                    // Use finalPrice if available, otherwise fall back to totalPrice
                    double amount = finalPrice != null ? finalPrice : 
                                   totalPrice != null ? totalPrice : 0.0;
                    
                    if (amount > 0) {
                        productSalesAmount.merge(productName, amount, Double::sum);
                    }
                }

                if (productSalesAmount.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        showStatus("No sales data found for selected period");
                    });
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    updateChart(productSalesAmount, (String) chartTypeComboBox.getSelectedItem());
                    showStatus("Data loaded successfully");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Error loading data: " + e.getMessage());
                });
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    btnRefresh.setEnabled(true);
                });
            }
        });
    }

    private Double safeGetDouble(Document doc, String key) {
        try {
            return doc.getDouble(key);
        } catch (Exception e) {
            return null;
        }
    }

    private Date getStartDateForPeriod(String period) {
        if (period == null || "All Time".equals(period)) {
            return null;
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "Last Month":
                startDate = now.minusMonths(1);
                break;
            case "Last 3 Months":
                startDate = now.minusMonths(3);
                break;
            case "Last 6 Months":
                startDate = now.minusMonths(6);
                break;
            case "Last Year":
                startDate = now.minusYears(1);
                break;
            default:
                return null;
        }

        return Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void updateChart(Map<String, Double> productSales, String chartType) {
        chartContainer.removeAll();
        
        try {
            if (productSales.isEmpty()) {
                JLabel noDataLabel = new JLabel("No sales data available", SwingConstants.CENTER);
                noDataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                chartContainer.add(noDataLabel, BorderLayout.CENTER);
                return;
            }

            ChartPanel chartPanel;
            
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
                chartPanel = new ChartPanel(chart);
                
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
                chartPanel = new ChartPanel(chart);
                
            } else {
                // Default to Pie Chart
                DefaultPieDataset dataset = new DefaultPieDataset();
                productSales.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        dataset.setValue(entry.getKey() + " (₹" + String.format("%,.2f", entry.getValue()) + ")", 
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
                chartPanel = new ChartPanel(chart);
            }

            chartPanel.setPreferredSize(new Dimension(800, 500));
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error creating chart: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            chartContainer.add(errorLabel, BorderLayout.CENTER);
        }
        
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
            plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
        } else if (chart.getPlot() instanceof PiePlot) {
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 12));
            plot.setLabelLinkPaint(Color.BLACK);
            plot.setLabelBackgroundPaint(Color.WHITE);
        }
    }

    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(new Color(40, 58, 82));
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}