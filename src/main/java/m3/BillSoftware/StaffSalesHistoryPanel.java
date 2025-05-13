package m3.BillSoftware;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
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
                mongoClient = MongoClients.create(".");
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

            InvoiceData invoiceData = new InvoiceData();

            // Shop Details
            invoiceData.shopName = "SS GOLD";
            invoiceData.shopAddress = "23, Brabourne Road, Purti Viraat, 5th Floor, Barabazar\n" +
                                      "Kolkata, Kolkata, West Bengal, 700001";
            invoiceData.invoiceNumber = "SSG/" + new Random().nextInt(100) + "/" + 
                                        new SimpleDateFormat("dd-MM").format(new Date());
            invoiceData.invoiceDate = record.timestamp != null ? 
                new SimpleDateFormat("dd/MM/yyyy").format(record.timestamp) : 
                new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            // Customer Details
            invoiceData.customerName = record.customerName != null ? record.customerName : "";
            invoiceData.customerAddress = "";
            invoiceData.customerPhone = "";
            invoiceData.gstin = "";
            invoiceData.pan = "";
            invoiceData.placeOfSupply = "27-Maharashtra";

            // Add items
            InvoiceItem item = new InvoiceItem();
            item.description = record.productName != null ? record.productName : "";
            item.hsnCode = "7117";
            item.quantity = 1;
            item.rate = record.totalPrice;
            item.gstPercent = gstRate * 100;
            item.amount = record.totalPrice;

            invoiceData.items = Arrays.asList(item);

            // Tax and totals
            invoiceData.gstAmount = gstAmount;
            invoiceData.taxableValue = record.totalPrice;
            invoiceData.grandTotal = totalWithGST;
            invoiceData.roundOff = totalWithGST - (record.totalPrice + gstAmount);

            // Create and show invoice dialog
            showInvoiceDialog(invoiceData, image);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating invoice: " + e.getMessage());
        }
    }

    private void showInvoiceDialog(InvoiceData invoiceData, ImageIcon image) {
        JDialog invoiceDialog = new JDialog();
        invoiceDialog.setTitle("Invoice");
        invoiceDialog.setSize(600, 850);
        invoiceDialog.setLayout(new BorderLayout());
        
        JPanel invoicePanel = new JPanel();
        invoicePanel.setLayout(new BoxLayout(invoicePanel, BoxLayout.Y_AXIS));
        invoicePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header
        JLabel shopNameLabel = new JLabel(invoiceData.shopName);
        shopNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        shopNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel shopAddressLabel = new JLabel("<html><center>" + invoiceData.shopAddress.replace("\n", "<br>") + "</center></html>");
        shopAddressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        shopAddressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel debitMemoLabel = new JLabel("Debit Memo");
        debitMemoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        debitMemoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel billedToLabel = new JLabel("Name & Address of the Receiptant (Billed To)");
        billedToLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        billedToLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel customerLabel = new JLabel(invoiceData.customerName);
        customerLabel.setFont(new Font("Arial", Font.BOLD, 12));
        customerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel customerAddressLabel = new JLabel(invoiceData.customerAddress);
        customerAddressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        customerAddressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Customer details section
        JPanel customerDetailsPanel = new JPanel();
        customerDetailsPanel.setLayout(new BoxLayout(customerDetailsPanel, BoxLayout.Y_AXIS));
        customerDetailsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerDetailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        JLabel mobileLabel = new JLabel("Mobile No.: " + invoiceData.customerPhone);
        mobileLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel gstinLabel = new JLabel("GSTIN No.: " + invoiceData.gstin);
        gstinLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel panLabel = new JLabel("PAN No.: " + invoiceData.pan);
        panLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel placeOfSupplyLabel = new JLabel("Place of Supply: " + invoiceData.placeOfSupply);
        placeOfSupplyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        customerDetailsPanel.add(mobileLabel);
        customerDetailsPanel.add(gstinLabel);
        customerDetailsPanel.add(panLabel);
        customerDetailsPanel.add(placeOfSupplyLabel);
        
        // Invoice details
        JPanel invoiceDetailsPanel = new JPanel(new GridLayout(0, 2));
        invoiceDetailsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        addInvoiceDetail(invoiceDetailsPanel, "Invoice No.", invoiceData.invoiceNumber);
        addInvoiceDetail(invoiceDetailsPanel, "Date", invoiceData.invoiceDate);
        addInvoiceDetail(invoiceDetailsPanel, "E Way Bill No.", "");
        addInvoiceDetail(invoiceDetailsPanel, "P. O. No.", "");
        addInvoiceDetail(invoiceDetailsPanel, "P. O. Date", "");
        
        // Items table
        String[] columnNames = {"Sr", "Description of Goods", "HSN/SAC", "Quantity", "Rate", "GST%", "Amount"};
        Object[][] rowData = new Object[invoiceData.items.size()][7];
        
        for (int i = 0; i < invoiceData.items.size(); i++) {
            InvoiceItem item = invoiceData.items.get(i);
            rowData[i][0] = i + 1;
            rowData[i][1] = item.description;
            rowData[i][2] = item.hsnCode;
            rowData[i][3] = item.quantity;
            rowData[i][4] = item.rate;
            rowData[i][5] = item.gstPercent;
            rowData[i][6] = item.amount;
        }
        
        JTable itemsTable = new JTable(rowData, columnNames);
        itemsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        
        // Totals section
        JPanel totalsPanel = new JPanel(new GridLayout(0, 3));
        totalsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add centered content directly
        JLabel label;

        label = new JLabel("GST Amount (In words)", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("Rupees " + convertToWords(invoiceData.gstAmount), SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("Discount", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("P & F Charges", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("Taxable Value", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel(String.format("%.2f", invoiceData.taxableValue), SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("IGST " + invoiceData.items.get(0).gstPercent + "%", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel(String.format("%.2f", invoiceData.gstAmount), SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("Discount", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("0.00", SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("P & F Charge", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("0.00", SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("Round Off", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel(String.format("%.2f", invoiceData.roundOff), SwingConstants.CENTER);
        totalsPanel.add(label);

        label = new JLabel("Grand Total", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel("", SwingConstants.CENTER);
        totalsPanel.add(label);
        label = new JLabel(String.format("%.2f", invoiceData.grandTotal), SwingConstants.CENTER);
        totalsPanel.add(label);
        
        // Terms and conditions
        JTextArea termsArea = new JTextArea();
        termsArea.setText("Terms & Condition:\n" +
                         "1. Goods once sold will not be taken back.\n" +
                         "2. Interest @18% p.a. will be charged if payment is not received within Due date.\n" +
                         "3. Our risk and responsibility ceases as soon as the material leaves our premises.\n" +
                         "4. Subject To Kolkata Jurisdiction Only. E. & O.E.");
        termsArea.setFont(new Font("Arial", Font.PLAIN, 10));
        termsArea.setEditable(false);
        termsArea.setBackground(invoicePanel.getBackground());
        
        // Footer
        JLabel gstinFooterLabel = new JLabel("Company's GST IN No.: " + invoiceData.gstin);
        gstinFooterLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        gstinFooterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel signatoryLabel = new JLabel("For, " + invoiceData.shopName);
        signatoryLabel.setFont(new Font("Arial", Font.BOLD, 12));
        signatoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel authSignLabel = new JLabel("(Authorised Signatory)");
        authSignLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        authSignLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add all components to invoice panel
        invoicePanel.add(shopNameLabel);
        invoicePanel.add(shopAddressLabel);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        invoicePanel.add(debitMemoLabel);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        invoicePanel.add(billedToLabel);
        invoicePanel.add(customerLabel);
        invoicePanel.add(customerAddressLabel);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        invoicePanel.add(customerDetailsPanel);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        invoicePanel.add(invoiceDetailsPanel);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        invoicePanel.add(tableScrollPane);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        invoicePanel.add(totalsPanel);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        invoicePanel.add(termsArea);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        invoicePanel.add(gstinFooterLabel);
        invoicePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        invoicePanel.add(signatoryLabel);
        invoicePanel.add(authSignLabel);
        
        // Buttons
        JButton btnPrint = new JButton("Print Invoice");
        btnPrint.addActionListener(e -> printInvoice(invoiceData));
        
        JButton btnDownloadPDF = new JButton("Download PDF");
        btnDownloadPDF.addActionListener(e -> downloadInvoiceAsPDF(invoiceData));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnDownloadPDF);
        
        invoiceDialog.add(new JScrollPane(invoicePanel), BorderLayout.CENTER);
        invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);
        invoiceDialog.setLocationRelativeTo(this);
        invoiceDialog.setVisible(true);
    }

    private void addInvoiceDetail(JPanel panel, String label, String value) {
        JLabel labelLabel = new JLabel(label + ":");
        labelLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        panel.add(labelLabel);
        panel.add(valueLabel);
    }

    private void printInvoice(InvoiceData invoiceData) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Invoice Print");
        if (job.printDialog()) {
            try {
                job.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage());
            }
        }
    }

    private void downloadInvoiceAsPDF(InvoiceData invoiceData) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Invoice as PDF");
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            
            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                
                // Add content to PDF
                addPdfContent(document, invoiceData);
                
                JOptionPane.showMessageDialog(this, "Invoice saved as PDF: " + file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving PDF: " + e.getMessage());
            } finally {
                if (document != null && document.isOpen()) {
                    document.close();
                }
            }
        }
    }

    private void addPdfContent(com.itextpdf.text.Document document, InvoiceData invoiceData) throws DocumentException {
        // Fonts
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);
        com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.NORMAL);
        
        // Shop header
        Paragraph shopHeader = new Paragraph(invoiceData.shopName + "\n", titleFont);
        shopHeader.setAlignment(Element.ALIGN_CENTER);
        document.add(shopHeader);
        
        Paragraph shopAddress = new Paragraph(invoiceData.shopAddress + "\n\n", normalFont);
        shopAddress.setAlignment(Element.ALIGN_CENTER);
        document.add(shopAddress);
        
        // Debit memo title
        Paragraph debitMemo = new Paragraph("Debit Memo\n", headerFont);
        debitMemo.setAlignment(Element.ALIGN_CENTER);
        document.add(debitMemo);
        
        // Billed to section
        document.add(new Paragraph("Name & Address of the Receiptant (Billed To)", normalFont));
        document.add(new Paragraph(invoiceData.customerName, headerFont));
        document.add(new Paragraph(invoiceData.customerAddress + "\n", normalFont));
        // Customer details
        document.add(new Paragraph("Mobile No.: " + invoiceData.customerPhone, normalFont));
        document.add(new Paragraph("GSTIN No.: " + invoiceData.gstin, normalFont));
        document.add(new Paragraph("PAN No.: " + invoiceData.pan, normalFont));
        document.add(new Paragraph("Place of Supply: " + invoiceData.placeOfSupply + "\n", normalFont));
        
        // Add margin/space after customer details section
        document.add(Chunk.NEWLINE); // This adds a blank line or margin
        // Invoice details table
        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        
        addPdfCell(detailsTable, "Invoice No.:", invoiceData.invoiceNumber, normalFont);
        addPdfCell(detailsTable, "Date:", invoiceData.invoiceDate, normalFont);
        addPdfCell(detailsTable, "E Way Bill No.:", "", normalFont);
        addPdfCell(detailsTable, "P. O. No.:", "", normalFont);
        addPdfCell(detailsTable, "P. O. Date:", "", normalFont);
        
        document.add(detailsTable);
        document.add(new Paragraph("\n"));
        
        // Items table
        PdfPTable itemsTable = new PdfPTable(7);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{0.5f, 2.5f, 1f, 1f, 1f, 1f, 1f});
        
        // Table header
        addPdfHeaderCell(itemsTable, "Sr", headerFont);
        addPdfHeaderCell(itemsTable, "Description of Goods", headerFont);
        addPdfHeaderCell(itemsTable, "HSN/SAC", headerFont);
        addPdfHeaderCell(itemsTable, "Quantity", headerFont);
        addPdfHeaderCell(itemsTable, "Rate", headerFont);
        addPdfHeaderCell(itemsTable, "GST%", headerFont);
        addPdfHeaderCell(itemsTable, "Amount", headerFont);
        
        // Table rows
        for (InvoiceItem item : invoiceData.items) {
            itemsTable.addCell(new PdfPCell(new Phrase(String.valueOf(invoiceData.items.indexOf(item) + 1), normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(item.description, normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(item.hsnCode, normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.quantity), normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.rate), normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.gstPercent), normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.amount), normalFont)));
        }
        
        document.add(itemsTable);
        document.add(new Paragraph("\n"));
        
        // Totals table
        PdfPTable totalsTable = new PdfPTable(3);
        totalsTable.setWidthPercentage(100);
        totalsTable.setWidths(new float[]{2f, 1f, 1f});
        
        addPdfCell(totalsTable, "GST Amount (In words)", "Rupees " + convertToWords(invoiceData.gstAmount), normalFont);
        addPdfCell(totalsTable, "", "Discount", normalFont);
        addPdfCell(totalsTable, "", "", normalFont);
        addPdfCell(totalsTable, "", "P & F Charges", normalFont);
        addPdfCell(totalsTable, "", "", normalFont);
        addPdfCell(totalsTable, "", "Taxable Value", normalFont);
        addPdfCell(totalsTable, "", String.format("%.2f", invoiceData.taxableValue), normalFont);
        addPdfCell(totalsTable, "IGST " + invoiceData.items.get(0).gstPercent + "%", "", normalFont);
        addPdfCell(totalsTable, "", String.format("%.2f", invoiceData.gstAmount), normalFont);
        addPdfCell(totalsTable, "Discount", "", normalFont);
        addPdfCell(totalsTable, "", "0.00", normalFont);
        addPdfCell(totalsTable, "P & F Charge", "", normalFont);
        addPdfCell(totalsTable, "", "0.00", normalFont);
        addPdfCell(totalsTable, "Round Off", "", normalFont);
        addPdfCell(totalsTable, "", String.format("%.2f", invoiceData.roundOff), normalFont);
        addPdfCell(totalsTable, "Grand Total", "", normalFont);
        addPdfCell(totalsTable, "", String.format("%.2f", invoiceData.grandTotal), normalFont);
        
        document.add(totalsTable);
        document.add(new Paragraph("\n"));
        
        // Terms and conditions
        Paragraph terms = new Paragraph("Terms & Condition:\n" +
                                      "1. Goods once sold will not be taken back.\n" +
                                      "2. Interest @18% p.a. will be charged if payment is not received within Due date.\n" +
                                      "3. Our risk and responsibility ceases as soon as the material leaves our premises.\n" +
                                      "4. Subject To Kolkata Jurisdiction Only. E. & O.E.", smallFont);
        document.add(terms);
        
        // Footer
        Paragraph footer = new Paragraph("\nCompany's GST IN No.: " + invoiceData.gstin + "\n\n", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        Paragraph signatory = new Paragraph("For, " + invoiceData.shopName + "\n", headerFont);
        signatory.setAlignment(Element.ALIGN_CENTER);
        document.add(signatory);
        
        Paragraph authSign = new Paragraph("(Authorised Signatory)", smallFont);
        authSign.setAlignment(Element.ALIGN_CENTER);
        document.add(authSign);
    }

    private void addPdfHeaderCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(220, 220, 220));
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, String label, String value, com.itextpdf.text.Font normalFont) {
        if (!label.isEmpty()) {
            table.addCell(new PdfPCell(new Phrase(label, normalFont)));
        } else {
            table.addCell(new PdfPCell(new Phrase("", normalFont)));
        }
        
        if (!value.isEmpty()) {
            PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(valueCell);
        } else {
            table.addCell(new PdfPCell(new Phrase("", normalFont)));
        }
    }

    private String convertToWords(double amount) {
        // Simple number to words conversion (you might want to implement a more complete solution)
        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        String[] teens = {"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        
        int rupees = (int) amount;
        int paise = (int) ((amount - rupees) * 100);
        
        if (rupees == 0) {
            return "Zero Rupees and " + paise + " Paise Only";
        }
        
        String words = "";
        if (rupees >= 10000000) {
            words += convertToWords(rupees / 10000000) + " Crore ";
            rupees %= 10000000;
        }
        if (rupees >= 100000) {
            words += convertToWords(rupees / 100000) + " Lakh ";
            rupees %= 100000;
        }
        if (rupees >= 1000) {
            words += convertToWords(rupees / 1000) + " Thousand ";
            rupees %= 1000;
        }
        if (rupees >= 100) {
            words += units[rupees / 100] + " Hundred ";
            rupees %= 100;
        }
        if (rupees > 0) {
            if (rupees < 10) {
                words += units[rupees];
            } else if (rupees < 20) {
                words += teens[rupees - 10];
            } else {
                words += tens[rupees / 10];
                if (rupees % 10 > 0) {
                    words += " " + units[rupees % 10];
                }
            }
        }
        
        words += " Rupees";
        if (paise > 0) {
            words += " and " + paise + " Paise";
        }
        words += " Only";
        
        return words;
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

                InvoiceData invoiceData = new InvoiceData();

                // Shop Details
                invoiceData.shopName = "SS GOLD";
                invoiceData.shopAddress = "23, Brabourne Road, Purti Viraat, 5th Floor, Barabazar\n" +
                                          "Kolkata, Kolkata, West Bengal, 700001";
                invoiceData.invoiceNumber = "SSG/" + new Random().nextInt(100) + "/" + 
                                            new SimpleDateFormat("dd-MM").format(new Date());
                invoiceData.invoiceDate = timestamp != null ? 
                    new SimpleDateFormat("dd/MM/yyyy").format(timestamp) : 
                    new SimpleDateFormat("dd/MM/yyyy").format(new Date());

                // Customer Details
                invoiceData.customerName = customerName != null ? customerName : "";
                invoiceData.customerAddress = "";
                invoiceData.customerPhone = "";
                invoiceData.gstin = "";
                invoiceData.pan = "";
                invoiceData.placeOfSupply = "27-Maharashtra";

                // Add items
                InvoiceItem item = new InvoiceItem();
                item.description = productName != null ? productName : "";
                item.hsnCode = "7117";
                item.quantity = 1;
                item.rate = totalAmount;
                item.gstPercent = gstRate * 100;
                item.amount = totalAmount;

                invoiceData.items = Arrays.asList(item);

                // Tax and totals
                invoiceData.gstAmount = gstAmount;
                invoiceData.taxableValue = totalAmount;
                invoiceData.grandTotal = totalWithGST;
                invoiceData.roundOff = totalWithGST - (totalAmount + gstAmount);

                downloadInvoiceAsPDF(invoiceData);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(StaffSalesHistoryPanel.this, 
                    "Error creating invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class InvoiceData {
        String shopName;
        String shopAddress;
        String invoiceNumber;
        String invoiceDate;
        String customerName;
        String customerAddress;
        String customerPhone;
        String gstin;
        String pan;
        String placeOfSupply;
        List<InvoiceItem> items;
        double taxableValue;
        double gstAmount;
        double grandTotal;
        double roundOff;
    }

    private static class InvoiceItem {
        String description;
        String hsnCode;
        double quantity;
        double rate;
        double gstPercent;
        double amount;
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