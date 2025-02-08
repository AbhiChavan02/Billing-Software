package m3.BillSoftware;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.io.File;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.HashMap;
import java.util.Map;

public class ProductRegistration extends JFrame {
    private JPanel menuPanel, contentPanel;
    private JSplitPane splitPane;
    private Color primaryColor = new Color(40, 58, 82);
    private Color secondaryColor = new Color(241, 242, 246);
    private String loggedInUsername;
    private String loggedInFirstName;
    private String loggedInLastName;

    public ProductRegistration(String username, String firstName, String lastName) {
        this.loggedInUsername = username;
        this.loggedInFirstName = firstName;
        this.loggedInLastName = lastName;
        
        setTitle("Jewelry POS System - Logged in as: " + loggedInUsername);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create Menu Panel (Left Side)
        menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(primaryColor);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Load company logo with proper scaling
        JLabel lblCompanyLogo = loadLogo("E:\\PWS\\PWS\\img\\RJJewel.jpg", 200, 100);
        
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(primaryColor);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        if (lblCompanyLogo != null) {
            userInfoPanel.add(lblCompanyLogo);
        }
        
        JLabel lblUserName = new JLabel(loggedInFirstName + " " + loggedInLastName);
        lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblUserName.setForeground(Color.WHITE);
        
        JLabel lblUserRole = new JLabel("(" + loggedInUsername + ")");
        lblUserRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUserRole.setForeground(new Color(200, 200, 200));

        userInfoPanel.add(lblUserName);
        userInfoPanel.add(lblUserRole);
        userInfoPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        gbc.gridy = 0;
        menuPanel.add(userInfoPanel, gbc);

        // Menu Buttons
        JButton btnRegisterProduct = createMenuButton("Product Registration");
        JButton btnProcessSales = createMenuButton("Start Sale");
        JButton btnRegisteredProduct = createMenuButton("Registered Products");
        JButton btnSalesHistory = createMenuButton("Sales History");
        JButton btnLogout = createMenuButton("Logout");

        gbc.gridy = 1; menuPanel.add(btnRegisterProduct, gbc);
        gbc.gridy = 2; menuPanel.add(btnProcessSales, gbc);
        gbc.gridy = 3; menuPanel.add(btnRegisteredProduct, gbc);
        gbc.gridy = 4; menuPanel.add(btnSalesHistory, gbc);
        gbc.gridy = 5; menuPanel.add(btnLogout, gbc);

        // Create Content Panel (Right Side)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(secondaryColor);

        // SplitPane to hold menu and content
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, contentPanel);
        splitPane.setDividerLocation(280);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(3);
        add(splitPane, BorderLayout.CENTER);

     // Button Actions
        btnRegisterProduct.addActionListener(e -> openProductRegistration());
        btnProcessSales.addActionListener(e -> openProcessSales());   // Missing Action Listener
        btnRegisteredProduct.addActionListener(e -> openRegisteredProducts()); // Missing Action Listener
        btnSalesHistory.addActionListener(e -> openSalesHistory());   // Missing Action Listener
        btnLogout.addActionListener(e -> {
            dispose();
            new AdminLoginRegister().setVisible(true);
        });

    }

    private JLabel loadLogo(String path, int width, int height) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Error: Image not found at " + path);
            return null;
        }

        ImageIcon originalIcon = new ImageIcon(path);
        System.out.println("Original Icon loaded: " + originalIcon);

        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        System.out.println("Scaled Image: " + scaledImage);

        return new JLabel(new ImageIcon(scaledImage));
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 102));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(67, 91, 124));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 73, 102));
            }
        });
        return btn;
    }

    private void openProductRegistration() {
        contentPanel.removeAll();
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        ProductRegistrationPanel registrationPanel = new ProductRegistrationPanel(loggedInUsername, loggedInFirstName, loggedInLastName);
        
        contentPanel.add(registrationPanel, gbc);

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void openProcessSales() {
        contentPanel.removeAll();
        
        // Use GridBagLayout to center the ProcessSalesPanel
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Center alignment
        gbc.fill = GridBagConstraints.BOTH; // Allow resizing

        ProcessSalesPanel processSalesPanel = new ProcessSalesPanel();
        contentPanel.add(processSalesPanel, gbc);

        contentPanel.revalidate();
        contentPanel.repaint();
    }


    private void openRegisteredProducts() {
        contentPanel.removeAll();
        
        // Use BorderLayout to make the panel take up the entire contentPanel
        contentPanel.setLayout(new BorderLayout());
        
        RegisteredProductsPanel registeredProductsPanel = new RegisteredProductsPanel();
        contentPanel.add(registeredProductsPanel, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void openSalesHistory() {
        contentPanel.removeAll();
        
        // Use BorderLayout to make the panel take up the entire contentPanel
        contentPanel.setLayout(new BorderLayout());
        
        SalesHistoryPanel salesHistoryPanel = new SalesHistoryPanel();
        contentPanel.add(salesHistoryPanel, BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }




//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new ProductRegistration(loggedInFirstName, loggedInFirstName, loggedInFirstName).setVisible(true));
//    }
}

class ProductRegistrationPanel extends JPanel {
    private JTextField txtBarcode, txtProductName, txtRatePerPiece, txtStockQuantity;
    private JComboBox<String> cmbCategory;
    private String productImagePath = "";
    private String loggedInUsername;
    private Color backgroundColor = new Color(241, 242, 246); // Light gray
    private Color formColor = Color.WHITE; // White
    private Cloudinary cloudinary;

    public ProductRegistrationPanel(String username, String firstName, String lastName) {
        // Update logged in user display
        JLabel lblLoggedInUser = new JLabel("Logged in as: " + firstName + " " + lastName + " (" + username + ")");
        lblLoggedInUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLoggedInUser.setForeground(new Color(100, 100, 100));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(formColor);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
     // Initialize Cloudinary (Replace with your credentials)
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dkcxniwte");
        config.put("api_key", "872993699858565");
        config.put("api_secret", "qWa0j2TzlDi7gITYZpaQbwkYKGg");
        cloudinary = new Cloudinary(config);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Header
        JLabel headerLabel = new JLabel("Product Registration");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82)); // Dark blue

        // User info
//        JLabel lblLoggedInUser1 = new JLabel("Logged in as: " + loggedInUsername);
//        lblLoggedInUser1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        lblLoggedInUser1.setForeground(new Color(100, 100, 100)); // Gray

        // Form components
        JLabel lblBarcode = createFormLabel("Barcode:");
        JLabel lblProductName = createFormLabel("Product Name:");
        JLabel lblCategory = createFormLabel("Category:");
        JLabel lblRatePerPiece = createFormLabel("Rate per Piece:");
        JLabel lblStockQuantity = createFormLabel("Stock Quantity:");
        JLabel lblProductImage = createFormLabel("Product Image:");

        txtBarcode = createFormTextField(20);
        txtProductName = createFormTextField(20);
        txtRatePerPiece = createFormTextField(20);
        txtStockQuantity = createFormTextField(20);

        cmbCategory = new JComboBox<>(new String[]{"Gold", "Silver", "Platinum"});
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCategory.setBackground(Color.WHITE);
        cmbCategory.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton btnUploadImage = createActionButton("Upload Image", new Color(52, 152, 219)); // Blue
        JButton btnRegister = createActionButton("Register Product", new Color(46, 204, 113)); // Green
        JButton btnClear = createActionButton("Clear", new Color(231, 76, 60)); // Red

        // Layout
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(headerLabel, gbc);

//        gbc.gridy = 1;
//        formPanel.add(lblLoggedInUser1, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        formPanel.add(lblBarcode, gbc);
        gbc.gridx = 1;
        formPanel.add(txtBarcode, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(lblProductName, gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(lblCategory, gbc);
        gbc.gridx = 1;
        formPanel.add(cmbCategory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(lblRatePerPiece, gbc);
        gbc.gridx = 1;
        formPanel.add(txtRatePerPiece, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(lblStockQuantity, gbc);
        gbc.gridx = 1;
        formPanel.add(txtStockQuantity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(lblProductImage, gbc);
        gbc.gridx = 1;
        formPanel.add(btnUploadImage, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(formColor);
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnClear);
        formPanel.add(buttonPanel, gbc);

        add(formPanel);

        btnRegister.addActionListener(e -> registerProduct());
        btnUploadImage.addActionListener(e -> uploadImage());
        btnClear.addActionListener(e -> clearFields());
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
                btn.setBackground(bgColor.darker()); // Hover effect
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor); // Reset color
            }
        });

        return btn;
    }

    private void registerProduct() {
        System.out.println("Register Product button clicked!"); // Debugging line

        // Step 1: Validate if the image was uploaded successfully
        if (productImagePath == null || productImagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please upload an image before registering the product!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Step 2: Proceed with registering product
        try {
            String barcode = txtBarcode.getText().trim();
            String productName = txtProductName.getText().trim();
            String category = (String) cmbCategory.getSelectedItem();
            String rateText = txtRatePerPiece.getText().trim();
            String quantityText = txtStockQuantity.getText().trim();

            // Validation
            if (barcode.isEmpty() || productName.isEmpty() || rateText.isEmpty() || quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double ratePerPiece;
            int stockQuantity;
            try {
                ratePerPiece = Double.parseDouble(rateText);
                stockQuantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid numerical values!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("Proceeding with product registration..."); // Debugging line

            // MongoDB connection
            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");
                MongoCollection<Document> productCollection = database.getCollection("Product");

                if (productCollection.find(new Document("barcode", barcode)).first() != null) {
                    JOptionPane.showMessageDialog(this, "Barcode already exists!", "Duplicate Entry", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Document product = new Document()
                    .append("barcode", barcode)
                    .append("productName", productName)
                    .append("category", category)
                    .append("ratePerPiece", ratePerPiece)
                    .append("stockQuantity", stockQuantity)
                    .append("productImagePath", productImagePath)  // ✅ Image uploaded before reaching here
                    .append("createdAt", new java.util.Date());

                productCollection.insertOne(product);

                JOptionPane.showMessageDialog(this, "Product registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearFields();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void uploadImage() {
        System.out.println("Upload Image button clicked!"); // Debugging line
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                if (selectedFile == null || !selectedFile.exists()) {
                    JOptionPane.showMessageDialog(this, "Invalid file selected!", "File Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                System.out.println("Uploading image: " + selectedFile.getAbsolutePath()); // Debugging line

                // Upload image to Cloudinary
                Map<?, ?> uploadResult = cloudinary.uploader().upload(selectedFile, ObjectUtils.emptyMap());

                // Get secure URL
                productImagePath = (String) uploadResult.get("secure_url");

//                if (productImagePath == null || productImagePath.isEmpty()) {
//                    JOptionPane.showMessageDialog(this, "Image upload failed!", "Upload Error", JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                JOptionPane.showMessageDialog(this, "Image uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
//                System.out.println("Uploaded Image URL: " + productImagePath); // Debugging line

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Image upload failed: " + e.getMessage(), "Upload Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        txtBarcode.setText("");
        txtProductName.setText("");
        txtRatePerPiece.setText("");
        txtStockQuantity.setText("");
        cmbCategory.setSelectedIndex(0);
        productImagePath = ""; // ✅ Reset the image path
    }


}