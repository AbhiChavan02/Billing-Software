package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessSalesStaff extends JFrame {
    private MainFrameStaff mainFrameStaff;
    private JLabel lblStaff, lblBarcode, lblProductName, lblPricePerGram, lblStockQuantity, lblQuantity, lblWeight, lblTotalPrice;
    private JComboBox<String> cmbStaff;
    private JTextField txtBarcode, txtProductName, txtPricePerGram, txtStockQuantity, txtQuantity, txtWeight, txtTotalPrice;
    private JButton btnProcess, btnClear, btnRefresh, btnExit;

    public ProcessSalesStaff(MainFrameStaff mainFrameStaff) {
        this.mainFrameStaff = (mainFrameStaff == null) ? new MainFrameStaff() : mainFrameStaff;
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initComponents();
        loadStaffDropdown();  // ✅ This will populate the dropdown when the UI starts
    }


    private void initComponents() {
        setTitle("Process Sales - Staff");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize components
        lblBarcode = new JLabel("Barcode:");
        txtBarcode = new JTextField(20);
        lblProductName = new JLabel("Product Name:");
        txtProductName = new JTextField(20);
        txtProductName.setEditable(false);
        lblPricePerGram = new JLabel("Price per Gram:");
        txtPricePerGram = new JTextField(20);
        txtPricePerGram.setEditable(false);
        lblStockQuantity = new JLabel("Stock Quantity:");
        txtStockQuantity = new JTextField(20);
        txtStockQuantity.setEditable(false);
        lblQuantity = new JLabel("Quantity:");
        txtQuantity = new JTextField(20);
        lblWeight = new JLabel("Weight (grams):");
        txtWeight = new JTextField(20);
        lblTotalPrice = new JLabel("Total Price:");
        txtTotalPrice = new JTextField(20);
        txtTotalPrice.setEditable(false);
        lblStaff = new JLabel("Staff:");
        cmbStaff = new JComboBox<>();

        btnProcess = new JButton("Process Sale");
        btnProcess.addActionListener(evt -> processSale());
        btnClear = new JButton("Clear");
        btnClear.addActionListener(evt -> clearFields());
        btnRefresh = new JButton("Fetch Product");
        btnRefresh.addActionListener(evt -> fetchProductDetails());
        btnExit = new JButton("Exit");
        btnExit.addActionListener(evt -> exitApplication());

        // Arrange components using GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(lblBarcode, gbc);
        gbc.gridx = 1;
        add(txtBarcode, gbc);
        gbc.gridx = 2;
        add(btnRefresh, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(lblProductName, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(txtProductName, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(lblPricePerGram, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(txtPricePerGram, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(lblStockQuantity, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(txtStockQuantity, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(lblQuantity, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(txtQuantity, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 5;
        add(lblWeight, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(txtWeight, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 6;
        add(lblTotalPrice, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(txtTotalPrice, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 7;
        add(lblStaff, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(cmbStaff, gbc);
        gbc.gridwidth = 1;

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnProcess);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnExit);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 3;
        add(buttonPanel, gbc);

        setLocationRelativeTo(null);
        pack();
    }

    private void fetchProductDetails() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            String barcode = txtBarcode.getText();
            if (barcode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a barcode.");
                return;
            }

            Document product = productCollection.find(new Document("barcode", barcode)).first();
            if (product != null) {
                txtProductName.setText(product.getString("productName"));
                txtPricePerGram.setText(String.valueOf(product.getDouble("pricePerGram")));
                txtStockQuantity.setText(String.valueOf(product.getInteger("stockQuantity")));
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }

    private void processSale() {
        try {
            // Ensure that staff is selected and barcode is entered
            String staff = (String) cmbStaff.getSelectedItem();
            if (staff == null || txtBarcode.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            // Parse input values
            double pricePerGram = Double.parseDouble(txtPricePerGram.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());
            double weight = Double.parseDouble(txtWeight.getText());
            double totalPrice = pricePerGram * weight * quantity; // Adjusted formula
            txtTotalPrice.setText(String.valueOf(totalPrice));

            // MongoDB operations for saving sale and updating stock
            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");
                MongoCollection<Document> salesCollection = database.getCollection("Sales");
                MongoCollection<Document> productCollection = database.getCollection("Product");

                String barcode = txtBarcode.getText();

                // Fetch product details
                Document product = productCollection.find(new Document("barcode", barcode)).first();
                if (product != null) {
                    int currentStock = product.getInteger("stockQuantity");

                    // Check if sufficient stock is available
                    if (quantity > currentStock) {
                        JOptionPane.showMessageDialog(this, "Insufficient stock available!");
                        return;
                    }

                    // Update stock quantity
                    int newStock = currentStock - quantity;
                    productCollection.updateOne(new Document("barcode", barcode),
                            new Document("$set", new Document("stockQuantity", newStock)));

                    // Save sale to sales collection, including staff info and timestamp
                    Document sale = new Document("productName", txtProductName.getText())
                            .append("quantity", quantity)
                            .append("weight", weight)
                            .append("totalPrice", totalPrice)
                            .append("staff", staff) // Added staff info
                            .append("timestamp", new java.util.Date()); // Timestamp

                    salesCollection.insertOne(sale);

                    JOptionPane.showMessageDialog(this, "Sale processed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found.");
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
        cmbStaff.setSelectedIndex(-1);
    }

    private void exitApplication() {
        this.dispose(); // Close the current window
        if (mainFrameStaff == null) {
            System.out.println("MainFrame is NULL, creating a new instance...");
            mainFrameStaff = new MainFrameStaff(); // Create a new MainFrame instance
        }
        mainFrameStaff.setVisible(true); // Show the MainFrame
    }

    private void loadStaffDropdown() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            System.out.println("✅ Connected to MongoDB successfully!");

            MongoDatabase database = mongoClient.getDatabase("testDB");
            System.out.println("Using database: " + database.getName());

            MongoCollection<Document> staffCollection = database.getCollection("Staff");
            System.out.println("Using collection: " + staffCollection.getNamespace());

            List<String> staffNames = new ArrayList<>();
            for (Document doc : staffCollection.find()) {
                System.out.println("📜 Found document: " + doc.toJson()); // Debugging line
                String firstName = doc.getString("firstname"); // Fetch "firstname" instead of "staffName"
                if (firstName != null && !firstName.isEmpty()) {
                    staffNames.add(firstName);
                }
            }

            if (!staffNames.isEmpty()) {
                cmbStaff.setModel(new DefaultComboBoxModel<>(staffNames.toArray(new String[0])));
                System.out.println("✅ Staff loaded successfully: " + staffNames);
            } else {
                System.out.println("⚠️ No staff found in the database.");
                JOptionPane.showMessageDialog(this, "No staff found in the database.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error connecting to MongoDB: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading staff: " + e.getMessage());
            e.printStackTrace();
        }
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrameStaff mainFrame = new MainFrameStaff(); // Create the MainFrame instance
            ProcessSalesStaff processSales = new ProcessSalesStaff(mainFrame); // Pass MainFrame to ProcessSales
            processSales.setVisible(true); // Show the ProcessSales window
        });
    }
}
