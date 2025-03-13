package m3.BillSoftware;

import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import com.itextpdf.text.Image; // For iText PDF image handling
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



public class ProcessSalesPanel extends JPanel {
	
    private JTextField txtBarcode, txtProductName, txtPricePerGram,txtCurrentGoldRate, txtQuantity, txtTotalPrice, txtName, txtFinalPrice;
    private JButton btnProcessSale, btnClear, btnRefresh;
    private Color backgroundColor = new Color(241, 242, 246); // Light gray background
    private Color formColor = Color.WHITE; // White for form background
    private Double Totalprice;
    private JLabel goldRateLabel, RateLabel, gstLabel, makingChargesLabel,totalLabel, lblGstAmount, lblMakingCharges,lbltotalamount;
    double grams = 0.0;
    private JTextField txtGstAmount, txtMakingCharges, txtTotalAmount;


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

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Header
        JLabel headerLabel = new JLabel("Start Sales");
        headerLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82)); // Dark blue

        // Form components
        txtName = createFormTextField(20);
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
        txtCurrentGoldRate = createFormTextField(20); // New field for current gold rate
        
        
        txtGstAmount.setEditable(false);
        txtMakingCharges.setEditable(false);
        txtTotalAmount.setEditable(false);
        
        
        // Labels for GST and Making Charges instead of input fields
        lblGstAmount = new JLabel("0.00");
        lblGstAmount.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

        lblMakingCharges = new JLabel("0.00");
        lblMakingCharges.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        
        lbltotalamount = new JLabel("0.00");
        lbltotalamount.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));


        // Add a DocumentListener to the txtQuantity field
        txtQuantity.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateTotalPrice();  // Recalculate total price when quantity is inserted
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateTotalPrice();  // Recalculate total price when quantity is removed or modified
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateTotalPrice();  // Recalculate total price if there is a change
            }
        });
        
     // Add a DocumentListener to the txtCurrentGoldRate field
        txtCurrentGoldRate.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateTotalPriceWithGoldRate();  // Recalculate total price when the gold rate is inserted
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateTotalPriceWithGoldRate();  // Recalculate total price when the gold rate is removed or modified
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateTotalPriceWithGoldRate();  // Recalculate total price if there is a change
            }
        });


        btnRefresh = createActionButton("Refresh", new Color(52, 152, 219)); // Blue
        btnProcessSale = createActionButton("Process Sale", new Color(46, 204, 113)); // Green
        btnClear = createActionButton("Clear", new Color(231, 76, 60)); // Red

        // Layout
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(headerLabel, gbc);

        // Name Field and Label
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        formPanel.add(createFormLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtName, gbc);

        // Barcode Field and Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createFormLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        formPanel.add(createInputPanel(txtBarcode, btnRefresh), gbc);

        // Product Name Field and Label
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(createFormLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductName, gbc);

     // Price Per Gram Field and Label
        gbc.gridx = 0;
        gbc.gridy = 4;

        // Create the initial label
        RateLabel = createFormLabel("Rate Per Piece:");

        // Add the initial label to the form panel
        formPanel.add(RateLabel, gbc);

        // Now set the gridx and gridy for the txtPricePerGram
        gbc.gridx = 1;
        formPanel.add(txtPricePerGram, gbc);

        // Quantity Field and Label
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(createFormLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtQuantity, gbc);

        // Total Price Field and Label
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(createFormLabel("Price:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;  // Allow the text field to expand and fill the available width
        formPanel.add(txtTotalPrice, gbc);
        
        
        
     // Current Gold Rate Field and Label (New field)
        goldRateLabel = createFormLabel("Current Gold Rate:"); // Label for current gold rate
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(goldRateLabel, gbc);  // Label for current gold rate

        gbc.gridx = 1;
        formPanel.add(txtCurrentGoldRate, gbc);  // TextField for current gold rate
        
     // GST Amount Field
        gbc.gridx = 0;
        gbc.gridy = 10;
        formPanel.add(createFormLabel("GST Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtGstAmount, gbc);  // Changed from lblGstAmount

        // Making Charges Field
        gbc.gridx = 0;
        gbc.gridy = 11;
        formPanel.add(createFormLabel("Making Charges:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtMakingCharges, gbc);  // Changed from lblMakingCharges

        // Total Amount Field
        gbc.gridx = 0;
        gbc.gridy = 13;
        formPanel.add(createFormLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtTotalAmount, gbc);  // Changed from lbltotalamount
        
     // Final Price Field and Label
        gbc.gridx = 0;
        gbc.gridy = 14;
        formPanel.add(createFormLabel("Final Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtFinalPrice, gbc);
        
        

        // Hide both the label and the text field
        goldRateLabel.setVisible(false);  // Hide the label
        txtCurrentGoldRate.setVisible(false);  // Hide the text field

        // Revalidate and repaint to adjust layout
        formPanel.revalidate();
        formPanel.repaint();

     // Button Panel for Process Sale, Clear
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
        

     // Add formPanel to JScrollPane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setPreferredSize(new Dimension(900, 700)); // Set a preferred size for the scrollable area
        add(scrollPane); // Add scrollPane to the JFrame instead of formPanel directly

        // Add action listeners
        btnRefresh.addActionListener(e -> fetchProductDetails());
        btnProcessSale.addActionListener(e -> processSale());
        btnClear.addActionListener(e -> clearFields());
    }
    
    
    private void updateTotalPriceWithGoldRate() {
        try {
            // Get the current gold rate entered by the user
            double currentGoldRate = Double.parseDouble(txtCurrentGoldRate.getText().trim());

            // Get the current total price (product total price) from txtTotalPrice
            double totalPrice = Double.parseDouble(txtTotalPrice.getText().trim());

            // Calculate the final price by multiplying the total price with the current gold rate
            double grams =  Double.parseDouble(txtPricePerGram.getText().trim());
            Totalprice = grams * currentGoldRate;

            // Display the final price in the txtFinalPrice field
            txtTotalPrice.setText(String.valueOf(Totalprice));
        } catch (NumberFormatException e) {
            // Handle invalid input for gold rate or total price
            txtFinalPrice.setText("Invalid input");
        }
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
        label.setFont(getSafeFont("Segoe UI", java.awt.Font.PLAIN, 14)); // Use safe font method
        label.setForeground(new Color(80, 80, 80)); // Dark gray
        return label;
    }

    private JTextField createFormTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(getSafeFont("Segoe UI", java.awt.Font.PLAIN, 14));  // Use safe font method
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return textField;
    }
    

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(getSafeFont("Segoe UI", Font.BOLD, 14)); // Use safe font method
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
            // Get the rate per piece (assuming this has already been set in txtPricePerGram)
            double ratePerPiece = Double.parseDouble(txtPricePerGram.getText());
            
            // Get the quantity from the txtQuantity field
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            // Calculate the total price (rate * quantity)
            double totalPrice = ratePerPiece * quantity;

            // Set the total price in the txtTotalPrice field
            txtTotalPrice.setText(String.valueOf(totalPrice));
            
        } catch (NumberFormatException e) {
            // If there's an error with parsing, handle it here (e.g., reset to 0)
            txtTotalPrice.setText("0.0");
        }
    }

    private java.awt.Font getSafeFont(String fontName, int style, int size) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();       
        return new java.awt.Font(java.awt.Font.SANS_SERIF, style, size); // Explicitly use java.awt.Font
    }





    private void fetchProductDetails() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            String barcode = txtBarcode.getText().trim();
            if (barcode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a barcode.");
                return;
            }

            Document product = productCollection.find(new Document("barcode", barcode)).first();
            if (product != null) {
                // Set product name
                txtProductName.setText(product.getString("productName"));

                // Get category
                String category = product.getString("category");

                // Default quantity set to 1
                int quantity = 1;
                txtQuantity.setText(String.valueOf(quantity));

                double ratePerPiece = getDoubleValue(product, "ratePerPiece");
                double salesPrice = getDoubleValue(product, "salesPrice");

                if ("Gold".equalsIgnoreCase(category) || "Silver".equalsIgnoreCase(category)) {
                    // Fetch grams value correctly
                    grams = getDoubleValue(product, "grams");

                    txtPricePerGram.setText(String.valueOf(grams)); // Display grams
                    txtCurrentGoldRate.setText(""); // Clear input for new gold rate

                    RateLabel.setText("Rate Per Gram");
                    txtCurrentGoldRate.setVisible(true);
                    goldRateLabel.setVisible(true);

                    // Add listener to update calculations dynamically
                    txtCurrentGoldRate.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            calculateGoldPrice(grams);
                        }
                    });

                } else if ("Emetation".equalsIgnoreCase(category)) {
                    txtPricePerGram.setText(String.valueOf(salesPrice));

                    double totalAmount = salesPrice * quantity;
                    txtTotalPrice.setText(String.valueOf(totalAmount));

                    RateLabel.setText("Rate Per Piece");
                    txtCurrentGoldRate.setVisible(false);
                    goldRateLabel.setVisible(false);

                    // Update GST, Making Charges, and Total Amount
                    updateGSTandFinalAmount(totalAmount, category);
                } else {
                    txtPricePerGram.setText(String.valueOf(ratePerPiece));

                    double totalAmount = ratePerPiece * quantity;
                    txtTotalPrice.setText(String.valueOf(totalAmount));

                    RateLabel.setText("Rate Per Piece");
                    txtCurrentGoldRate.setVisible(false);
                    goldRateLabel.setVisible(false);

                    // Update GST, Making Charges, and Total Amount
                    updateGSTandFinalAmount(totalAmount, category);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }

    // ✅ Function to calculate Gold Price dynamically
    private void calculateGoldPrice(double grams) {
        try {
            double goldRate = Double.parseDouble(txtCurrentGoldRate.getText().trim());
            double totalAmount = goldRate * grams;

            txtTotalPrice.setText(String.format("%.2f", totalAmount));

            // Update GST, Making Charges, and Total Amount
            updateGSTandFinalAmount(totalAmount, "Gold");
        } catch (NumberFormatException ex) {
            txtTotalPrice.setText("0.00");
            txtGstAmount.setText("0.00");
            txtMakingCharges.setText("0.00");
            txtTotalAmount.setText("0.00");
        }
    }
    
 // ✅ Function to calculate GST and Final Amount correctly
 // ====== 1. Update the updateGSTandFinalAmount Method ======
 // ✅ Updated GST rates (Emetation now 3%)
    private void updateGSTandFinalAmount(double totalAmount, String category) {
        // Calculate Making Charges
        double makingChargesRate = ("Gold".equalsIgnoreCase(category) || "Silver".equalsIgnoreCase(category)) ? 0.14 : 0.0;
        double makingCharges = totalAmount * makingChargesRate;

        // New total after adding making charges
        double newTotal = totalAmount + makingCharges;

        // Updated GST rates (Changed Emetation from 5% to 3%)
        double gstRate = ("Gold".equalsIgnoreCase(category) || "Silver".equalsIgnoreCase(category)) ? 0.03 :
                        ("Emetation".equalsIgnoreCase(category)) ? 0.03 : 0.18; // Changed here
        
        double gstAmount = newTotal * gstRate;

        // Final amount including GST
        double finalAmount = newTotal + gstAmount;

        // Update text fields with calculated values
        txtMakingCharges.setText(String.format("%.2f", makingCharges));
        txtGstAmount.setText(String.format("%.2f", gstAmount));
        txtTotalAmount.setText(String.format("%.2f", finalAmount));

        // Update Final Price field
        txtFinalPrice.setText(String.format("%.2f", finalAmount));
    }


    // ✅ Function to safely fetch numbers from MongoDB
    private double getDoubleValue(Document product, String key) {
        Object value = product.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value); // Convert string to double
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

                String barcode = txtBarcode.getText();
                Document product = productCollection.find(new Document("barcode", barcode)).first();
                if (product != null) {
                    int currentStock = product.getInteger("stockQuantity");
                    if (quantity > currentStock) {
                        JOptionPane.showMessageDialog(this, "Insufficient stock available!");
                        return;
                    }

                    int newStock = currentStock - quantity;
                    productCollection.updateOne(new Document("barcode", barcode),
                            new Document("$set", new Document("stockQuantity", newStock)));

                    Document sale = new Document("productName", txtProductName.getText())
                            .append("quantity", quantity)
                            .append("totalPrice", totalPrice)
                            .append("finalPrice", finalPrice)
                            .append("savings", savings)
                            .append("customerName", txtName.getText())
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
    
    private void displayImageInInvoice(String imageBase64) {
        try {
            // Decode Base64 string to byte array
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

            // Convert byte array to ImageIcon
            ImageIcon imageIcon = new ImageIcon(imageBytes);

            // Scale the image using java.awt.Image
            java.awt.Image scaledImage = imageIcon.getImage().getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH);
            ImageIcon scaledImageIcon = new ImageIcon(scaledImage);

            // Display in a JLabel
            JLabel imageLabel = new JLabel(scaledImageIcon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Create a dialog to show the image
            JDialog invoiceDialog = new JDialog();
            invoiceDialog.setLayout(new BorderLayout());
            invoiceDialog.add(imageLabel, BorderLayout.CENTER);
            invoiceDialog.pack();
            invoiceDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error displaying image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private String fetchImageFromMongoDB(String barcode) {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            Document product = productCollection.find(new Document("barcode", barcode)).first();
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
            String barcode = txtBarcode.getText().trim();
            if (barcode.isEmpty()) {
                return null;
            }

            Cloudinary cloudinary = CloudinaryConfig.getInstance();

            // Log the public_id being used
            System.out.println("Fetching image for barcode (public_id): " + barcode);

            // Try to fetch the image
            Map result = cloudinary.uploader().explicit(barcode, ObjectUtils.asMap("type", "upload"));

            // Log the result from Cloudinary
            System.out.println("Cloudinary response: " + result);

            if (result != null && result.containsKey("secure_url")) {
                String imageUrl = result.get("secure_url").toString();
                System.out.println("Image URL found: " + imageUrl);
                return imageUrl;
            } else {
                System.out.println("Image not found in Cloudinary for public_id: " + barcode);
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error fetching image from Cloudinary: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error fetching image from Cloudinary: " + e.getMessage());
            return null;
        }
    }


    // Function to check if an image URL is valid
    private boolean imageExists(String imageUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setRequestMethod("HEAD");
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }




    private void generateInvoice(double totalPrice, double finalPrice, double savings) {
        JDialog invoiceDialog = new JDialog();
        invoiceDialog.setTitle("Invoice");
        invoiceDialog.setSize(500, 700);
        invoiceDialog.setLayout(new BorderLayout());

        JTextPane invoiceContent = new JTextPane();
        invoiceContent.setContentType("text/html");
        invoiceContent.setEditable(false);

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='padding:20px;'>");
        html.append("<h1 style='text-align:center;'>RajLaxhmi Jewelers</h1>");
        html.append("<hr>");
        html.append("<p><b>Date:</b> ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())).append("</p>");
        html.append("<p><b>Customer Name:</b> ").append(txtName.getText()).append("</p>");
        html.append("<h3>Product Details:</h3>");

        // Fetch image from MongoDB
        String imageBase64 = fetchImageFromMongoDB(txtBarcode.getText().trim());
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            html.append("<p style='text-align:center;'><img src='data:image/png;base64,")
                .append(imageBase64)
                .append("' width='100' height='100'/></p>");
        }

        // Add product details
        html.append("<p><b>Product Name:</b> ").append(txtProductName.getText()).append("</p>");
        html.append("<p><b>Barcode:</b> ").append(txtBarcode.getText()).append("</p>");
        html.append("<p><b>Quantity:</b> ").append(txtQuantity.getText()).append("</p>");
        html.append("<p><b>Total Price:</b> ₹").append(totalPrice).append("</p>");
        html.append("<p><b>Final Price:</b> ₹").append(finalPrice).append("</p>");
        html.append("<p><b>Savings:</b> ₹").append(savings).append("</p>");
        html.append("<hr>");
        html.append("<p style='text-align:center;'>Thank you for your business!</p>");
        html.append("</body></html>");

        invoiceContent.setText(html.toString());

        // Add print and download buttons
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
            downloadInvoiceAsPDF(totalPrice, finalPrice, savings, fetchCloudinaryImageUrl());
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnDownloadPDF);

        invoiceDialog.add(new JScrollPane(invoiceContent), BorderLayout.CENTER);
        invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);
        invoiceDialog.setVisible(true);
    }

    private void downloadInvoiceAsPDF(double totalPrice, double finalPrice, double savings, String cloudinaryImageUrl) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Invoice as PDF");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                PdfWriter.getInstance(pdfDoc, fos);
                pdfDoc.open();

                // Define fonts
                BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
                Font titleFont = new Font(baseFont, 20, Font.BOLD, BaseColor.BLACK);
                Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
                Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);

                // Ensure text field values are not empty
                String customerName = txtName.getText().trim().isEmpty() ? "Not Provided" : txtName.getText().trim();
                String productName = txtProductName.getText().trim().isEmpty() ? "Not Provided" : txtProductName.getText().trim();
                String barcode = txtBarcode.getText().trim().isEmpty() ? "Not Provided" : txtBarcode.getText().trim();
                String quantity = txtQuantity.getText().trim().isEmpty() ? "Not Provided" : txtQuantity.getText().trim();

             

                // Add Invoice Title
                Paragraph title = new Paragraph("ABC Jewelers - Invoice", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                pdfDoc.add(title);
                pdfDoc.add(new Paragraph("--------------------------------------------------------------"));

                // Add Customer and Date
                pdfDoc.add(new Paragraph("Date: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())));
                pdfDoc.add(new Paragraph("Customer Name: " + customerName));
                pdfDoc.add(new Paragraph("\n"));

                // Fetch image from MongoDB
                String imageBase64 = fetchImageFromMongoDB(barcode); // Fetch image from MongoDB
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                    com.itextpdf.text.Image productImage = com.itextpdf.text.Image.getInstance(imageBytes); // Use iText Image
                    productImage.scaleToFit(100, 100);
                    productImage.setAlignment(Element.ALIGN_CENTER);
                    pdfDoc.add(productImage);
                }

                // Create Table for Product Details
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("Product Name", boldFont)));
                table.addCell(new PdfPCell(new Phrase(productName, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Barcode", boldFont)));
                table.addCell(new PdfPCell(new Phrase(barcode, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Quantity", boldFont)));
                table.addCell(new PdfPCell(new Phrase(quantity, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Total Price", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + Totalprice, normalFont)));

                table.addCell(new PdfPCell(new Phrase("Final Price", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + finalPrice, normalFont)));

                table.addCell(new PdfPCell(new Phrase("You Saved", boldFont)));
                table.addCell(new PdfPCell(new Phrase("₹" + savings, normalFont)));

                pdfDoc.add(table);

                // Add Thank You Message
                pdfDoc.add(new Paragraph("\n\nThank you for shopping with ABC Jewelers!", boldFont));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving PDF: " + e.getMessage());
            } finally {
                pdfDoc.close();
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
        txtFinalPrice.setText("");
        txtGstAmount.setText("");
        txtMakingCharges.setText("");
        txtTotalAmount.setText("");
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