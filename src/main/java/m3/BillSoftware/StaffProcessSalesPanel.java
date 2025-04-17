package m3.BillSoftware;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.mongodb.client.*;
import org.bson.Document;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.Font;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.ArrayList; // For ArrayList implementation

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
    private Double Totalprice;
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

        // Form components
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

        btnRefresh = createActionButton("Refresh", new Color(52, 152, 219));
        btnProcessSale = createActionButton("Process Sale", new Color(46, 204, 113));
        btnClear = createActionButton("Clear", new Color(231, 76, 60));

        // Initialize labels
        makingChargesPercentageLabel = new JLabel("Making Charges %:");
        makingChargesPercentageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Layout
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

        // Staff Dropdown
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(createFormLabel("Staff Member:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbStaff, gbc);

        // Barcode Field
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

        // Price/Grams
        RateLabel = createFormLabel("Rate Per Piece:");
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
        
        // Current Gold Rate (hidden initially)
        goldRateLabel = createFormLabel("Current Gold Rate:");
        gbc.gridx = 0;
        gbc.gridy = 9;
        formPanel.add(goldRateLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(txtCurrentGoldRate, gbc);
        goldRateLabel.setVisible(false);
        txtCurrentGoldRate.setVisible(false);
        
        // Making Charges Percentage
        gbc.gridx = 0;
        gbc.gridy = 10;
        formPanel.add(makingChargesPercentageLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(txtMakingChargesPercentage, gbc);
        makingChargesPercentageLabel.setVisible(false);
        txtMakingChargesPercentage.setVisible(false);

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

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 16;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.add(btnProcessSale);
        buttonPanel.add(btnClear);
        formPanel.add(buttonPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(900, 700));
        add(scrollPane);

        // Add listeners
        txtQuantity.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCalculations(); }
            public void removeUpdate(DocumentEvent e) { updateCalculations(); }
            public void changedUpdate(DocumentEvent e) { updateCalculations(); }
        });
        
        txtCurrentGoldRate.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCalculations(); }
            public void removeUpdate(DocumentEvent e) { updateCalculations(); }
            public void changedUpdate(DocumentEvent e) { updateCalculations(); }
        });

        btnRefresh.addActionListener(e -> fetchProductDetails());
        btnProcessSale.addActionListener(e -> processSale());
        btnClear.addActionListener(e -> clearFields());
    }

    private void loadStaffDropdown() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> staffCollection = database.getCollection("Staff");

            java.util.List<String> staffNames = new ArrayList<String>();

            staffCollection.find().forEach(doc -> 
                staffNames.add(doc.getString("firstname") + " " + doc.getString("lastname"))
            );

            cmbStaff.setModel(new DefaultComboBoxModel<>(staffNames.toArray(new String[0])));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading staff: " + e.getMessage());
        }
    }

    private JTextField createFormTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        
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

    private JPanel createInputPanel(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(formColor);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
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

                if ("Gold".equalsIgnoreCase(currentCategory) || "Silver".equalsIgnoreCase(currentCategory)) {
                    grams = product.getDouble("grams");
                    txtPricePerGram.setText(String.valueOf(grams));
                    RateLabel.setText("Grams:");
                    goldRateLabel.setVisible(true);
                    txtCurrentGoldRate.setVisible(true);
                    makingChargesPercentageLabel.setVisible(true);
                    txtMakingChargesPercentage.setVisible(true);
                } else {
                    double rate = "Emetation".equalsIgnoreCase(currentCategory) ? 
                        product.getDouble("salesPrice") : 
                        product.getDouble("ratePerPiece");
                    txtPricePerGram.setText(String.valueOf(rate));
                    RateLabel.setText("Rate Per Piece:");
                    goldRateLabel.setVisible(false);
                    txtCurrentGoldRate.setVisible(false);
                    makingChargesPercentageLabel.setVisible(false);
                    txtMakingChargesPercentage.setVisible(false);
                }
                updateCalculations();
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
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

    private void processSale() {
        try {
            if (txtCustomerName.getText().trim().isEmpty() || cmbStaff.getSelectedItem() == null ||
                txtPhoneNumber.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields (Customer Name, Phone Number, and Staff Member)");
                return;
            }

            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");
                Document product = database.getCollection("Product")
                                        .find(new Document("barcodeNumber", txtBarcode.getText().trim()))
                                        .first();

                int currentStock = product.getInteger("stockQuantity");
                int quantity = Integer.parseInt(txtQuantity.getText().trim());
                if (quantity > currentStock) {
                    JOptionPane.showMessageDialog(this, "Insufficient stock available!");
                    return;
                }

                // Update stock
                database.getCollection("Product")
                       .updateOne(new Document("barcodeNumber", txtBarcode.getText().trim()),
                                new Document("$inc", new Document("stockQuantity", -quantity)));

                // Create sale record
                Document sale = new Document()
                        .append("customerName", txtCustomerName.getText().trim())
                        .append("phoneNumber", txtPhoneNumber.getText().trim())
                        .append("address", txtAddress.getText().trim())
                        .append("staff", cmbStaff.getSelectedItem().toString())
                        .append("productName", txtProductName.getText().trim())
                        .append("quantity", quantity)
                        .append("totalPrice", Totalprice)
                        .append("makingCharges", Double.parseDouble(txtMakingCharges.getText()))
                        .append("gstAmount", Double.parseDouble(txtGstAmount.getText()))
                        .append("finalPrice", Double.parseDouble(txtFinalPrice.getText()))
                        .append("timestamp", new Date());

                database.getCollection("Sales").insertOne(sale);

                JOptionPane.showMessageDialog(this, "Sale processed successfully!");
                generateInvoice();
                clearFields();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing sale: " + e.getMessage());
        }
    }

    private void generateInvoice() {
        JDialog invoiceDialog = new JDialog();
        invoiceDialog.setTitle("Invoice");
        invoiceDialog.setSize(500, 700);
        invoiceDialog.setLayout(new BorderLayout());

        JTextPane invoiceContent = new JTextPane();
        invoiceContent.setContentType("text/html");
        invoiceContent.setEditable(false);

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='padding:20px;'>")
            .append("<h1 style='text-align:center;'>ABC Jewelers</h1>")
            .append("<hr>")
            .append("<p><b>Date:</b> ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())).append("</p>")
            .append("<p><b>Customer:</b> ").append(txtCustomerName.getText()).append("</p>")
            .append("<p><b>Phone:</b> ").append(txtPhoneNumber.getText()).append("</p>")
            .append("<p><b>Address:</b> ").append(txtAddress.getText()).append("</p>")
            .append("<p><b>Staff:</b> ").append(cmbStaff.getSelectedItem()).append("</p>")
            .append("<h3>Product Details:</h3>");

        // Add image if available
        String imageUrl = fetchCloudinaryImageUrl();
        if (imageUrl != null) {
            html.append("<p style='text-align:center;'><img src='").append(imageUrl).append("' width='100' height='100'/></p>");
        }

        html.append("<p><b>Product:</b> ").append(txtProductName.getText()).append("</p>")
            .append("<p><b>Barcode:</b> ").append(txtBarcode.getText()).append("</p>")
            .append("<p><b>Quantity:</b> ").append(txtQuantity.getText()).append("</p>")
            .append("<p><b>Total Price:</b> ₹").append(Totalprice).append("</p>");
            
        if ("Gold".equalsIgnoreCase(currentCategory) || "Silver".equalsIgnoreCase(currentCategory)) {
            html.append("<p><b>Making Charges (%):</b> ").append(txtMakingChargesPercentage.getText()).append("%</p>");
        }
            
        html.append("<p><b>Making Charges:</b> ₹").append(txtMakingCharges.getText()).append("</p>")
            .append("<p><b>GST:</b> ₹").append(txtGstAmount.getText()).append("</p>")
            .append("<p><b>Final Amount:</b> ₹").append(txtFinalPrice.getText()).append("</p>")
            .append("<hr><p style='text-align:center;'>Thank you for your business!</p></body></html>");

        invoiceContent.setText(html.toString());

        // Add buttons
        JButton btnPrint = new JButton("Print Invoice");
        btnPrint.addActionListener(e -> printInvoice());

        JButton btnPDF = new JButton("Save PDF");
        btnPDF.addActionListener(e -> saveAsPDF());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnPDF);

        invoiceDialog.add(new JScrollPane(invoiceContent), BorderLayout.CENTER);
        invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);
        invoiceDialog.setVisible(true);
    }

    private void printInvoice() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            if (job.printDialog()) {
                job.print();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Print error: " + e.getMessage());
        }
    }

    private void saveAsPDF() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParent(), file.getName() + ".pdf");
            }

            com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                PdfWriter.getInstance(pdfDoc, fos);
                pdfDoc.open();

                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
                pdfDoc.add(new Paragraph("ABC Jewelers - Invoice", titleFont));
                pdfDoc.add(new Paragraph("Date: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())));
                pdfDoc.add(new Paragraph("Customer: " + txtCustomerName.getText()));
                pdfDoc.add(new Paragraph("Phone: " + txtPhoneNumber.getText()));
                pdfDoc.add(new Paragraph("Address: " + txtAddress.getText()));
                pdfDoc.add(new Paragraph("Staff: " + cmbStaff.getSelectedItem()));
                pdfDoc.add(new Paragraph("Product: " + txtProductName.getText()));
                pdfDoc.add(new Paragraph("Barcode: " + txtBarcode.getText()));
                pdfDoc.add(new Paragraph("Quantity: " + txtQuantity.getText()));
                pdfDoc.add(new Paragraph("Total Price: ₹" + Totalprice));
                
                if ("Gold".equalsIgnoreCase(currentCategory) || "Silver".equalsIgnoreCase(currentCategory)) {
                    pdfDoc.add(new Paragraph("Making Charges (%): " + txtMakingChargesPercentage.getText() + "%"));
                }
                
                pdfDoc.add(new Paragraph("Making Charges: ₹" + txtMakingCharges.getText()));
                pdfDoc.add(new Paragraph("GST: ₹" + txtGstAmount.getText()));
                pdfDoc.add(new Paragraph("Final Amount: ₹" + txtFinalPrice.getText()));

                pdfDoc.close();
                JOptionPane.showMessageDialog(this, "Invoice saved as PDF: " + file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving PDF: " + e.getMessage());
            }
        }
    }

    private String fetchCloudinaryImageUrl() {
        try {
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dkcxniwte",
                "api_key", "872993699858565",
                "api_secret", "qWa0j2TzlDi7gITYZpaQbwkYKGg"
            ));

            Map result = cloudinary.uploader().explicit(txtBarcode.getText(), 
                ObjectUtils.asMap("type", "upload"));

            return result.containsKey("secure_url") ? result.get("secure_url").toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void resetCalculations() {
        txtGstAmount.setText("0.00");
        txtMakingCharges.setText("0.00");
        txtTotalAmount.setText("0.00");
        txtFinalPrice.setText("0.00");
    }

    private void clearFields() {
        txtCustomerName.setText("");
        txtPhoneNumber.setText("");
        txtAddress.setText("");
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerGram.setText("");
        txtQuantity.setText("");
        txtTotalPrice.setText("");
        txtFinalPrice.setText("");
        txtCurrentGoldRate.setText("");
        txtMakingChargesPercentage.setText("14");
        resetCalculations();
        cmbStaff.setSelectedIndex(0);
    }
}