package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javax.swing.*;
import org.bson.Document;

public class ProductRegistration extends javax.swing.JFrame {

    // Declare components
    private JLabel lblBarcode, lblProductName, lblCategory, lblRatePerPiece, lblStockQuantity, lblProductImage;
    private JTextField txtBarcode, txtProductName, txtRatePerPiece, txtStockQuantity;
    private JComboBox<String> cmbCategory;
    private JButton btnRegister, btnClear, btnExit, btnUploadImage;
    private JPanel panelInput, panelActions;
    private MainFrame MainFrame;  // Reference to the MainFrame class

    private String productImagePath = "";  // To store the uploaded image path

    public ProductRegistration(MainFrame MainFrame) {
        this.MainFrame = MainFrame;  // Initialize the reference to the main frame
        initComponents();
    }

    private void initComponents() {
        // Initialize components
        lblBarcode = new JLabel("Barcode:");
        lblProductName = new JLabel("Product Name:");
        lblCategory = new JLabel("Category:");
        lblRatePerPiece = new JLabel("Rate per Piece:");
        lblStockQuantity = new JLabel("Stock Quantity:");
        lblProductImage = new JLabel("Product Image:");

        txtBarcode = new JTextField();
        txtProductName = new JTextField();
        txtRatePerPiece = new JTextField();
        txtStockQuantity = new JTextField();

        cmbCategory = new JComboBox<>(new String[]{"Gold", "Silver", "Platinum"});

        btnRegister = new JButton("Product Registration");
        btnClear = new JButton("Clear");
        btnExit = new JButton("Exit");
        btnUploadImage = new JButton("Upload Image");

        panelInput = new JPanel();
        panelActions = new JPanel();

        // Frame settings
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Product Registration");
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Add components to input panel
        panelInput.setLayout(new java.awt.GridLayout(6, 2, 10, 10));
        panelInput.add(lblBarcode);
        panelInput.add(txtBarcode);
        panelInput.add(lblProductName);
        panelInput.add(txtProductName);
        panelInput.add(lblCategory);
        panelInput.add(cmbCategory);
        panelInput.add(lblRatePerPiece);
        panelInput.add(txtRatePerPiece);
        panelInput.add(lblStockQuantity);
        panelInput.add(txtStockQuantity);
        panelInput.add(lblProductImage);
        panelInput.add(btnUploadImage);
        add(panelInput);

        // Add components to action panel
        panelActions.add(btnRegister);
        panelActions.add(btnClear);
        panelActions.add(btnExit);
        add(panelActions);

        // Button actions
        btnRegister.addActionListener(evt -> registerProduct());
        btnClear.addActionListener(evt -> clearFields());
        btnExit.addActionListener(evt -> exitApplication());
        btnUploadImage.addActionListener(evt -> uploadImage());

        pack();
        setSize(500, 500);
        setLocationRelativeTo(null);
    }

    private void registerProduct() {
        try {
            String barcode = txtBarcode.getText();
            String productName = txtProductName.getText();
            String category = (String) cmbCategory.getSelectedItem();
            double ratePerPiece = Double.parseDouble(txtRatePerPiece.getText());
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
                    .append("ratePerPiece", ratePerPiece)
                    .append("stockQuantity", stockQuantity)
                    .append("productImagePath", productImagePath);

            // Insert into database
            productCollection.insertOne(product);

            JOptionPane.showMessageDialog(this, "Product registered successfully!");
            mongoClient.close();

            // Clear input fields
            clearFields();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input for rate or quantity.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtBarcode.setText("");
        txtProductName.setText("");
        txtRatePerPiece.setText("");
        txtStockQuantity.setText("");
        cmbCategory.setSelectedIndex(0);
        productImagePath = "";
    }

    private void exitApplication() {
        int confirmExit = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
        if (confirmExit == JOptionPane.YES_OPTION) {
            this.setVisible(false);
            this.dispose();
            MainFrame.setVisible(true);  // Show the main frame when exiting
        }
    }

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            productImagePath = fileChooser.getSelectedFile().getAbsolutePath();
            JOptionPane.showMessageDialog(this, "Image selected: " + productImagePath);
        } else {
            JOptionPane.showMessageDialog(this, "Image selection canceled.");
        }
    }

    public static void main(String[] args) {
        // Create the main frame instance
        MainFrame MainFrame = new MainFrame();

        // Start the ProductRegistration window with a reference to the main frame
        java.awt.EventQueue.invokeLater(() -> {
            new ProductRegistration(MainFrame).setVisible(true);
            MainFrame.setVisible(false);  // Initially hide the main frame
        });
    }
}
