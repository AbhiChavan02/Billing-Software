package m3.BillSoftware;

import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import com.itextpdf.text.Image;
import javax.swing.*;
import org.bson.Document;
import com.itextpdf.text.pdf.*;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.mongodb.client.*;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ProcessSalesPanel extends JPanel {
    
    private JTextField txtBarcode, txtProductName, txtPricePerGram, txtCurrentGoldRate, txtQuantity, txtTotalPrice;
    private JTextField txtName, txtFinalPrice, txtPhoneNumber, txtAddress;
    private JButton btnProcessSale, btnClear, btnRefresh;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;
    private Double Totalprice;
    private JLabel goldRateLabel, RateLabel, gstLabel, makingChargesLabel, totalLabel, makingChargesPercentageLabel;
    private JLabel lblGstAmount, lblMakingCharges, lbltotalamount;
    double grams = 0.0;
    private JTextField txtGstAmount, txtMakingCharges, txtTotalAmount, txtMakingChargesPercentage;
    private String currentCategory = "";

    public ProcessSalesPanel() {
        setLayout(new GridBagLayout());
        setBackground(backgroundColor);
        createForm();
    }
    
    private void createForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(formColor);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Header
        JLabel headerLabel = new JLabel("Start Sales");
        headerLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));

        // Initialize all text fields
        txtName = createFormTextField(20);
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

        // Initialize labels
        lblGstAmount = new JLabel("0.00");
        lblGstAmount.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblMakingCharges = new JLabel("0.00");
        lblMakingCharges.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lbltotalamount = new JLabel("0.00");
        lbltotalamount.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        makingChargesPercentageLabel = new JLabel("Making Charges %:");
        makingChargesPercentageLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));

        // Add document listeners
        txtQuantity.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotalPrice(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotalPrice(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotalPrice(); }
        });
        
        txtCurrentGoldRate.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTotalPriceWithGoldRate(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTotalPriceWithGoldRate(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTotalPriceWithGoldRate(); }
        });

        // Initialize buttons
        btnRefresh = createActionButton("Refresh", new Color(52, 152, 219));
        btnProcessSale = createActionButton("Process Sale", new Color(46, 204, 113));
        btnClear = createActionButton("Clear", new Color(231, 76, 60));

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
        formPanel.add(txtName, gbc);

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

        // Barcode
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(createFormLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        formPanel.add(createInputPanel(txtBarcode, btnRefresh), gbc);

        // Product Name
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(createFormLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductName, gbc);

        // Price Per Gram/Piece
        gbc.gridx = 0;
        gbc.gridy = 6;
        RateLabel = createFormLabel("Rate Per Piece:");
        formPanel.add(RateLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPricePerGram, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(createFormLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtQuantity, gbc);

        // Total Price
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(createFormLabel("Price:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(txtTotalPrice, gbc);
        
        // Current Gold Rate
        goldRateLabel = createFormLabel("Current Gold Rate:");
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
        
        // Making Charges
        gbc.gridx = 0;
        gbc.gridy = 11;
        formPanel.add(createFormLabel("Making Charges:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtMakingCharges, gbc);

        // GST Amount
        gbc.gridx = 0;
        gbc.gridy = 12;
        formPanel.add(createFormLabel("GST Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtGstAmount, gbc);

        // Total Amount
        gbc.gridx = 0;
        gbc.gridy = 13;
        formPanel.add(createFormLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtTotalAmount, gbc);
        
        // Final Price
        gbc.gridx = 0;
        gbc.gridy = 14;
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
        gbc.gridy = 15;
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
        label.setFont(getSafeFont("Segoe UI", java.awt.Font.PLAIN, 14));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }

    private JTextField createFormTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(getSafeFont("Segoe UI", java.awt.Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return textField;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(getSafeFont("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
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
    
    private void updateTotalPrice() {
        try {
            double ratePerPiece = Double.parseDouble(txtPricePerGram.getText());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            double totalPrice = ratePerPiece * quantity;
            txtTotalPrice.setText(String.valueOf(totalPrice));
            updateGSTandFinalAmount(totalPrice, currentCategory);
        } catch (NumberFormatException e) {
            txtTotalPrice.setText("0.0");
        }
    }

    private void updateTotalPriceWithGoldRate() {
        try {
            double currentGoldRate = Double.parseDouble(txtCurrentGoldRate.getText().trim());
            double grams = Double.parseDouble(txtPricePerGram.getText().trim());
            Totalprice = grams * currentGoldRate;
            txtTotalPrice.setText(String.valueOf(Totalprice));
            updateGSTandFinalAmount(Totalprice, currentCategory);
        } catch (NumberFormatException e) {
            txtFinalPrice.setText("Invalid input");
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

    private java.awt.Font getSafeFont(String fontName, int style, int size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();       
        return new java.awt.Font(java.awt.Font.SANS_SERIF, style, size);
    }

    private void fetchProductDetails() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            String barcodeNumber = txtBarcode.getText().trim();
            if (barcodeNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a barcodeNumber.");
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
                    RateLabel.setText("Rate Per Gram");
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
            double pricePerGram = Double.parseDouble(txtPricePerGram.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());
            double totalPrice = pricePerGram * quantity;
            txtTotalPrice.setText(String.valueOf(totalPrice));

            double finalPrice = Double.parseDouble(txtFinalPrice.getText());
            double savings = totalPrice - finalPrice;

            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");
                MongoCollection<Document> salesCollection = database.getCollection("Sales");
                MongoCollection<Document> productCollection = database.getCollection("Product");

                String barcodeNumber = txtBarcode.getText();
                Document product = productCollection.find(new Document("barcodeNumber", barcodeNumber)).first();
                if (product != null) {
                    int currentStock = product.getInteger("stockQuantity");
                    if (quantity > currentStock) {
                        JOptionPane.showMessageDialog(this, "Insufficient stock available!");
                        return;
                    }

                    int newStock = currentStock - quantity;
                    productCollection.updateOne(new Document("barcodeNumber", barcodeNumber),
                            new Document("$set", new Document("stockQuantity", newStock)));

                    Document sale = new Document("productName", txtProductName.getText())
                            .append("quantity", quantity)
                            .append("totalPrice", totalPrice)
                            .append("finalPrice", finalPrice)
                            .append("savings", savings)
                            .append("customerName", txtName.getText())
                            .append("phoneNumber", txtPhoneNumber.getText())
                            .append("address", txtAddress.getText())
                            .append("timestamp", new java.util.Date());
                    salesCollection.insertOne(sale);

                    JOptionPane.showMessageDialog(this, "Sale processed successfully!");
                    generateInvoice(totalPrice, finalPrice, savings);
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
        JDialog invoiceDialog = new JDialog();
        invoiceDialog.setTitle("Invoice");
        invoiceDialog.setSize(595, 842);
        invoiceDialog.setLayout(new BorderLayout());

        JTextPane invoiceContent = new JTextPane();
        invoiceContent.setContentType("text/html");
        invoiceContent.setEditable(false);

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='padding:20px; font-family:Arial;'>");
        html.append("<h1 style='text-align:center;'>RajLaxhmi Jewelers</h1>");
        html.append("<hr>");
        html.append("<p><b>Date:</b> ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())).append("</p>");
        html.append("<p><b>Customer Name:</b> ").append(txtName.getText()).append("</p>");
        html.append("<p><b>Phone Number:</b> ").append(txtPhoneNumber.getText()).append("</p>");
        html.append("<p><b>Address:</b> ").append(txtAddress.getText()).append("</p>");
        html.append("<h3>Product Details:</h3>");

        String imageBase64 = fetchImageFromMongoDB(txtBarcode.getText().trim());
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            html.append("<p style='text-align:center;'><img src='data:image/png;base64,")
                .append(imageBase64)
                .append("' width='100' height='100'/></p>");
        }

        html.append("<p><b>Product Name:</b> ").append(txtProductName.getText()).append("</p>");
        html.append("<p><b>Barcode:</b> ").append(txtBarcode.getText()).append("</p>");
        html.append("<p><b>Quantity:</b> ").append(txtQuantity.getText()).append("</p>");
        html.append("<p><b>Total Price:</b> ₹").append(totalPrice).append("</p>");
        html.append("<p><b>Making Charges (%):</b> ").append(txtMakingChargesPercentage.getText()).append("%</p>");
        html.append("<p><b>Making Charges:</b> ₹").append(txtMakingCharges.getText()).append("</p>");
        html.append("<p><b>GST Amount:</b> ₹").append(txtGstAmount.getText()).append("</p>");
        html.append("<p><b>Final Price:</b> ₹").append(finalPrice).append("</p>");
        html.append("<p><b>Savings:</b> ₹").append(savings).append("</p>");
        html.append("<hr>");
        html.append("<p style='text-align:center;'>Thank you for your business!</p>");
        html.append("</body></html>");

        invoiceContent.setText(html.toString());

        JButton btnPrint = new JButton("Print Invoice");
        btnPrint.addActionListener(e -> {
            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("Invoice Print");
                if (job.printDialog()) {
                    job.print();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage());
            }
        });

        JButton btnDownloadPDF = new JButton("Download PDF");
        btnDownloadPDF.addActionListener(e -> {
            downloadInvoiceAsPDF(txtProductName.getText(), txtBarcode.getText(), 
                txtQuantity.getText(), totalPrice, finalPrice, savings, 
                fetchCloudinaryImageUrl());
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnDownloadPDF);

        invoiceDialog.add(new JScrollPane(invoiceContent), BorderLayout.CENTER);
        invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);
        invoiceDialog.setVisible(true);
    }

    private String fetchImageFromMongoDB(String barcodeNumber) {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            Document product = productCollection.find(new Document("barcodeNumber", barcodeNumber)).first();
            if (product != null) {
                return product.getString("imageBase64");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching image from MongoDB: " + e.getMessage());
        }
        return null;
    }

    public class CloudinaryConfig {
        private static Cloudinary cloudinary;

        static {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dkcxniwte",
                "api_key", "872993699858565",
                "api_secret", "qWa0j2TzlDi7gITYZpaQbwkYKGg"
            ));
        }

        public static Cloudinary getInstance() {
            return cloudinary;
        }
    }
    
    private String fetchCloudinaryImageUrl() {
        try {
            String barcodeNumber = txtBarcode.getText().trim();
            if (barcodeNumber.isEmpty()) {
                return null;
            }

            Cloudinary cloudinary = CloudinaryConfig.getInstance();
            System.out.println("Fetching image for barcodeNumber: " + barcodeNumber);
            Map result = cloudinary.uploader().explicit(barcodeNumber, ObjectUtils.asMap("type", "upload"));
            System.out.println("Cloudinary response: " + result);

            if (result != null && result.containsKey("secure_url")) {
                String imageUrl = result.get("secure_url").toString();
                System.out.println("Image URL found: " + imageUrl);
                return imageUrl;
            } else {
                System.out.println("Image not found in Cloudinary");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error fetching image from Cloudinary: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error fetching image from Cloudinary: " + e.getMessage());
            return null;
        }
    }

    private void downloadInvoiceAsPDF(String productName, String barcodeNumber, String qty, 
                                    double totalPrice, double finalPrice, double savings, 
                                    String cloudinaryImageUrl) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Invoice as PDF");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document(PageSize.A4);
            FileOutputStream fos = null;
            PdfWriter writer = null;

            try {
                fos = new FileOutputStream(file);
                writer = PdfWriter.getInstance(pdfDoc, fos);
                pdfDoc.open();

                BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
                Font titleFont = new Font(baseFont, 20, Font.BOLD, BaseColor.BLACK);
                Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
                Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

                // Title
                Paragraph title = new Paragraph("RajLaxhmi Jewelers - Invoice", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                pdfDoc.add(title);
                pdfDoc.add(new Paragraph("--------------------------------------------------------------"));

                // Customer Info
                pdfDoc.add(new Paragraph("Date: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())));
                pdfDoc.add(new Paragraph("Customer Name: " + txtName.getText().toString()));
                pdfDoc.add(new Paragraph("Phone Number: " + txtPhoneNumber.getText().toString()));
                pdfDoc.add(new Paragraph("Address: " + txtAddress.getText().toString()));
                pdfDoc.add(new Paragraph("\n"));

                // Product Image
                String imageBase64 = fetchImageFromMongoDB(barcodeNumber);
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                    com.itextpdf.text.Image productImage = com.itextpdf.text.Image.getInstance(imageBytes);
                    productImage.scaleToFit(100, 100);
                    productImage.setAlignment(Element.ALIGN_CENTER);
                    pdfDoc.add(productImage);
                }

                // Product Details Table
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("Product Name", boldFont)));
                table.addCell(new PdfPCell(new Phrase(productName, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Barcode", boldFont)));
                table.addCell(new PdfPCell(new Phrase(barcodeNumber, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Quantity", boldFont)));
                table.addCell(new PdfPCell(new Phrase(qty, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Total Price", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + totalPrice, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Making Charges %", boldFont)));
                table.addCell(new PdfPCell(new Phrase(txtMakingChargesPercentage.getText() + "%", normalFont)));

                table.addCell(new PdfPCell(new Phrase("Making Charges", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + txtMakingCharges.getText(), normalFont)));

                table.addCell(new PdfPCell(new Phrase("GST Amount", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + txtGstAmount.getText(), normalFont)));

                table.addCell(new PdfPCell(new Phrase("Final Price", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + finalPrice, normalFont)));

                table.addCell(new PdfPCell(new Phrase("You Saved", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + savings, normalFont)));

                pdfDoc.add(table);
                pdfDoc.add(new Paragraph("\n\nThank you for shopping with RajLaxhmi Jewelers!", boldFont));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving PDF: " + e.getMessage());
            } finally {
                if (pdfDoc != null && pdfDoc.isOpen()) {
                    pdfDoc.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this, "Error closing stream: " + e.getMessage());
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Invoice saved as PDF: " + file.getAbsolutePath());
        }
    }

    private void clearFields() {
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerGram.setText("");
        txtQuantity.setText("");
        txtTotalPrice.setText("");
        txtName.setText("");
        txtPhoneNumber.setText("");
        txtAddress.setText("");
        txtFinalPrice.setText("");
        txtGstAmount.setText("");
        txtMakingCharges.setText("");
        txtTotalAmount.setText("");
        txtMakingChargesPercentage.setText("14");
        currentCategory = "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Process Sales");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new ProcessSalesPanel());
            frame.setVisible(true);
        });
    }
}