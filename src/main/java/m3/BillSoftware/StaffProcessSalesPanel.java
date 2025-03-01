package m3.BillSoftware;

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
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.DocumentException;

public class StaffProcessSalesPanel extends JPanel {
    private JTextField txtBarcode, txtProductName, txtPricePerGram, txtQuantity, 
                       txtTotalPrice, txtStockQuantity, txtCustomerName, txtFinalPrice, txtCurrentGoldRate;
    private JComboBox<String> cmbStaff;
    private JButton btnProcessSale, btnClear, btnRefresh;
    private JLabel goldRateLabel, RateLabel, gstLabel, makingChargesLabel, totalLabel, 
    lblGstAmount, lblMakingCharges, lbltotalamount;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;
    private double grams = 0.0;
    private Double Totalprice;

    public StaffProcessSalesPanel() {
        setLayout(new GridBagLayout());
        setBackground(backgroundColor);
        initializeUI();
        loadStaffDropdown();
        setupActionListeners();
    }

    private void initializeUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(formColor);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Header
        JLabel headerLabel = new JLabel("Staff Sales Portal");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(headerLabel, gbc);

        // Initialize RateLabel
        RateLabel = createFormLabel("Rate Per Piece:"); // Initialize RateLabel here

        // Form Fields
        String[] labels = {
            "Customer Name:", "Staff Member:", "Barcode:", 
            "Product Name:", "Rate/Grams:", "Stock Quantity:", 
            "Quantity:", "Total Price:", "Final Price:"
        };

        Component[] fields = {
            txtCustomerName = createFormTextField(20),
            cmbStaff = new JComboBox<>(),
            createInputPanel(txtBarcode = createFormTextField(15), btnRefresh = createActionButton("Refresh", new Color(52, 152, 219))),
            txtProductName = createFormTextField(20),
            txtPricePerGram = createFormTextField(20),
            txtStockQuantity = createFormTextField(20),
            txtQuantity = createFormTextField(20),
            txtTotalPrice = createFormTextField(20),
            txtFinalPrice = createFormTextField(20)
        };

        // Configure uneditable fields
        txtProductName.setEditable(false);
        txtPricePerGram.setEditable(false);
        txtStockQuantity.setEditable(false);
        txtTotalPrice.setEditable(false);
        txtFinalPrice.setEditable(false);

        // Gold Rate Fields (hidden initially)
        goldRateLabel = createFormLabel("Current Gold Rate:");
        txtCurrentGoldRate = createFormTextField(20);
        goldRateLabel.setVisible(false);
        txtCurrentGoldRate.setVisible(false);

        // Add main form components
        for(int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.gridwidth = 1;
            formPanel.add(createFormLabel(labels[i]), gbc);
            
            gbc.gridx = 1;
            formPanel.add(fields[i], gbc);
        }

        // Add Gold Rate fields
        gbc.gridy = labels.length + 1;
        gbc.gridx = 0;
        formPanel.add(goldRateLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(txtCurrentGoldRate, gbc);

        // Calculation Labels
        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(createFormLabel("GST Amount:"), gbc);
        gbc.gridx = 1;
        lblGstAmount = createValueLabel();
        formPanel.add(lblGstAmount, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(createFormLabel("Making Charges:"), gbc);
        gbc.gridx = 1;
        lblMakingCharges = createValueLabel();
        formPanel.add(lblMakingCharges, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        formPanel.add(createFormLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        lbltotalamount = createValueLabel();
        formPanel.add(lbltotalamount, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(formColor);
        btnProcessSale = createActionButton("Process Sale", new Color(46, 204, 113));
        btnClear = createActionButton("Clear Form", new Color(231, 76, 60));
        buttonPanel.add(btnProcessSale);
        buttonPanel.add(btnClear);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(900, 700));
        add(scrollPane);

        // Add document listeners
        DocumentListener updateListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateCalculations(); }
            public void removeUpdate(DocumentEvent e) { updateCalculations(); }
            public void changedUpdate(DocumentEvent e) { updateCalculations(); }
        };
        txtQuantity.getDocument().addDocumentListener(updateListener);
        txtCurrentGoldRate.getDocument().addDocumentListener(updateListener);
    }

    private void setupActionListeners() {
        btnRefresh.addActionListener(e -> fetchProductDetails());
        btnProcessSale.addActionListener(e -> processSale());
        btnClear.addActionListener(e -> clearFields());
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

    private JLabel createValueLabel() {
        JLabel label = new JLabel("0.00");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(40, 58, 82));
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
    
    private void updateCalculations() {
        try {
            String category = getProductCategory();
            double pricePerUnit = Double.parseDouble(txtPricePerGram.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());

            if (category.equalsIgnoreCase("Gold") || category.equalsIgnoreCase("Silver")) {
                double goldRate = Double.parseDouble(txtCurrentGoldRate.getText());
                Totalprice = grams * goldRate * quantity;
            } else {
                Totalprice = pricePerUnit * quantity;
            }

            updateGSTandFinalAmount(Totalprice, category);
            txtTotalPrice.setText(String.format("%.2f", Totalprice));
        } catch (NumberFormatException ex) {
            resetCalculations();
        }
    }
    
    private void updateGSTandFinalAmount(double totalAmount, String category) {
        double gstRate = category.equalsIgnoreCase("Gold") || category.equalsIgnoreCase("Silver") ? 0.03 :
                        category.equalsIgnoreCase("Emetation") ? 0.05 : 0.18;
        
        double makingChargeRate = category.equalsIgnoreCase("Gold") || category.equalsIgnoreCase("Silver") ? 0.10 : 0.0;

        double gstAmount = totalAmount * gstRate;
        double makingCharges = totalAmount * makingChargeRate;
        double finalAmount = totalAmount + gstAmount + makingCharges;

        lblGstAmount.setText(String.format("%.2f", gstAmount));
        lblMakingCharges.setText(String.format("%.2f", makingCharges));
        lbltotalamount.setText(String.format("%.2f", finalAmount));
        txtFinalPrice.setText(String.format("%.2f", finalAmount));
    }
    
    private String getProductCategory() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            return mongoClient.getDatabase("testDB")
                            .getCollection("Product")
                            .find(new Document("barcode", txtBarcode.getText().trim()))
                            .first()
                            .getString("category");
        } catch (Exception e) {
            return "";
        }
    }

    private void fetchProductDetails() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a barcode");
            return;
        }

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            Document product = database.getCollection("Product")
                                    .find(new Document("barcode", barcode))
                                    .first();

            if (product != null) {
                txtProductName.setText(product.getString("productName"));
                String category = product.getString("category");

                if (category.equalsIgnoreCase("Gold") || category.equalsIgnoreCase("Silver")) {
                    grams = product.getDouble("grams");
                    txtPricePerGram.setText(String.format("%.2f", grams));
                    goldRateLabel.setVisible(true);
                    txtCurrentGoldRate.setVisible(true);
                    RateLabel.setText("Grams:");
                } else {
                    // Handle Emetation and other categories
                    double rate;
                    if (category.equalsIgnoreCase("Emetation")) {
                        rate = product.getDouble("salesPrice"); // Use salesPrice for Emetation
                    } else {
                        rate = product.getDouble("ratePerPiece"); // Use ratePerPiece for others
                    }
                    txtPricePerGram.setText(String.format("%.2f", rate));
                    goldRateLabel.setVisible(false);
                    txtCurrentGoldRate.setVisible(false);
                    RateLabel.setText("Rate Per Piece:");
                }

                txtStockQuantity.setText(String.valueOf(product.getInteger("stockQuantity")));
                txtQuantity.setText("1");
                updateCalculations();
            } else {
                JOptionPane.showMessageDialog(this, "Product not found!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void processSale() {
        try {
            // Validation
            if (txtCustomerName.getText().trim().isEmpty() ||
                cmbStaff.getSelectedItem() == null ||
                txtBarcode.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields");
                return;
            }

            // Stock check
            int stock = Integer.parseInt(txtStockQuantity.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());
            if (stock < quantity) {
                JOptionPane.showMessageDialog(this, "Insufficient stock! Available: " + stock);
                return;
            }

            // Update database
            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");

                // Update stock
                database.getCollection("Product")
                       .updateOne(new Document("barcode", txtBarcode.getText()),
                                new Document("$inc", new Document("stockQuantity", -quantity)));

                // Create sales record
                Document saleRecord = new Document()
                        .append("customerName", txtCustomerName.getText())
                        .append("staff", cmbStaff.getSelectedItem())
                        .append("barcode", txtBarcode.getText())
                        .append("productName", txtProductName.getText())
                        .append("quantity", quantity)
                        .append("totalPrice", Totalprice)
                        .append("finalPrice", Double.parseDouble(txtFinalPrice.getText()))
                        .append("gst", Double.parseDouble(lblGstAmount.getText()))
                        .append("makingCharges", Double.parseDouble(lblMakingCharges.getText()))
                        .append("timestamp", new Date());

                database.getCollection("Sales").insertOne(saleRecord);

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
            .append("<p><b>Staff:</b> ").append(cmbStaff.getSelectedItem()).append("</p>")
            .append("<h3>Product Details:</h3>");

        // Add product image
        String imageUrl = fetchCloudinaryImageUrl();
        if (imageUrl != null) {
            html.append("<p style='text-align:center;'><img src='").append(imageUrl).append("' width='100' height='100'/></p>");
        }

        html.append("<p><b>Product:</b> ").append(txtProductName.getText()).append("</p>")
            .append("<p><b>Barcode:</b> ").append(txtBarcode.getText()).append("</p>")
            .append("<p><b>Quantity:</b> ").append(txtQuantity.getText()).append("</p>")
            .append("<p><b>Total Price:</b> ₹").append(Totalprice).append("</p>")
            .append("<p><b>GST:</b> ₹").append(lblGstAmount.getText()).append("</p>")
            .append("<p><b>Making Charges:</b> ₹").append(lblMakingCharges.getText()).append("</p>")
            .append("<p><b>Final Amount:</b> ₹").append(lbltotalamount.getText()).append("</p>")
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

                // Define the font using FontFactory
                com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);

                // Add content
                pdfDoc.add(new Paragraph("ABC Jewelers - Invoice", titleFont));
                pdfDoc.add(new Paragraph("Date: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())));
                pdfDoc.add(new Paragraph("Customer: " + txtCustomerName.getText()));
                pdfDoc.add(new Paragraph("Staff: " + cmbStaff.getSelectedItem()));
                pdfDoc.add(new Paragraph("Product: " + txtProductName.getText()));
                pdfDoc.add(new Paragraph("Barcode: " + txtBarcode.getText()));
                pdfDoc.add(new Paragraph("Quantity: " + txtQuantity.getText()));
                pdfDoc.add(new Paragraph("Total Price: ₹" + Totalprice));
                pdfDoc.add(new Paragraph("GST: ₹" + lblGstAmount.getText()));
                pdfDoc.add(new Paragraph("Making Charges: ₹" + lblMakingCharges.getText()));
                pdfDoc.add(new Paragraph("Final Amount: ₹" + lbltotalamount.getText()));

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
        lblGstAmount.setText("0.00");
        lblMakingCharges.setText("0.00");
        lbltotalamount.setText("0.00");
        txtFinalPrice.setText("0.00");
    }


    private void clearFields() {
        txtCustomerName.setText("");
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerGram.setText("");
        txtStockQuantity.setText("");
        txtQuantity.setText("");
        txtTotalPrice.setText("");
        txtFinalPrice.setText("");
        txtCurrentGoldRate.setText("");
        lblGstAmount.setText("0.00");
        lblMakingCharges.setText("0.00");
        lbltotalamount.setText("0.00");
        cmbStaff.setSelectedIndex(0);
    }
}