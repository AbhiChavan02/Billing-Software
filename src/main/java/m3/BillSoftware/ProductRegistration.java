package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javax.swing.*;
import org.bson.Document;

public class ProductRegistration extends javax.swing.JFrame {

    // Declare components
    private JLabel lblBarcode, lblProductName, lblCategory, lblPricePerGram, lblStockQuantity;
    private JTextField txtBarcode, txtProductName, txtPricePerGram, txtStockQuantity;
    private JComboBox<String> cmbCategory;
    private JButton btnRegister, btnClear;
    private JPanel panelInput, panelActions;

    public ProductRegistration() {
        initComponents();
    }

    private void initComponents() {
        // Initialize components
        lblBarcode = new JLabel("Barcode:");
        lblProductName = new JLabel("Product Name:");
        lblCategory = new JLabel("Category:");
        lblPricePerGram = new JLabel("Price per Gram:");
        lblStockQuantity = new JLabel("Stock Quantity:");
        txtBarcode = new JTextField();
        txtProductName = new JTextField();
        txtPricePerGram = new JTextField();
        txtStockQuantity = new JTextField();
        cmbCategory = new JComboBox<>(new String[]{"Gold", "Silver", "Platinum"});
        btnRegister = new JButton("Register Product");
        btnClear = new JButton("Clear");
        panelInput = new JPanel();
        panelActions = new JPanel();

        // Frame settings
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Product Registration");
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Add components to input panel
        panelInput.setLayout(new java.awt.GridLayout(5, 2, 10, 10));
        panelInput.add(lblBarcode);
        panelInput.add(txtBarcode);
        panelInput.add(lblProductName);
        panelInput.add(txtProductName);
        panelInput.add(lblCategory);
        panelInput.add(cmbCategory);
        panelInput.add(lblPricePerGram);
        panelInput.add(txtPricePerGram);
        panelInput.add(lblStockQuantity);
        panelInput.add(txtStockQuantity);
        add(panelInput);

        // Add components to action panel
        panelActions.add(btnRegister);
        panelActions.add(btnClear);
        add(panelActions);

        // Button actions
        btnRegister.addActionListener(evt -> registerProduct());
        btnClear.addActionListener(evt -> clearFields());

        pack();
        setSize(400, 300);
        setLocationRelativeTo(null);
    }

    private void registerProduct() {
        try {
            String barcode = txtBarcode.getText();
            String productName = txtProductName.getText();
            String category = (String) cmbCategory.getSelectedItem();
            double pricePerGram = Double.parseDouble(txtPricePerGram.getText());
            int stockQuantity = Integer.parseInt(txtStockQuantity.getText());

            if (barcode.isEmpty() || productName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Barcode and Product Name are required.");
                return;
            }

            // MongoDB connection
            String uri = "mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/";
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Check if product already exists
            Document existingProduct = productCollection.find(new Document("barcode", barcode)).first();
            if (existingProduct != null) {
                JOptionPane.showMessageDialog(this, "A product with this barcode already exists.");
                mongoClient.close();
                return;
            }

            // Create product document
            Document product = new Document("barcode", barcode)
                    .append("productName", productName)
                    .append("category", category)
                    .append("pricePerGram", pricePerGram)
                    .append("stockQuantity", stockQuantity);

            // Insert into database
            productCollection.insertOne(product);

            JOptionPane.showMessageDialog(this, "Product registered successfully!");
            mongoClient.close();

            // Clear input fields
            clearFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input for price or quantity.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerGram.setText("");
        txtStockQuantity.setText("");
        cmbCategory.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductRegistration().setVisible(true));
    }
}