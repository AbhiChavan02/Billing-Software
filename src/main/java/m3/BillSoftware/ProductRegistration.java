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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.bson.Document;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class ProductRegistration extends JFrame {
    private static final String MONGODB_URI = "";
    private static final String DATABASE_NAME = "testDB";
    
    private JPanel menuPanel, contentPanel;
    private JSplitPane splitPane;
    private Color primaryColor = new Color(40, 58, 82);
    private Color secondaryColor = new Color(241, 242, 246);
    private String loggedInUsername;
    private String loggedInFirstName;
    private String loggedInLastName;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public ProductRegistration(String username, String firstName, String lastName) {
        this.loggedInUsername = username;
        this.loggedInFirstName = firstName;
        this.loggedInLastName = lastName;

        initializeUI();
        setupMenuPanel();
        setupContentPanel();
        setupEventHandlers();
    }

    private void initializeUI() {
        setTitle("Jewelry POS System - Logged in as: " + loggedInUsername);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void setupMenuPanel() {
        menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(primaryColor);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // User Info Panel
        JPanel userInfoPanel = createUserInfoPanel();
        gbc.gridy = 0;
        menuPanel.add(userInfoPanel, gbc);

        // Menu Buttons
        String[] buttonLabels = {
            "Product Registration", "Start Sale", "Registered Products",
            "Sales History", "Available Stock", "Sales Dashboard", "Logout"
        };
        
        for (int i = 0; i < buttonLabels.length; i++) {
            gbc.gridy = i + 1;
            menuPanel.add(createMenuButton(buttonLabels[i]), gbc);
        }
    }

    private JPanel createUserInfoPanel() {
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(primaryColor);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCompanyLogo = loadLogo("E:\\PWS\\PWS\\img\\RJJewel.jpg", 200, 100);
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

        return userInfoPanel;
    }

    private void setupContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(secondaryColor);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, contentPanel);
        splitPane.setDividerLocation(280);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(3);
        add(splitPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        Component[] components = menuPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                String text = button.getText();
                
                switch (text) {
                    case "Product Registration":
                        button.addActionListener(e -> openProductRegistration());
                        break;
                    case "Start Sale":
                        button.addActionListener(e -> openProcessSales());
                        break;
                    case "Registered Products":
                        button.addActionListener(e -> openRegisteredProducts());
                        break;
                    case "Sales History":
                        button.addActionListener(e -> openSalesHistory());
                        break;
                    case "Available Stock":
                        button.addActionListener(e -> openProductStats());
                        break;
                    case "Sales Dashboard":
                        button.addActionListener(e -> openSalesDashboard());
                        break;
                    case "Logout":
                        button.addActionListener(e -> {
                            dispose();
                            executorService.shutdown();
                            new AdminLoginRegister().setVisible(true);
                        });
                        break;
                }
            }
        }
    }

    private JLabel loadLogo(String path, int width, int height) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("Logo file not found: " + path);
                return null;
            }

            ImageIcon originalIcon = new ImageIcon(path);
            Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(scaledImage));
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            return null;
        }
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
    
    private void openSalesDashboard() {
        executorService.execute(() -> {
            SwingUtilities.invokeLater(() -> {
                contentPanel.removeAll();
                contentPanel.setLayout(new BorderLayout());
                
                DashboardGraph dashboard = new DashboardGraph();
                contentPanel.add(dashboard, BorderLayout.CENTER);
                
                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });
    }

    private void openProductRegistration() {
        executorService.execute(() -> {
            SwingUtilities.invokeLater(() -> {
                contentPanel.removeAll();
                contentPanel.setLayout(new GridBagLayout());
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.CENTER;

                ProductRegistrationPanel registrationPanel = 
                    new ProductRegistrationPanel(loggedInUsername, loggedInFirstName, loggedInLastName);

                contentPanel.add(registrationPanel, gbc);
                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });
    }
    
    private void openProductStats() {
        executorService.execute(() -> {
            try (MongoClient mongoClient = MongoClients.create(MONGODB_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> productCollection = database.getCollection("Product");
                MongoCollection<Document> salesCollection = database.getCollection("Sales");

                ProductStats stats = calculateProductStats(productCollection, salesCollection);
                
                SwingUtilities.invokeLater(() -> {
                    displayProductStats(stats);
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Error retrieving stats: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private ProductStats calculateProductStats(MongoCollection<Document> productCollection, 
                                             MongoCollection<Document> salesCollection) {
        ProductStats stats = new ProductStats();
        
        stats.totalProducts = productCollection.countDocuments();
        
        for (Document sale : salesCollection.find()) {
            stats.soldProducts += sale.getInteger("quantity", 0);
        }

        for (Document product : productCollection.find()) {
            int stock = product.getInteger("stockQuantity", 0);
            stats.totalQuantity += stock;

            String category = product.getString("category");
            if ("Emetation".equalsIgnoreCase(category)) {
                double salePrice = product.getDouble("salesPrice");
                stats.totalValue += salePrice * stock;
                stats.emetationCount++;
            } else if ("Gold".equalsIgnoreCase(category)) {
                double grams = product.getDouble("grams");
                stats.totalValue += grams * stock;
                stats.goldCount++;
            } else if ("Silver".equalsIgnoreCase(category)) {
                double grams = product.getDouble("grams");
                stats.totalValue += grams * stock;
                stats.silverCount++;
            }
        }
        
        return stats;
    }

    private void displayProductStats(ProductStats stats) {
        contentPanel.removeAll();
        
        JPanel statsPanel = new JPanel();
        statsPanel.setBackground(secondaryColor);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Product Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsPanel.add(titleLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        metricsPanel.setBackground(secondaryColor);

        metricsPanel.add(createMetricBox("Total Products", String.valueOf(stats.totalProducts), new Color(52, 152, 219)));
        metricsPanel.add(createMetricBox("Available Stock", String.valueOf(stats.totalQuantity), new Color(46, 204, 113)));
        metricsPanel.add(createMetricBox("Total Value", "₹" + String.format("%.2f", stats.totalValue), new Color(231, 76, 60)));
        metricsPanel.add(createMetricBox("Sold Products", String.valueOf(stats.soldProducts), new Color(241, 196, 15)));

        statsPanel.add(metricsPanel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel categoryCountsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        categoryCountsPanel.setBackground(secondaryColor);

        categoryCountsPanel.add(createPlainLabel("Emetation: " + stats.emetationCount, new Color(52, 152, 219)));
        categoryCountsPanel.add(createPlainLabel("Gold: " + stats.goldCount, new Color(241, 196, 15)));
        categoryCountsPanel.add(createPlainLabel("Silver: " + stats.silverCount, new Color(231, 76, 60)));

        statsPanel.add(categoryCountsPanel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> openProductStats());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(secondaryColor);
        buttonPanel.add(btnRefresh);
        statsPanel.add(buttonPanel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(secondaryColor);
        tabbedPane.setForeground(primaryColor);

        tabbedPane.addTab("All", createCategoryPanel("All"));
        tabbedPane.addTab("Emetation", createCategoryPanel("Emetation"));
        tabbedPane.addTab("Gold", createCategoryPanel("Gold"));
        tabbedPane.addTab("Silver", createCategoryPanel("Silver"));

        statsPanel.add(tabbedPane);
        contentPanel.add(statsPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createCategoryPanel(String category) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(secondaryColor);

        JPanel productCardPanel = new JPanel();
        productCardPanel.setBackground(secondaryColor);
        productCardPanel.setLayout(new GridLayout(0, 6, 10, 10));

        executorService.execute(() -> {
            try (MongoClient mongoClient = MongoClients.create(MONGODB_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> productCollection = database.getCollection("Product");

                for (Document product : productCollection.find()) {
                    String productCategory = product.getString("category");
                    if ("All".equals(category) || category.equals(productCategory)) {
                        JPanel productCard = createProductCard(product, productCategory);
                        SwingUtilities.invokeLater(() -> {
                            productCardPanel.add(productCard);
                        });
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    JScrollPane scrollPane = new JScrollPane(productCardPanel);
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    panel.add(scrollPane, BorderLayout.CENTER);
                    panel.revalidate();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return panel;
    }

    private JPanel createProductCard(Document product, String category) {
        JPanel productCard = new JPanel();
        productCard.setLayout(new BoxLayout(productCard, BoxLayout.Y_AXIS));
        productCard.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        productCard.setBackground(Color.WHITE);
        productCard.setPreferredSize(new Dimension(180, 220));

        ImageIcon productImage = loadProductImage(product.getString("productImagePath"));
        JLabel imageLabel = new JLabel(productImage);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        productCard.add(imageLabel);
        productCard.add(Box.createRigidArea(new Dimension(0, 5)));

        addProductDetail(productCard, "Name: " + product.getString("productName"));
        addProductDetail(productCard, "Category: " + category);

        if ("Emetation".equalsIgnoreCase(category)) {
            addProductDetail(productCard, "Purchase: ₹" + product.getDouble("purchasePrice"));
            addProductDetail(productCard, "Sale: ₹" + product.getDouble("salesPrice"));
        } else {
            addProductDetail(productCard, "Grams: " + product.getDouble("grams") + "g");
        }

        addProductDetail(productCard, "Stock: " + product.getInteger("stockQuantity"));
        addProductDetail(productCard, "Barcode: " + product.getString("barcodeNumber"));

        return productCard;
    }

    private JLabel createPlainLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(color);
        return label;
    }

    private JPanel createMetricBox(String title, String value, Color color) {
        JPanel metricBox = new JPanel(new GridBagLayout());
        metricBox.setBackground(color);
        metricBox.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        metricBox.setPreferredSize(new Dimension(200, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        metricBox.add(titleLabel, gbc);

        gbc.gridy = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(Color.WHITE);
        metricBox.add(valueLabel, gbc);

        return metricBox;
    }

    private void addProductDetail(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 3)));
    }

    private ImageIcon loadProductImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                BufferedImage image = ImageIO.read(new URL(imageUrl));
                return new ImageIcon(image.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            System.err.println("Error loading product image: " + e.getMessage());
        }
        return new ImageIcon(new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB));
    }

    private void openProcessSales() {
        executorService.execute(() -> {
            SwingUtilities.invokeLater(() -> {
                contentPanel.removeAll();
                contentPanel.setLayout(new GridBagLayout());
                
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.fill = GridBagConstraints.BOTH;

                ProcessSalesPanel processSalesPanel = new ProcessSalesPanel();
                contentPanel.add(processSalesPanel, gbc);

                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });
    }

    private void openRegisteredProducts() {
        executorService.execute(() -> {
            SwingUtilities.invokeLater(() -> {
                contentPanel.removeAll();
                contentPanel.setLayout(new BorderLayout());
                
                RegisteredProductsPanel registeredProductsPanel = new RegisteredProductsPanel();
                contentPanel.add(registeredProductsPanel, BorderLayout.CENTER);

                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });
    }

    private void openSalesHistory() {
        executorService.execute(() -> {
            SwingUtilities.invokeLater(() -> {
                contentPanel.removeAll();
                contentPanel.setLayout(new BorderLayout());
                
                SalesHistoryPanel salesHistoryPanel = new SalesHistoryPanel();
                contentPanel.add(salesHistoryPanel, BorderLayout.CENTER);

                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });
    }

    private static class ProductStats {
        long totalProducts = 0;
        int totalQuantity = 0;
        double totalValue = 0;
        int soldProducts = 0;
        int emetationCount = 0;
        int goldCount = 0;
        int silverCount = 0;
    }
}

class ProductRegistrationPanel extends JPanel {
    private static final String MONGODB_URI = "";
    private static final String DATABASE_NAME = "testDB";
    
    private JTextField txtProductName, txtStockQuantity, txtPurchasePrice, txtSalesPrice, txtGrams;
    private JComboBox<String> cmbCategory;
    private JPanel dynamicFieldsPanel;
    private String productImagePath = "";
    private String loggedInUsername;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;
    private Cloudinary cloudinary;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private String barcodeNumber;

    public ProductRegistrationPanel(String username, String firstName, String lastName) {
        this.loggedInUsername = username;
        initializeCloudinary();
        initializeForm();
    }

    private void initializeCloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "");
        config.put("api_key", "");
        config.put("api_secret", "");
        cloudinary = new Cloudinary(config);
    }

    private void initializeForm() {
        setLayout(new GridBagLayout());
        JPanel formPanel = createFormPanel();
        add(formPanel);
    }

    private JPanel createFormPanel() {
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

        JLabel headerLabel = new JLabel("Product Registration");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));

        txtProductName = createFormTextField(20);
        txtStockQuantity = createFormTextField(20);

        cmbCategory = new JComboBox<>(new String[]{"Emetation", "Gold", "Silver"});
        styleComboBox(cmbCategory);

        dynamicFieldsPanel = new JPanel(new CardLayout());
        dynamicFieldsPanel.add(createEmetationPanel(), "Emetation");
        dynamicFieldsPanel.add(createGoldSilverPanel(), "GoldSilver");

        cmbCategory.addItemListener(this::handleCategoryChange);

        JButton btnUploadImage = createActionButton("Upload Image", new Color(52, 152, 219));
        JButton btnGenerateBarcode = createActionButton("Generate Barcode", new Color(155, 89, 182));
        JButton btnRegister = createActionButton("Register Product", new Color(46, 204, 113));
        JButton btnClear = createActionButton("Clear", new Color(231, 76, 60));

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(headerLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        formPanel.add(createFormLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtProductName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(createFormLabel("Category:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cmbCategory, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(dynamicFieldsPanel, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(createFormLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtStockQuantity, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(createFormLabel("Product Image:"), gbc);
        gbc.gridx = 1;
        formPanel.add(btnUploadImage, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(createFormLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        formPanel.add(btnGenerateBarcode, gbc);

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

        btnRegister.addActionListener(e -> registerProduct());
        btnUploadImage.addActionListener(e -> uploadImage());
        btnGenerateBarcode.addActionListener(e -> generateAndShowBarcode());
        btnClear.addActionListener(e -> clearFields());

        return formPanel;
    }

    private void handleCategoryChange(ItemEvent e) {
        String category = (String) cmbCategory.getSelectedItem();
        CardLayout cl = (CardLayout) dynamicFieldsPanel.getLayout();
        if ("Emetation".equals(category)) {
            cl.show(dynamicFieldsPanel, "Emetation");
        } else {
            cl.show(dynamicFieldsPanel, "GoldSilver");
        }
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
        label.setForeground(new Color(80, 80, 80));
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

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
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
    
    
    
    
    

    private void generateAndShowBarcode() {
        String productName = txtProductName.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();
        String quantity = txtStockQuantity.getText().trim();
        
        if (productName.isEmpty() || quantity.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter product name and quantity first", 
                "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Generate a unique barcode number
            barcodeNumber = generateBarcodeNumber();
            
            // Create barcode data
            String barcodeData = barcodeNumber + "\n" + productName + "\n" + category;
            
            // Generate QR code with the barcode data
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(barcodeData, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Create a barcode-style label with the QR code and product info
            JPanel barcodePanel = createBarcodePanel(qrImage, productName, barcodeNumber);
            
            // Show the barcode in a dialog
            JDialog barcodeDialog = new JDialog();
            barcodeDialog.setTitle("Product Barcode");
            barcodeDialog.setModal(true);
            barcodeDialog.setSize(400, 500);
            barcodeDialog.setLayout(new BorderLayout());
            
            barcodeDialog.add(barcodePanel, BorderLayout.CENTER);
            
            // Add print button
            JButton btnPrint = new JButton("Print Barcode");
            btnPrint.addActionListener(e -> printBarcode(barcodePanel));
            barcodeDialog.add(btnPrint, BorderLayout.SOUTH);
            
            barcodeDialog.setLocationRelativeTo(this);
            barcodeDialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating barcode: " + e.getMessage(), 
                "Generation Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String generateBarcodeNumber() {
        // Generate a unique barcode number combining timestamp and random number
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        String timestamp = sdf.format(new Date());
        Random random = new Random();
        int randomNum = random.nextInt(9000) + 1000; // 4-digit random number
        return "RJ" + timestamp + randomNum;
    }

    private JPanel createBarcodePanel(BufferedImage qrImage, String productName, String barcodeNumber) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        
        // Company name
        JLabel companyLabel = new JLabel("RJ JEWELS");
        companyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        companyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Product name
        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // QR Code image
        JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Barcode number
        JLabel barcodeLabel = new JLabel(barcodeNumber);
        barcodeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        barcodeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add all components to panel
        panel.add(companyLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(nameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(qrLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(barcodeLabel);
        
        return panel;
    }

    private void printBarcode(JPanel barcodePanel) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Print Barcode");
        
        if (job.printDialog()) {
            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                    if (pageIndex > 0) {
                        return Printable.NO_SUCH_PAGE;
                    }
                    
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    
                    // Scale the panel to fit the page
                    double scale = Math.min(
                        pageFormat.getImageableWidth() / barcodePanel.getWidth(),
                        pageFormat.getImageableHeight() / barcodePanel.getHeight()
                    );
                    g2d.scale(scale, scale);
                    
                    barcodePanel.paint(g2d);
                    return Printable.PAGE_EXISTS;
                }
            });
            
            try {
                job.print();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error printing barcode: " + e.getMessage(), 
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void registerProduct() {
        if (!validateImageUpload()) return;
        if (barcodeNumber == null || barcodeNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please generate a barcode before registering the product!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProductRegistrationData data = collectProductData();
        if (data == null) return;

        executorService.execute(() -> {
            try (MongoClient mongoClient = MongoClients.create(MONGODB_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> productCollection = database.getCollection("Product");

                Document product = createProductDocument(data);
                productCollection.insertOne(product);

                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Product registered successfully with barcode: " + barcodeNumber, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, 
                        "Database error: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            }
        });
    }

    private boolean validateImageUpload() {
        if (productImagePath == null || productImagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please upload an image before registering the product!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private ProductRegistrationData collectProductData() {
        ProductRegistrationData data = new ProductRegistrationData();
        
        data.productName = txtProductName.getText().trim();
        data.category = (String) cmbCategory.getSelectedItem();
        data.quantityText = txtStockQuantity.getText().trim();
        data.barcodeNumber = barcodeNumber;

        if (data.productName.isEmpty() || data.quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "All fields are required!", 
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        try {
            data.stockQuantity = Integer.parseInt(data.quantityText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid numerical values!", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if ("Emetation".equals(data.category)) {
            try {
                data.purchasePrice = Double.parseDouble(txtPurchasePrice.getText().trim());
                data.salesPrice = Double.parseDouble(txtSalesPrice.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid price values!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else {
            try {
                data.grams = Double.parseDouble(txtGrams.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid grams value!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        
        return data;
    }

    private Document createProductDocument(ProductRegistrationData data) {
        return new Document()
                .append("productName", data.productName)
                .append("category", data.category)
                .append("purchasePrice", data.purchasePrice)
                .append("salesPrice", data.salesPrice)
                .append("grams", data.grams)
                .append("stockQuantity", data.stockQuantity)
                .append("productImagePath", productImagePath)
                .append("barcodeNumber", data.barcodeNumber)
                .append("createdAt", new java.util.Date());
    }
    
    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image Files", "jpg", "png", "jpeg"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            if (selectedFile == null || !selectedFile.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid file selected!", 
                    "File Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            executorService.execute(() -> {
                try {
                    Map<?, ?> uploadResult = cloudinary.uploader().upload(selectedFile, ObjectUtils.emptyMap());
                    productImagePath = (String) uploadResult.get("secure_url");
                    
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Image uploaded successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "Image upload failed: " + e.getMessage(), 
                            "Upload Error", JOptionPane.ERROR_MESSAGE);
                    });
                    e.printStackTrace();
                }
            });
        }
    }

    private void clearFields() {
        SwingUtilities.invokeLater(() -> {
            txtProductName.setText("");
            txtStockQuantity.setText("");
            cmbCategory.setSelectedIndex(0);
            txtPurchasePrice.setText("");
            txtSalesPrice.setText("");
            txtGrams.setText("");
            productImagePath = "";
            barcodeNumber = null;
        });
    }

    private static class ProductRegistrationData {
        String productName;
        String category;
        String quantityText;
        int stockQuantity;
        double purchasePrice;
        double salesPrice;
        double grams;
        String barcodeNumber;
    }
}