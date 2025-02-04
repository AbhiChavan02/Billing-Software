package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;

public class ProcessSalesPanel extends JPanel {
    private JTextField txtBarcode, txtProductName, txtPricePerGram, txtQuantity, txtWeight, txtTotalPrice, txtStockQuantity, txtName;
    private JButton btnProcessSale, btnClear, btnRefresh;
    private Color backgroundColor = new Color(241, 242, 246); // Light gray background
    private Color formColor = Color.WHITE; // White for form background

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
        JLabel headerLabel = new JLabel("Process Sales");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82)); // Dark blue

        // Form components
        txtName = createFormTextField(20);
        txtBarcode = createFormTextField(20);
        txtProductName = createFormTextField(20);
        txtProductName.setEditable(false);
        txtPricePerGram = createFormTextField(20);
        txtPricePerGram.setEditable(false);
        txtStockQuantity = createFormTextField(20);
        txtStockQuantity.setEditable(false);
        txtQuantity = createFormTextField(20);
        txtWeight = createFormTextField(20);
        txtTotalPrice = createFormTextField(20);
        txtTotalPrice.setEditable(false);

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
        formPanel.add(createFormLabel("Rate Per Piece:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPricePerGram, gbc);

        // Stock Quantity Field and Label
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(createFormLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtStockQuantity, gbc);

        // Quantity Field and Label
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(createFormLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtQuantity, gbc);

        // Weight Field and Label
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(createFormLabel("Weight (grams):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtWeight, gbc);

        // Total Price Field and Label
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(createFormLabel("Total Price:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtTotalPrice, gbc);

        // Button Panel for Process Sale, Clear
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(formColor);
        buttonPanel.add(btnProcessSale);
        buttonPanel.add(btnClear);
        formPanel.add(buttonPanel, gbc);

        add(formPanel);

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
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(80, 80, 80)); // Dark gray
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
                txtProductName.setText(product.getString("productName"));

                // CORRECTED: Use "ratePerPiece" (lowercase) to match the MongoDB field
                Object ratePerPieceObj = product.get("ratePerPiece"); // <-- Fix here
                double ratePerPiece = 0.0;  // Default value
                if (ratePerPieceObj instanceof Number) {
                    ratePerPiece = ((Number) ratePerPieceObj).doubleValue();
                } else if (ratePerPieceObj != null) {
                    // Attempt to parse if it's stored as a string
                    try {
                        ratePerPiece = Double.parseDouble(ratePerPieceObj.toString());
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "ratePerPiece is invalid in the database!");
                    }
                }
                txtPricePerGram.setText(String.valueOf(ratePerPiece));

                // Set stock quantity safely
                txtStockQuantity.setText(String.valueOf(product.getInteger("stockQuantity", 0)));
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }


    private void processSale() {
        try {
            double pricePerGram = Double.parseDouble(txtPricePerGram.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());
            double weight = Double.parseDouble(txtWeight.getText());
            double totalPrice = pricePerGram * weight * quantity;
            txtTotalPrice.setText(String.valueOf(totalPrice));

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
                            .append("weight", weight)
                            .append("totalPrice", totalPrice)
                            .append("customerName", txtName.getText())
                            .append("timestamp", new java.util.Date());
                    salesCollection.insertOne(sale);

                    JOptionPane.showMessageDialog(this, "Sale processed successfully!");
                    clearFields();
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing sale: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerGram.setText("");
        txtStockQuantity.setText("");
        txtQuantity.setText("");
        txtWeight.setText("");
        txtTotalPrice.setText("");
        txtName.setText("");
    }
}