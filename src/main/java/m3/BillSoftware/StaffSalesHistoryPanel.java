package m3.BillSoftware;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StaffSalesHistoryPanel extends JPanel {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;
    private JComboBox<String> monthComboBox;
    private JComboBox<String> productFilterComboBox;
    private JTextField searchField;
    private JLabel totalCollectionLabel;
    private JLabel totalProfitLabel;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private ExecutorService executorService;
    private List<SalesRecord> allSalesRecords; // Store all sales records for filtering and sorting

    public StaffSalesHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        initializeDatabaseConnection();
        createUI();
        loadDataAsync();
    }

    private void initializeDatabaseConnection() {
        executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            try {
                mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/");
                database = mongoClient.getDatabase("testDB");
                database.listCollectionNames().first();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Database connection failed: " + e.getMessage(),
                            "Connection Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    database = null;
                });
            }
        });
    }

    private void createUI() {
        // Table model setup
        tableModel = new DefaultTableModel(new Object[]{
                "Barcode", "Category", "Product Name", "Product Image", "Total Amount",
                "Final Price", "Profit", "Customer Name", "Seller", "Date", "Invoice"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? ImageIcon.class : Object.class; // Column 3 is for images
            }
        };

        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(70);
        salesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        salesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Set cell borders
        salesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JComponent) c).setBorder(new LineBorder(Color.LIGHT_GRAY));
                return c;
            }
        });

        salesTable.getColumnModel().getColumn(3).setPreferredWidth(90); // Product Image column
        salesTable.getColumnModel().getColumn(10).setPreferredWidth(100); // Invoice column

        // Set custom renderer and editor for the "Invoice" column
        salesTable.getColumnModel().getColumn(10).setCellRenderer(new ButtonCellRenderer());
        salesTable.getColumnModel().getColumn(10).setCellEditor(new ButtonCellEditor());

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter panel setup
        JPanel filterPanel = new JPanel(new GridLayout(2, 1)); // Use GridLayout for better organization
        filterPanel.setBackground(formColor);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top row for filters and search
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topRow.setBackground(formColor);

        String[] months = {"All", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthComboBox.addActionListener(e -> filterSalesByMonthAndSearch());
        topRow.add(new JLabel("Filter by Month:"));
        topRow.add(monthComboBox);

        String[] productCategories = {"All", "Emetation", "Gold", "Silver"};
        productFilterComboBox = new JComboBox<>(productCategories);
        productFilterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productFilterComboBox.addActionListener(e -> filterSalesByMonthAndSearch());
        topRow.add(new JLabel("Filter by Product:"));
        topRow.add(productFilterComboBox);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topRow.add(new JLabel("Search:"));
        topRow.add(searchField);

        JButton btnSearch = createActionButton("Search", new Color(52, 152, 219));
        btnSearch.addActionListener(e -> filterSalesByMonthAndSearch());
        topRow.add(btnSearch);

        JButton btnRefresh = createActionButton("Refresh", new Color(52, 152, 219));
        btnRefresh.addActionListener(e -> {
            tableModel.setRowCount(0);
            loadDataAsync();
        });
        topRow.add(btnRefresh);

        filterPanel.add(topRow);

        // Bottom row for sort buttons, total collection, and profit
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomRow.setBackground(formColor);

        // Add sort buttons
        JButton btnSortGold = createActionButton("Sort Gold Sales", new Color(241, 196, 15)); // Yellow
        btnSortGold.addActionListener(e -> sortSalesByCategory("Gold"));
        bottomRow.add(btnSortGold);

        JButton btnSortEmetation = createActionButton("Sort Emetation Sales", new Color(46, 204, 113)); // Green
        btnSortEmetation.addActionListener(e -> sortSalesByCategory("Emetation"));
        bottomRow.add(btnSortEmetation);

        JButton btnSortSilver = createActionButton("Sort Silver Sales", new Color(231, 76, 60)); // Red
        btnSortSilver.addActionListener(e -> sortSalesByCategory("Silver"));
        bottomRow.add(btnSortSilver);

        // Add total collection and profit labels
        totalCollectionLabel = new JLabel("Total Collection: ₹0.00");
        bottomRow.add(totalCollectionLabel);

        totalProfitLabel = new JLabel("Total Profit (Emetation): ₹0.00");
        bottomRow.add(totalProfitLabel);

        filterPanel.add(bottomRow);

        add(filterPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadDataAsync() {
        executorService.execute(() -> {
            try {
                int retries = 0;
                while (database == null && retries < 5) {
                    Thread.sleep(500);
                    retries++;
                }

                if (database == null) throw new IllegalStateException("Database connection timeout");

                allSalesRecords = fetchSalesData();
                SwingUtilities.invokeLater(() -> populateTable(allSalesRecords));

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(),
                                "Data Error", JOptionPane.ERROR_MESSAGE));
            }
        });
    }

    private List<SalesRecord> fetchSalesData() {
        List<SalesRecord> records = new ArrayList<>();
        MongoCollection<Document> salesCollection = database.getCollection("Sales");

        List<String> productNames = new ArrayList<>();
        for (Document sale : salesCollection.find()) {
            String productName = sale.getString("productName");
            if (productName != null) {
                productNames.add(productName);
            }
        }

        MongoCollection<Document> productCollection = database.getCollection("Product");
        List<Document> products = productCollection.find(
                Filters.in("productName", productNames)
        ).into(new ArrayList<>());

        Map<String, Document> productCache = new HashMap<>();
        for (Document product : products) {
            String productName = product.getString("productName");
            if (productName != null) {
                productCache.put(productName, product);
            }
        }

        for (Document sale : salesCollection.find()) {
            String productName = sale.getString("productName");
            Document product = productCache.getOrDefault(productName, new Document());

            // Fetch category directly from the product document
            String category = product.getString("category");
            if (category == null) {
                category = "Unknown";
            }

            // Determine seller type
            String seller = sale.containsKey("staff") ?
                    "Staff: " + sale.getString("staff") : "Admin";

            // Create SalesRecord using getDoubleValue
            SalesRecord record = new SalesRecord(
                    product.getString("barcodeNumber"),
                    category,
                    productName,
                    product.getString("productImagePath"),
                    getDoubleValue(sale, "totalPrice"),
                    getDoubleValue(sale, "finalPrice"),
                    sale.getString("customerName"),
                    seller,
                    sale.getDate("timestamp")
            );
            records.add(record);
        }
        return records;
    }

    private double getDoubleValue(Document document, String key) {
        if (document.containsKey(key)) {
            Object value = document.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format for key: " + key);
                    return 0.0;
                }
            }
        }
        return 0.0;
    }

    private void populateTable(List<SalesRecord> records) {
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
        model.setRowCount(0);

        Map<String, ImageIcon> imageCache = new HashMap<>();

        for (SalesRecord record : records) {
            ImageIcon productImage = imageCache.computeIfAbsent(record.productImagePath, path -> {
                ImageIcon icon = loadProductImage(path);
                if (icon.getImage().getWidth(null) == -1) {
                    executorService.execute(() -> {
                        ImageIcon loadedIcon = loadProductImage(path);
                        imageCache.put(path, loadedIcon);
                        SwingUtilities.invokeLater(() -> updateRowImage(record, loadedIcon));
                    });
                }
                return icon;
            });

            Object[] row = createTableRow(record, productImage);
            model.addRow(row);
        }

        calculateTotalCollectionAndProfit();
    }

    private void updateRowImage(SalesRecord record, ImageIcon image) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Objects.equals(tableModel.getValueAt(i, 2), record.productName)) {
                tableModel.setValueAt(image, i, 3);
                break;
            }
        }
    }

    private Object[] createTableRow(SalesRecord record, ImageIcon image) {
        JButton downloadButton = createActionButton("Download", new Color(52, 152, 219));
        downloadButton.addActionListener(e -> generateInvoice(record, image));

        // Calculate profit
        double profit = record.finalPrice - record.totalPrice;

        return new Object[]{
                record.barcodeNumber != null ? record.barcodeNumber : "",
                record.category != null ? record.category : "",
                record.productName != null ? record.productName : "",
                image,
                record.totalPrice,
                record.finalPrice,
                profit,
                record.customerName != null ? record.customerName : "",
                record.staff != null ? record.staff : "",
                record.timestamp != null ? new SimpleDateFormat("EEE MMM dd").format(record.timestamp) : "",
                downloadButton
        };
    }

    private void calculateTotalCollectionAndProfit() {
        double totalCollection = 0;
        double totalProfit = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                double totalAmount = (Double) tableModel.getValueAt(i, 4);
                double profit = (Double) tableModel.getValueAt(i, 6);
                totalCollection += totalAmount;

                // Calculate profit only for Emetation products
                String category = (String) tableModel.getValueAt(i, 1);
                if (category != null && category.equalsIgnoreCase("Emetation")) {
                    totalProfit += profit;
                }
            } catch (Exception e) {
                System.err.println("Error calculating totals for row " + i + ": " + e.getMessage());
            }
        }

        totalCollectionLabel.setText("Total Collection: ₹" + String.format("%.2f", totalCollection));
        totalProfitLabel.setText("Total Profit (Emetation): ₹" + String.format("%.2f", totalProfit));
    }

    private void sortSalesByCategory(String category) {
        List<SalesRecord> filteredRecords = new ArrayList<>();
        for (SalesRecord record : allSalesRecords) {
            if (record.category != null && record.category.equalsIgnoreCase(category)) {
                filteredRecords.add(record);
            }
        }
        populateTable(filteredRecords);
    }

    private void filterSalesByMonthAndSearch() {
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        String selectedProduct = (String) productFilterComboBox.getSelectedItem();
        String searchText = searchField.getText().toLowerCase().trim();

        List<SalesRecord> filteredRecords = new ArrayList<>();
        for (SalesRecord record : allSalesRecords) {
            // Filter by month
            String recordMonth = record.timestamp != null ? 
                new SimpleDateFormat("MMMM").format(record.timestamp) : "";
            boolean matchesMonth = selectedMonth.equals("All") || recordMonth.equalsIgnoreCase(selectedMonth);

            // Filter by product
            boolean matchesProduct = selectedProduct.equals("All") || 
                (record.category != null && record.category.equalsIgnoreCase(selectedProduct));

            // Filter by search text
            boolean matchesSearch = searchText.isEmpty() ||
                (record.barcodeNumber != null && record.barcodeNumber.toLowerCase().contains(searchText)) ||
                (record.productName != null && record.productName.toLowerCase().contains(searchText)) ||
                (record.customerName != null && record.customerName.toLowerCase().contains(searchText)) ||
                String.valueOf(record.totalPrice).contains(searchText);

            if (matchesMonth && matchesProduct && matchesSearch) {
                filteredRecords.add(record);
            }
        }

        populateTable(filteredRecords);
    }

    private void generateInvoice(SalesRecord record, ImageIcon image) {
        try {
            double savings = record.totalPrice - record.finalPrice;
            double gstRate = "Emetation".equalsIgnoreCase(record.category) ? 0.03 : 0.18;
            double gstAmount = record.finalPrice * gstRate;
            double totalWithGST = record.finalPrice + gstAmount;

            JDialog invoiceDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Invoice", true);
            invoiceDialog.setLayout(new BorderLayout());

            JPanel invoicePanel = new JPanel();
            invoicePanel.setLayout(new BoxLayout(invoicePanel, BoxLayout.Y_AXIS));
            invoicePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            invoicePanel.add(new JLabel("<html><h2>Invoice</h2></html>"));
            invoicePanel.add(new JLabel("Barcode: " + (record.barcodeNumber != null ? record.barcodeNumber : "")));
            invoicePanel.add(new JLabel("Category: " + (record.category != null ? record.category : "")));
            invoicePanel.add(new JLabel("Product: " + (record.productName != null ? record.productName : "")));
            invoicePanel.add(new JLabel("Total Price: ₹" + String.format("%.2f", record.totalPrice)));
            invoicePanel.add(new JLabel("Final Price: ₹" + String.format("%.2f", record.finalPrice)));
            invoicePanel.add(new JLabel("GST (" + (gstRate * 100) + "%): ₹" + String.format("%.2f", gstAmount)));
            invoicePanel.add(new JLabel("Total with GST: ₹" + String.format("%.2f", totalWithGST)));
            invoicePanel.add(new JLabel("Savings: ₹" + String.format("%.2f", savings)));
            invoicePanel.add(new JLabel("Customer: " + (record.customerName != null ? record.customerName : "")));
            invoicePanel.add(new JLabel("Seller: " + (record.staff != null ? record.staff : "")));
            invoicePanel.add(new JLabel("Date: " + (record.timestamp != null ? 
                new SimpleDateFormat("EEE MMM dd").format(record.timestamp) : "")));

            JLabel imageLabel = new JLabel(image);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            invoicePanel.add(Box.createVerticalStrut(10));
            invoicePanel.add(imageLabel);

            JButton downloadButton = createActionButton("Download", new Color(52, 152, 219));
            invoicePanel.add(Box.createVerticalStrut(10));
            invoicePanel.add(downloadButton);

            downloadButton.addActionListener(e -> {
                downloadInvoiceAsPDF(record, image, gstAmount, totalWithGST);
                JOptionPane.showMessageDialog(invoiceDialog, "Invoice Downloaded!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            });

            invoiceDialog.add(invoicePanel, BorderLayout.CENTER);

            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> invoiceDialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);

            invoiceDialog.setSize(350, 450);
            invoiceDialog.setLocationRelativeTo(this);
            invoiceDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating invoice: " + e.getMessage());
        }
    }

    private void downloadInvoiceAsPDF(SalesRecord record, ImageIcon image, double gstAmount, double totalWithGST) {
        try {
            String[] options = {"Print", "E-Invoice"};
            int choice = JOptionPane.showOptionDialog(this, "Select an option:", "Download Invoice",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            double savings = record.totalPrice - record.finalPrice;

            com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
            File file;

            if (choice == 1) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save E-Invoice");
                fileChooser.setSelectedFile(new File(
                    (record.productName != null ? record.productName : "Invoice") + "_E-Invoice.pdf"));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".pdf")) {
                        file = new File(file.getAbsolutePath() + ".pdf");
                    }
                } else {
                    return;
                }
            } else {
                file = new File(System.getProperty("java.io.tmpdir"), 
                    (record.productName != null ? record.productName : "Invoice") + "_Invoice.pdf");
            }

            PdfWriter.getInstance(pdfDoc, new FileOutputStream(file));
            pdfDoc.open();

            pdfDoc.add(new Paragraph("Invoice"));
            pdfDoc.add(new Paragraph(" "));
            pdfDoc.add(new Paragraph("Barcode: " + (record.barcodeNumber != null ? record.barcodeNumber : "")));
            pdfDoc.add(new Paragraph("Category: " + (record.category != null ? record.category : "")));
            pdfDoc.add(new Paragraph("Product Name: " + (record.productName != null ? record.productName : "")));
            pdfDoc.add(new Paragraph("Total Price: ₹" + String.format("%.2f", record.totalPrice)));
            pdfDoc.add(new Paragraph("Final Price: ₹" + String.format("%.2f", record.finalPrice)));
            pdfDoc.add(new Paragraph("GST (" + (gstAmount / record.finalPrice * 100) + "%): ₹" + String.format("%.2f", gstAmount)));
            pdfDoc.add(new Paragraph("Total with GST: ₹" + String.format("%.2f", totalWithGST)));
            pdfDoc.add(new Paragraph("Savings: ₹" + String.format("%.2f", savings)));
            pdfDoc.add(new Paragraph("Customer Name: " + (record.customerName != null ? record.customerName : "")));
            pdfDoc.add(new Paragraph("Seller: " + (record.staff != null ? record.staff : "")));
            pdfDoc.add(new Paragraph("Date: " + (record.timestamp != null ? 
                new SimpleDateFormat("EEE MMM dd").format(record.timestamp) : "")));
            pdfDoc.add(new Paragraph(" "));
            pdfDoc.add(new Paragraph("Thank you for your business!"));

            pdfDoc.close();

            if (choice == 0) {
                printPDF(file);
            } else {
                JOptionPane.showMessageDialog(this, "E-Invoice saved successfully: " + file.getAbsolutePath());
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage());
        }
    }

    private void printPDF(File file) {
        try {
            PDDocument document = PDDocument.load(file);
            PrinterJob job = PrinterJob.getPrinterJob();

            if (job.printDialog()) {
                job.setPageable(new PDFPageable(document));
                job.print();
                JOptionPane.showMessageDialog(this, "Printing started...");
            } else {
                JOptionPane.showMessageDialog(this, "Printing cancelled.");
            }

            document.close();
        } catch (IOException | PrinterException e) {
            JOptionPane.showMessageDialog(this, "Printing error: " + e.getMessage());
        }
    }

    private ImageIcon loadProductImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = new ImageIcon(new URL(imageUrl)).getImage();
                if (image == null) {
                    return getDefaultImage();
                }
                Image scaledImage = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            } else {
                return getDefaultImage();
            }
        } catch (Exception e) {
            return getDefaultImage();
        }
    }

    private ImageIcon getDefaultImage() {
        BufferedImage image = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, 60, 60);
        g2d.setColor(Color.BLACK);
        g2d.drawString("No Image", 10, 30);
        g2d.dispose();
        return new ImageIcon(image);
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private class SalesRecord {
        final String barcodeNumber;
        final String category;
        final String productName;
        final String productImagePath;
        final double totalPrice;
        final double finalPrice;
        final String customerName;
        final String staff;
        final Date timestamp;

        SalesRecord(String barcodeNumber, String category, String productName, String productImagePath,
                    double totalPrice, double finalPrice, String customerName,
                    String staff, Date timestamp) {
            this.barcodeNumber = barcodeNumber;
            this.category = category;
            this.productName = productName;
            this.productImagePath = productImagePath;
            this.totalPrice = totalPrice;
            this.finalPrice = finalPrice;
            this.customerName = customerName;
            this.staff = staff;
            this.timestamp = timestamp;
        }
    }

    private class ButtonCellRenderer extends JButton implements TableCellRenderer {
        public ButtonCellRenderer() {
            setOpaque(true);
            setBackground(new Color(231, 76, 60));
            setForeground(Color.WHITE);
            setText("Download PDF");
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                      boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(new Color(192, 57, 43));
            } else {
                setBackground(new Color(231, 76, 60));
            }
            return this;
        }
    }

    private class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JButton button;
        private int currentRow;

        public ButtonCellEditor() {
            button = new JButton("Download PDF");
            button.setBackground(new Color(46, 204, 113));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                    boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            downloadProductPDF(currentRow);
            fireEditingStopped();
        }

        private void downloadProductPDF(int row) {
            String barcodeNumber = (String) tableModel.getValueAt(row, 0);
            String category = (String) tableModel.getValueAt(row, 1);
            String productName = (String) tableModel.getValueAt(row, 2);
            double totalAmount = (Double) tableModel.getValueAt(row, 4);
            double finalPrice = (Double) tableModel.getValueAt(row, 5);
            String customerName = (String) tableModel.getValueAt(row, 7);
            String seller = (String) tableModel.getValueAt(row, 8);
            String timestampStr = (String) tableModel.getValueAt(row, 9);

            try {
                Date timestamp = timestampStr != null && !timestampStr.isEmpty() ? 
                    new SimpleDateFormat("EEE MMM dd").parse(timestampStr) : new Date();

                SalesRecord record = new SalesRecord(
                        barcodeNumber, category, productName, "", totalAmount, finalPrice, customerName, seller, timestamp
                );

                double gstRate = "Emetation".equalsIgnoreCase(category) ? 0.03 : 0.18;
                double gstAmount = finalPrice * gstRate;
                double totalWithGST = finalPrice + gstAmount;

                downloadInvoiceAsPDF(record, new ImageIcon(), gstAmount, totalWithGST);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(StaffSalesHistoryPanel.this, 
                    "Error creating invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}