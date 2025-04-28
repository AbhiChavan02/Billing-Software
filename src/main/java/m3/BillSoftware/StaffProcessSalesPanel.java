package m3.BillSoftware;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class StaffProcessSalesPanel extends JPanel {
    private JTextField txtBarcode, txtProductName, txtPricePerGram, txtCurrentGoldRate;
    private JTextField txtQuantity, txtTotalPrice, txtCustomerName, txtFinalPrice;
    private JTextField txtGstAmount, txtMakingCharges, txtTotalAmount, txtMakingChargesPercentage;
    private JTextField txtPhoneNumber, txtAddress;
    private JComboBox<String> cmbStaff;
    private JButton btnProcessSale, btnClear, btnRefresh;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;
    private double grams = 0.0;
    private Double Totalprice = 0.0;
    private JLabel goldRateLabel, RateLabel, makingChargesPercentageLabel;
    private String currentCategory = "";

    public StaffProcessSalesPanel() {
        setLayout(new GridBagLayout());
        setBackground(backgroundColor);
        createForm();
        loadStaffDropdown();
    }

    private void createForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(formColor);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Header
        JLabel headerLabel = new JLabel("Staff Sales Portal");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));

        // Initialize all text fields
        txtCustomerName = createFormTextField(20);
        txtPhoneNumber = createFormTextField(20);
        txtAddress = createFormTextField(20);
        txtBarcode = createFormTextField(20);
        txtProductName = createFormTextField(20);
        txtProductName.setEditable(false);
        txtPricePerGram = createFormTextField(20);
        txtPricePerGram.setEditable(false);
        txtQuantity = createFormTextField(20);
        txtTotalPrice = createFormTextField(20);
        txtTotalPrice.setEditable(false);
        txtFinalPrice = createFormTextField(20);
        txtGstAmount = createFormTextField(20);
        txtMakingCharges = createFormTextField(20);
        txtTotalAmount = createFormTextField(20);
        txtCurrentGoldRate = createFormTextField(20);
        txtMakingChargesPercentage = createFormTextField(5);
        
        cmbStaff = new JComboBox<>();
        cmbStaff.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtGstAmount.setEditable(false);
        txtMakingCharges.setEditable(false);
        txtTotalAmount.setEditable(false);
        
        // Set default making charges percentage
        txtMakingChargesPercentage.setText("14");
        
        // Add document listener for making charges percentage
        txtMakingChargesPercentage.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateMakingCharges(); }
            @Override public void removeUpdate(DocumentEvent e) { updateMakingCharges(); }
            @Override public void changedUpdate(DocumentEvent e) { updateMakingCharges(); }
        });

        // Initialize buttons
        btnRefresh = createActionButton("Refresh", new Color(52, 152, 219));
        btnProcessSale = createActionButton("Process Sale", new Color(46, 204, 113));
        btnClear = createActionButton("Clear", new Color(231, 76, 60));

        // Initialize labels
        goldRateLabel = createFormLabel("Current Gold Rate:");
        RateLabel = createFormLabel("Rate Per Piece:");
        makingChargesPercentageLabel = createFormLabel("Making Charges %:");

        // Layout components
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(headerLabel, gbc);

        // Customer Name
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        formPanel.add(createFormLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtCustomerName, gbc);

        // Phone Number
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createFormLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPhoneNumber, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(createFormLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtAddress, gbc);

        // Staff Member
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(createFormLabel("Staff Member:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbStaff, gbc);

        // Barcode
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(createFormLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        formPanel.add(createInputPanel(txtBarcode, btnRefresh), gbc);

        // Product Name
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(createFormLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductName, gbc);

        // Price Per Gram/Piece
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(RateLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPricePerGram, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(createFormLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtQuantity, gbc);

        // Current Gold Rate
        gbc.gridx = 0;
        gbc.gridy = 9;
        formPanel.add(goldRateLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(txtCurrentGoldRate, gbc);
        
        // Making Charges Percentage
        gbc.gridx = 0;
        gbc.gridy = 10;
        formPanel.add(makingChargesPercentageLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(txtMakingChargesPercentage, gbc);

        // Total Price
        gbc.gridx = 0;
        gbc.gridy = 11;
        formPanel.add(createFormLabel("Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtTotalPrice, gbc);

        // Making Charges
        gbc.gridx = 0;
        gbc.gridy = 12;
        formPanel.add(createFormLabel("Making Charges:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtMakingCharges, gbc);

        // GST Amount
        gbc.gridx = 0;
        gbc.gridy = 13;
        formPanel.add(createFormLabel("GST Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtGstAmount, gbc);

        // Total Amount
        gbc.gridx = 0;
        gbc.gridy = 14;
        formPanel.add(createFormLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtTotalAmount, gbc);

        // Final Price
        gbc.gridx = 0;
        gbc.gridy = 15;
        formPanel.add(createFormLabel("Final Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtFinalPrice, gbc);

        // Hide gold rate fields initially
        goldRateLabel.setVisible(false);
        txtCurrentGoldRate.setVisible(false);
        makingChargesPercentageLabel.setVisible(false);
        txtMakingChargesPercentage.setVisible(false);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(formColor);
        buttonPanel.add(btnProcessSale);
        buttonPanel.add(btnClear);
        formPanel.add(buttonPanel, gbc);

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(900, 700));
        add(scrollPane);

        // Add action listeners
        btnRefresh.addActionListener(e -> fetchProductDetails());
        btnProcessSale.addActionListener(e -> processSale());
        btnClear.addActionListener(e -> clearFields());
        
        // Add document listeners for calculations
        txtQuantity.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCalculations(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCalculations(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCalculations(); }
        });
        
        txtCurrentGoldRate.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateCalculations(); }
            @Override public void removeUpdate(DocumentEvent e) { updateCalculations(); }
            @Override public void changedUpdate(DocumentEvent e) { updateCalculations(); }
        });
    }

    private JPanel createInputPanel(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(formColor);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }

    private JTextField createFormTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return textField;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        
        // Add hover effect
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
    
    private void loadStaffDropdown() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> staffCollection = database.getCollection("Staff");

            List<String> staffNames = new ArrayList<>();
            staffCollection.find().forEach(doc -> 
                staffNames.add(doc.getString("firstname") + " " + doc.getString("lastname"))
            );

            cmbStaff.setModel(new DefaultComboBoxModel<>(staffNames.toArray(new String[0])));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading staff: " + e.getMessage());
        }
    }

    private void fetchProductDetails() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            String barcodeNumber = txtBarcode.getText().trim();
            if (barcodeNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a barcode number.");
                return;
            }

            Document product = productCollection.find(new Document("barcodeNumber", barcodeNumber)).first();
            if (product != null) {
                txtProductName.setText(product.getString("productName"));
                currentCategory = product.getString("category");
                int quantity = 1;
                txtQuantity.setText(String.valueOf(quantity));

                double ratePerPiece = getDoubleValue(product, "ratePerPiece");
                double salesPrice = getDoubleValue(product, "salesPrice");

                if ("Gold".equalsIgnoreCase(currentCategory) || "Silver".equalsIgnoreCase(currentCategory)) {
                    grams = getDoubleValue(product, "grams");
                    txtPricePerGram.setText(String.valueOf(grams));
                    txtCurrentGoldRate.setText("");
                    RateLabel.setText("Grams:");
                    txtCurrentGoldRate.setVisible(true);
                    goldRateLabel.setVisible(true);
                    makingChargesPercentageLabel.setVisible(true);
                    txtMakingChargesPercentage.setVisible(true);

                    txtCurrentGoldRate.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            calculateGoldPrice(grams);
                        }
                    });

                } else if ("Emetation".equalsIgnoreCase(currentCategory)) {
                    txtPricePerGram.setText(String.valueOf(salesPrice));
                    double totalAmount = salesPrice * quantity;
                    txtTotalPrice.setText(String.valueOf(totalAmount));
                    RateLabel.setText("Rate Per Piece");
                    txtCurrentGoldRate.setVisible(false);
                    goldRateLabel.setVisible(false);
                    makingChargesPercentageLabel.setVisible(false);
                    txtMakingChargesPercentage.setVisible(false);
                    updateGSTandFinalAmount(totalAmount, currentCategory);
                } else {
                    txtPricePerGram.setText(String.valueOf(ratePerPiece));
                    double totalAmount = ratePerPiece * quantity;
                    txtTotalPrice.setText(String.valueOf(totalAmount));
                    RateLabel.setText("Rate Per Piece");
                    txtCurrentGoldRate.setVisible(false);
                    goldRateLabel.setVisible(false);
                    makingChargesPercentageLabel.setVisible(false);
                    txtMakingChargesPercentage.setVisible(false);
                    updateGSTandFinalAmount(totalAmount, currentCategory);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }

    private void calculateGoldPrice(double grams) {
        try {
            double goldRate = Double.parseDouble(txtCurrentGoldRate.getText().trim());
            double totalAmount = goldRate * grams;
            txtTotalPrice.setText(String.format("%.2f", totalAmount));
            updateGSTandFinalAmount(totalAmount, currentCategory);
        } catch (NumberFormatException ex) {
            txtTotalPrice.setText("0.00");
            txtGstAmount.setText("0.00");
            txtMakingCharges.setText("0.00");
            txtTotalAmount.setText("0.00");
        }
    }
    
    private void updateCalculations() {
        try {
            double pricePerUnit = Double.parseDouble(txtPricePerGram.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());

            if (currentCategory.equalsIgnoreCase("Gold") || currentCategory.equalsIgnoreCase("Silver")) {
                double goldRate = Double.parseDouble(txtCurrentGoldRate.getText());
                Totalprice = grams * goldRate * quantity;
            } else {
                Totalprice = pricePerUnit * quantity;
            }

            updateGSTandFinalAmount(Totalprice, currentCategory);
            txtTotalPrice.setText(String.format("%.2f", Totalprice));
        } catch (NumberFormatException ex) {
            Totalprice = 0.0; // Set default value
            resetCalculations();
        }
    }
    
    private void updateMakingCharges() {
        try {
            double totalPrice = Double.parseDouble(txtTotalPrice.getText().trim());
            updateGSTandFinalAmount(totalPrice, currentCategory);
        } catch (NumberFormatException e) {
            // Ignore if total price is not set yet
        }
    }

    private void updateGSTandFinalAmount(double totalAmount, String category) {
        double makingChargesRate = 0.0;
        
        try {
            if ("Gold".equalsIgnoreCase(category) || "Silver".equalsIgnoreCase(category)) {
                // Get making charges percentage from text field (1-100)
                double percentage = Double.parseDouble(txtMakingChargesPercentage.getText().trim());
                if (percentage < 0) percentage = 0;
                if (percentage > 100) percentage = 100;
                makingChargesRate = percentage / 100.0;
            }
        } catch (NumberFormatException e) {
            makingChargesRate = 0.14; // Default to 14% if invalid input
        }
        
        double makingCharges = totalAmount * makingChargesRate;
        double newTotal = totalAmount + makingCharges;

        double gstRate = ("Gold".equalsIgnoreCase(category) || "Silver".equalsIgnoreCase(category)) ? 0.03 :
                        ("Emetation".equalsIgnoreCase(category)) ? 0.03 : 0.18;
        
        double gstAmount = newTotal * gstRate;
        double finalAmount = newTotal + gstAmount;

        txtMakingCharges.setText(String.format("%.2f", makingCharges));
        txtGstAmount.setText(String.format("%.2f", gstAmount));
        txtTotalAmount.setText(String.format("%.2f", finalAmount));
        txtFinalPrice.setText(String.format("%.2f", finalAmount));
    }

    private double getDoubleValue(Document product, String key) {
        Object value = product.get(key);
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
        return 0.0;
    }

    private void processSale() {
        try {
            // First update calculations to ensure Totalprice is set
            updateCalculations();
            
            if (txtCustomerName.getText().trim().isEmpty() || cmbStaff.getSelectedItem() == null ||
                txtPhoneNumber.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields (Customer Name, Phone Number, and Staff Member)");
                return;
            }

            // Check if Totalprice is still null (shouldn't happen after updateCalculations())
            if (Totalprice == null) {
                JOptionPane.showMessageDialog(this, "Please complete the product information first");
                return;
            }

            double finalPrice = Double.parseDouble(txtFinalPrice.getText());
            double savings = Totalprice - finalPrice;

            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");
                MongoCollection<Document> salesCollection = database.getCollection("Sales");
                MongoCollection<Document> productCollection = database.getCollection("Product");

                String barcodeNumber = txtBarcode.getText();
                Document product = productCollection.find(new Document("barcodeNumber", barcodeNumber)).first();
                if (product != null) {
                    int currentStock = product.getInteger("stockQuantity");
                    int quantity = Integer.parseInt(txtQuantity.getText().trim());
                    if (quantity > currentStock) {
                        JOptionPane.showMessageDialog(this, "Insufficient stock available!");
                        return;
                    }

                    int newStock = currentStock - quantity;
                    productCollection.updateOne(new Document("barcodeNumber", barcodeNumber),
                            new Document("$set", new Document("stockQuantity", newStock)));

                    Document sale = new Document("productName", txtProductName.getText())
                            .append("quantity", quantity)
                            .append("totalPrice", Totalprice)
                            .append("finalPrice", finalPrice)
                            .append("savings", savings)
                            .append("customerName", txtCustomerName.getText())
                            .append("phoneNumber", txtPhoneNumber.getText())
                            .append("address", txtAddress.getText())
                            .append("staff", cmbStaff.getSelectedItem().toString())
                            .append("timestamp", new java.util.Date());
                    salesCollection.insertOne(sale);

                    JOptionPane.showMessageDialog(this, "Sale processed successfully!");
                    generateInvoice(Totalprice, finalPrice, savings);
                    clearFields();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing sale: " + e.getMessage());
        }
    }

    private void generateInvoice(double totalPrice, double finalPrice, double savings) {
        InvoiceData invoiceData = new InvoiceData();

        // Shop Details
        invoiceData.shopName = "SS GOLD";
        invoiceData.shopAddress = "23, Brabourne Road, Purti Viraat, 5th Floor, Barabazar\n" +
                                  "Kolkata, Kolkata, West Bengal, 700001";
        invoiceData.invoiceNumber = "SSG/" + new Random().nextInt(100) + "/" + 
                                    new SimpleDateFormat("dd-MM").format(new Date());
        invoiceData.invoiceDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        // Customer Details
        invoiceData.customerName = txtCustomerName.getText();
        invoiceData.customerAddress = txtAddress.getText();
        invoiceData.customerPhone = txtPhoneNumber.getText();
        invoiceData.gstin = "";
        invoiceData.pan = "";
        invoiceData.placeOfSupply = "27-Maharashtra";

        // Add items
        InvoiceItem item = new InvoiceItem();
        item.description = txtProductName.getText();
        item.hsnCode = "7117";
        item.quantity = Double.parseDouble(txtQuantity.getText());
        item.rate = Double.parseDouble(txtPricePerGram.getText());
        item.gstPercent = 3.0;
        item.amount = totalPrice;

        invoiceData.items = Arrays.asList(item);

        // Tax and totals
        invoiceData.gstAmount = Double.parseDouble(txtGstAmount.getText());
        invoiceData.taxableValue = totalPrice;
        invoiceData.grandTotal = finalPrice;
        invoiceData.roundOff = finalPrice - (totalPrice + invoiceData.gstAmount);

        // Create and show invoice dialog
        showInvoiceDialog(invoiceData);
    }

    private void showInvoiceDialog(InvoiceData invoiceData) {
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

    private void resetCalculations() {
        txtGstAmount.setText("0.00");
        txtMakingCharges.setText("0.00");
        txtTotalAmount.setText("0.00");
        txtFinalPrice.setText("0.00");
    }

    private void clearFields() {
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerGram.setText("");
        txtQuantity.setText("");
        txtTotalPrice.setText("");
        txtCustomerName.setText("");
        txtPhoneNumber.setText("");
        txtAddress.setText("");
        txtFinalPrice.setText("");
        txtGstAmount.setText("");
        txtMakingCharges.setText("");
        txtTotalAmount.setText("");
        txtMakingChargesPercentage.setText("14");
        currentCategory = "";
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Staff Process Sales");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new StaffProcessSalesPanel());
            frame.setVisible(true);
        });
    }
}