package m3.BillSoftware;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.bson.Document;

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
        JButton btnTotalStock = createMenuButton("Available Stock");
        JButton btnLogout = createMenuButton("Logout");

        gbc.gridy = 1; menuPanel.add(btnRegisterProduct, gbc);
        gbc.gridy = 2; menuPanel.add(btnProcessSales, gbc);
        gbc.gridy = 3; menuPanel.add(btnRegisteredProduct, gbc);
        gbc.gridy = 4; menuPanel.add(btnSalesHistory, gbc);
        gbc.gridy = 5; menuPanel.add(btnTotalStock, gbc);
        gbc.gridy = 6; menuPanel.add(btnLogout, gbc);

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
        btnProcessSales.addActionListener(e -> openProcessSales());
        btnRegisteredProduct.addActionListener(e -> openRegisteredProducts());
        btnSalesHistory.addActionListener(e -> openSalesHistory());
        btnTotalStock.addActionListener(e -> openProductStats());

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
    
    
    private void openProductStats() {
        contentPanel.removeAll();

        JPanel statsPanel = new JPanel();
        statsPanel.setBackground(new Color(241, 242, 246));
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));

        // Title Label
        JLabel titleLabel = new JLabel("Product Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(40, 58, 82));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsPanel.add(titleLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Metrics Panel (Total Products, Available Stocks, Total Value, Sold Products)
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        metricsPanel.setBackground(new Color(241, 242, 246));

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");
            MongoCollection<Document> salesCollection = database.getCollection("Sales");

            // Calculate total products and stock
            long totalProducts = productCollection.countDocuments();
            int totalQuantity = 0;
            double totalValue = 0;
            int soldProducts = 0;

            // Get total sales from sales collection
            for (Document sale : salesCollection.find()) {
                soldProducts += sale.getInteger("quantity", 0);
            }

            // Calculate available stock values
            for (Document product : productCollection.find()) {
                int stock = product.getInteger("stockQuantity", 0);
                totalQuantity += stock;

                String category = product.getString("category");
                if ("Emetation".equalsIgnoreCase(category)) {
                    double salePrice = product.getDouble("salesPrice");
                    totalValue += salePrice * stock;
                } else {
                    double grams = product.getDouble("grams");
                    // Assuming grams represents value for precious metals
                    totalValue += grams * stock;
                }
            }

            // Create metric boxes
            JPanel totalProductsBox = createMetricBox("Total Products", String.valueOf(totalProducts), new Color(52, 152, 219));
            JPanel availableStockBox = createMetricBox("Available Stock", String.valueOf(totalQuantity), new Color(46, 204, 113));
            JPanel totalValueBox = createMetricBox("Total Value", "₹" + String.format("%.2f", totalValue), new Color(231, 76, 60));
            JPanel soldProductsBox = createMetricBox("Sold Products", String.valueOf(soldProducts), new Color(241, 196, 15));

            metricsPanel.add(totalProductsBox);
            metricsPanel.add(availableStockBox);
            metricsPanel.add(totalValueBox);
            metricsPanel.add(soldProductsBox);

            statsPanel.add(metricsPanel);
            statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Add a button above the tabbed pane
            JButton btnRefresh = new JButton("Refresh");
            btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnRefresh.setBackground(new Color(52, 152, 219));
            btnRefresh.setForeground(Color.WHITE);
            btnRefresh.setFocusPainted(false);
            btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnRefresh.addActionListener(e -> openProductStats()); // Refresh the panel

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBackground(new Color(241, 242, 246));
            buttonPanel.add(btnRefresh);
            statsPanel.add(buttonPanel);
            statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // Create a tabbed pane for All, Emetation, Gold, and Silver
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
            tabbedPane.setBackground(new Color(241, 242, 246));
            tabbedPane.setForeground(new Color(40, 58, 82));

            // Panels for each category
            JPanel allPanel = createCategoryPanel("All"); // New panel for "All"
            JPanel emetationPanel = createCategoryPanel("Emetation");
            JPanel goldPanel = createCategoryPanel("Gold");
            JPanel silverPanel = createCategoryPanel("Silver");

            // Add tabs to the tabbed pane
            tabbedPane.addTab("All", allPanel);
            tabbedPane.addTab("Emetation", emetationPanel);
            tabbedPane.addTab("Gold", goldPanel);
            tabbedPane.addTab("Silver", silverPanel);

            statsPanel.add(tabbedPane);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving stats: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        contentPanel.add(statsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Helper method to create a panel for a specific category
    private JPanel createCategoryPanel(String category) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(241, 242, 246));

        // Product cards
        JPanel productCardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        productCardPanel.setBackground(new Color(241, 242, 246));

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Filter products by category
            for (Document product : productCollection.find()) {
                String productCategory = product.getString("category");

                // Show all products if "All" is selected
                if ("All".equals(category) || category.equals(productCategory)) {
                    JPanel productCard = new JPanel();
                    productCard.setLayout(new BoxLayout(productCard, BoxLayout.Y_AXIS));
                    productCard.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    productCard.setBackground(Color.WHITE);
                    productCard.setPreferredSize(new Dimension(180, 220));

                    // Product Image
                    ImageIcon productImage = loadProductImage(product.getString("productImagePath"));
                    JLabel imageLabel = new JLabel(productImage);
                    imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    productCard.add(imageLabel);
                    productCard.add(Box.createRigidArea(new Dimension(0, 5)));

                    // Product Details
                    addProductDetail(productCard, "Name: " + product.getString("productName"));
                    addProductDetail(productCard, "Category: " + productCategory);

                    if ("Emetation".equalsIgnoreCase(productCategory)) {
                        addProductDetail(productCard, "Purchase: ₹" + product.getDouble("purchasePrice"));
                        addProductDetail(productCard, "Sale: ₹" + product.getDouble("salesPrice"));
                    } else {
                        addProductDetail(productCard, "Grams: " + product.getDouble("grams") + "g");
                    }

                    addProductDetail(productCard, "Stock: " + product.getInteger("stockQuantity"));

                    productCardPanel.add(productCard);
                }
            }

            JScrollPane scrollPane = new JScrollPane(productCardPanel);
            scrollPane.setPreferredSize(new Dimension(contentPanel.getWidth(), 400));
            panel.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving stats: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        return panel;
    }

    // Helper method to create a metric box
    private JPanel createMetricBox(String title, String value, Color color) {
        JPanel metricBox = new JPanel();
        metricBox.setBackground(color);
        metricBox.setLayout(new GridBagLayout());
        metricBox.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        metricBox.setPreferredSize(new Dimension(200, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title Label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        metricBox.add(titleLabel, gbc);

        // Value Label
        gbc.gridy = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(Color.WHITE);
        metricBox.add(valueLabel, gbc);

        return metricBox;
    }

    // Helper method to add product details
    private void addProductDetail(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
    }

    // Updated loadProductImage method
    private ImageIcon loadProductImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = ImageIO.read(new URL(imageUrl));
                return new ImageIcon(image.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }
        return new ImageIcon(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB)); // Blank image
    }


    // New function to load images from Cloudinary URL
    private ImageIcon loadImageFromUrl(String imageUrl) {
        try {
            // Fetch the image from the URL
            URL url = new URL(imageUrl);
            BufferedImage image = ImageIO.read(url);

            // Resize the image to fit the product card
            if (image != null) {
                Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading image from URL: " + imageUrl);
        }

        // Return a placeholder if the image fails to load
        return new ImageIcon("path/to/placeholder.png"); // Add a placeholder image in your project
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
    private JTextField txtBarcode, txtProductName, txtStockQuantity, txtPurchasePrice, txtSalesPrice, txtGrams;
    private JComboBox<String> cmbCategory;
    private JPanel dynamicFieldsPanel;
    private String productImagePath = "";
    private String loggedInUsername;
    private Color backgroundColor = new Color(241, 242, 246); // Light gray
    private Color formColor = Color.WHITE; // White
    private Cloudinary cloudinary;

    public ProductRegistrationPanel(String username, String firstName, String lastName) {
        this.loggedInUsername = username;

        // Initialize Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dkcxniwte");
        config.put("api_key", "872993699858565");
        config.put("api_secret", "qWa0j2TzlDi7gITYZpaQbwkYKGg");
        cloudinary = new Cloudinary(config);

        // Form Panel
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
        JLabel headerLabel = new JLabel("Product Registration");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82)); // Dark blue

        // Form components
        JLabel lblBarcode = createFormLabel("Barcode:");
        JLabel lblProductName = createFormLabel("Product Name:");
        JLabel lblCategory = createFormLabel("Category:");
        JLabel lblStockQuantity = createFormLabel("Stock Quantity:");
        JLabel lblProductImage = createFormLabel("Product Image:");

        txtBarcode = createFormTextField(20);
        txtProductName = createFormTextField(20);
        txtStockQuantity = createFormTextField(20);
        txtBarcode = createFormTextField(20);
        txtBarcode.setEditable(false); // Make barcode field read-only
        
        generateNextBarcode();

        cmbCategory = new JComboBox<>(new String[]{"Emetation", "Gold", "Silver"});
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCategory.setBackground(Color.WHITE);
        cmbCategory.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Initialize dynamic fields panels
        dynamicFieldsPanel = new JPanel(new CardLayout());
        JPanel emetationPanel = createEmetationPanel();
        JPanel goldSilverPanel = createGoldSilverPanel();

        dynamicFieldsPanel.add(emetationPanel, "Emetation");
        dynamicFieldsPanel.add(goldSilverPanel, "GoldSilver");

        // Add category change listener
        cmbCategory.addItemListener(e -> {
            String category = (String) cmbCategory.getSelectedItem();
            CardLayout cl = (CardLayout) dynamicFieldsPanel.getLayout();
            if ("Emetation".equals(category)) {
                cl.show(dynamicFieldsPanel, "Emetation");
            } else {
                cl.show(dynamicFieldsPanel, "GoldSilver");
            }
        });

        // Buttons
        JButton btnUploadImage = createActionButton("Upload Image", new Color(52, 152, 219)); // Blue
        JButton btnRegister = createActionButton("Register Product", new Color(46, 204, 113)); // Green
        JButton btnClear = createActionButton("Clear", new Color(231, 76, 60)); // Red

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(headerLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        formPanel.add(lblBarcode, gbc);
        gbc.gridx = 1;
        formPanel.add(txtBarcode, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(lblProductName, gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(lblCategory, gbc);
        gbc.gridx = 1;
        formPanel.add(cmbCategory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formPanel.add(dynamicFieldsPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(lblStockQuantity, gbc);
        gbc.gridx = 1;
        formPanel.add(txtStockQuantity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(lblProductImage, gbc);
        gbc.gridx = 1;
        formPanel.add(btnUploadImage, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(formColor);
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnClear);
        formPanel.add(buttonPanel, gbc);

        add(formPanel);

        // Button Actions
        btnRegister.addActionListener(e -> registerProduct());
        btnUploadImage.addActionListener(e -> uploadImage());
        btnClear.addActionListener(e -> clearFields());
    }

    private JPanel createEmetationPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBackground(formColor);

        txtPurchasePrice = createFormTextField(10);
        txtSalesPrice = createFormTextField(10);

        panel.add(createFormLabel("Purchase Price:"));
        panel.add(txtPurchasePrice);
        panel.add(createFormLabel("Sales Price:"));
        panel.add(txtSalesPrice);

        return panel;
    }
    
    
    private void generateNextBarcode() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            int maxBarcode = 0;
            // Iterate through all products to find the maximum barcode
            for (Document product : productCollection.find()) {
                String barcodeStr = product.getString("barcode");
                try {
                    int barcode = Integer.parseInt(barcodeStr);
                    if (barcode > maxBarcode) {
                        maxBarcode = barcode;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid barcode: " + barcodeStr);
                }
            }
            int nextBarcode = maxBarcode + 1;
            txtBarcode.setText(String.valueOf(nextBarcode));
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to 1 if there's an error
            txtBarcode.setText("1");
        }
    }

    private JPanel createGoldSilverPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(formColor);

        txtGrams = createFormTextField(10);
        panel.add(createFormLabel("Grams:"));
        panel.add(txtGrams);

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
            String quantityText = txtStockQuantity.getText().trim();
            double purchasePrice = 0, salesPrice = 0, grams = 0;

            // Validation
            if (barcode.isEmpty() || productName.isEmpty()  || quantityText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int stockQuantity;
            try {
                stockQuantity = Integer.parseInt(quantityText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid numerical values!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("Emetation".equals(category)) {
                try {
                    purchasePrice = Double.parseDouble(txtPurchasePrice.getText().trim());
                    salesPrice = Double.parseDouble(txtSalesPrice.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid price values!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                try {
                    grams = Double.parseDouble(txtGrams.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid grams value!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

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
                        .append("purchasePrice", purchasePrice)
                        .append("salesPrice", salesPrice)
                        .append("grams", grams)
                        .append("stockQuantity", stockQuantity)
                        .append("productImagePath", productImagePath)
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
        txtStockQuantity.setText("");
        cmbCategory.setSelectedIndex(0);
        txtPurchasePrice.setText("");
        txtSalesPrice.setText("");
        txtGrams.setText("");
        productImagePath = "";
        generateNextBarcode(); // Generate new barcode after clear
        
    }


}