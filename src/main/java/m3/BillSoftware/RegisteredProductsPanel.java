package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Objects;

public class RegisteredProductsPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> searchCriteria;
    private JButton btnUpdate, btnRefresh, btnDelete, btnSearch;
    private Color backgroundColor = new Color(241, 242, 246); // Light gray background
    private Color formColor = Color.WHITE; // White for form background

    public RegisteredProductsPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        createUI();
        loadProductData();
    }

    // Image renderer for displaying product images in the table
    private class ImageCellRenderer extends JLabel implements TableCellRenderer {
        private final int IMAGE_SIZE = 60; // Set the size of the image (60x60 pixels)

        public ImageCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER); // Center-align the image
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) value;
                // Scale the image to a square shape with fixed width and height
                Image img = icon.getImage().getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(img)); // Set the resized image
            } else {
                setIcon(null); // In case the value is not an ImageIcon
            }
            return this;
        }
    }

 // In ButtonCellRenderer class - ALWAYS SHOW BUTTONS
    private class ButtonCellRenderer extends JPanel implements TableCellRenderer {
        private JButton updateButton;
        private JButton deleteButton;

        public ButtonCellRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);

            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");

            updateButton.setBackground(new Color(52, 152, 219));
            deleteButton.setBackground(new Color(231, 76, 60));
            updateButton.setForeground(Color.WHITE);
            deleteButton.setForeground(Color.WHITE);

            // Add buttons to the panel
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 5, 2, 5); // Add spacing between buttons
            add(updateButton, gbc);

            gbc.gridx = 1;
            add(deleteButton, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Always show buttons
            return this;
        }
    }

 // In ButtonCellEditor class - ALWAYS SHOW BUTTONS
    private class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton updateButton;
        private JButton deleteButton;
        private int currentRow;

        public ButtonCellEditor() {
            panel = new JPanel(new GridBagLayout());

            updateButton = new JButton("Update");
            deleteButton = new JButton("Delete");

            updateButton.setBackground(new Color(52, 152, 219));
            deleteButton.setBackground(new Color(231, 76, 60));
            updateButton.setForeground(Color.WHITE);
            deleteButton.setForeground(Color.WHITE);

            updateButton.addActionListener(this);
            deleteButton.addActionListener(this);

            // Add buttons to the panel
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 5, 2, 5); // Add spacing between buttons
            panel.add(updateButton, gbc);

            gbc.gridx = 1;
            panel.add(deleteButton, gbc);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return panel; // Always show buttons
        }

        @Override
        public Object getCellEditorValue() {
            return ""; // Return an empty string as the cell value
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == updateButton) {
                updateProductData(currentRow);
            } else if (e.getSource() == deleteButton) {
                deleteProductData(currentRow);
            }
            fireEditingStopped(); // Stop editing after button click
        }
    }

    // Initialize the UI components
    private void createUI() {
        // Initialize table model with columns
        tableModel = new DefaultTableModel(
                new Object[]{"Barcode", "Product Name", "Image", "Category", "Purchase Price", "Sales Price", "Grams", "Stock Quantity", "Actions"}, 0);

        productTable = new JTable(tableModel);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        productTable.setRowHeight(100); // Increase row height to display larger images

     // In createUI() - COLUMN WIDTH SETTINGS
        productTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Barcode
        productTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Image
        productTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Actions

     // In createUI() method - CORRECTED COLUMN INDEX FOR ACTIONS
        productTable.getColumnModel().getColumn(8).setCellRenderer(new ButtonCellRenderer()); // Index 8 for Actions column
        productTable.getColumnModel().getColumn(8).setCellEditor(new ButtonCellEditor());
        productTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Wider for buttons

        // Center-align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Set image renderer for column 2 (Image)
        productTable.getColumnModel().getColumn(2).setCellRenderer(new ImageCellRenderer());

        // Initialize buttons
        btnUpdate = createActionButton("Update", new Color(52, 152, 219)); // Blue
        btnRefresh = createActionButton("Refresh", new Color(46, 204, 113)); // Green
        btnDelete = createActionButton("Delete", new Color(231, 76, 60)); // Red
        btnSearch = createActionButton("Search", new Color(241, 196, 15)); // Yellow

        // Initialize search components
        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchCriteria = new JComboBox<>(new String[]{"Barcode", "Product Name", "Category", "Purchase Price", "Sales Price", "Grams", "Stock Quantity"});
        searchCriteria.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add action listeners for buttons
        btnUpdate.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                updateProductData(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to update.");
            }
        });

        btnRefresh.addActionListener(e -> loadProductData());

        btnDelete.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteProductData(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            }
        });

        btnSearch.addActionListener(e -> handleSearch());

        // Create a panel for search and buttons
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(formColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(searchCriteria);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        // Add components to the main panel
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(productTable), BorderLayout.CENTER);
    }

    // Helper method to create styled buttons
    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
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

    // Load product data from MongoDB
    private void loadProductData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            tableModel.setRowCount(0); // Clear existing rows

            for (Document product : productCollection.find()) {
                String barcode = getStringFromDocument(product, "barcode");
                String productName = getStringFromDocument(product, "productName");
                String category = getStringFromDocument(product, "category");

                // Fetch fields based on category
                Double purchasePrice = null;
                Double salesPrice = null;
                Double grams = null;

                if ("Emetation".equals(category)) {
                    purchasePrice = product.getDouble("purchasePrice");
                    salesPrice = product.getDouble("salesPrice");
                } else {
                    grams = product.getDouble("grams");
                }

                Integer stockQuantity = product.getInteger("stockQuantity");
                String imageUrl = product.getString("productImagePath");
                ImageIcon productImage = loadProductImage(imageUrl);

                tableModel.addRow(new Object[]{
                        barcode != null ? barcode : "N/A",
                        productName != null ? productName : "N/A",
                        productImage,
                        category != null ? category : "N/A",
                        purchasePrice,
                        salesPrice,
                        grams,
                        stockQuantity != null ? stockQuantity : 0,
                        ""  // Action column
                });
            }

            // Set custom renderers for numeric columns
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (value == null) {
                        setText("N/A");
                    } else if (value instanceof Double) {
                        switch (column) {
                            case 4: // Purchase Price
                            case 5: // Sales Price
                                setText(String.format("₹%.2f", value));
                                break;
                            case 6: // Grams
                                setText(String.format("%.2f g", value));
                                break;
                        }
                    }
                    return this;
                }
            };

            // Apply to columns 4-7
            for (int i = 4; i <= 7; i++) {
                productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading product data: " + e.getMessage());
        }
    }

 // In loadProductImage() - PROPER IMAGE HANDLING
    private ImageIcon loadProductImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                BufferedImage image = ImageIO.read(new URL(imageUrl));
                return new ImageIcon(image.getScaledInstance(60, 60, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            // Return blank image
            return new ImageIcon(new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB));
        }
        return new ImageIcon(); // Return empty if no URL
    }

    // Handle search functionality
    private void handleSearch() {
        String searchValue = txtSearch.getText().trim();
        String criteria = (String) Objects.requireNonNull(searchCriteria.getSelectedItem());

        if (searchValue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a value to search.");
            return;
        }

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            Document query = new Document();
            switch (criteria) {
                case "Barcode":
                    query.append("barcode", searchValue);
                    break;
                case "Product Name":
                    query.append("productName", new Document("$regex", searchValue).append("$options", "i"));
                    break;
                case "Category":
                    query.append("category", new Document("$regex", searchValue).append("$options", "i"));
                    break;
                case "Purchase Price":
                    try {
                        query.append("purchasePrice", Double.parseDouble(searchValue));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input for Purchase Price.");
                        return;
                    }
                    break;
                case "Sales Price":
                    try {
                        query.append("salesPrice", Double.parseDouble(searchValue));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input for Sales Price.");
                        return;
                    }
                    break;
                case "Grams":
                    try {
                        query.append("grams", Double.parseDouble(searchValue));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input for Grams.");
                        return;
                    }
                    break;
                case "Stock Quantity":
                    try {
                        query.append("stockQuantity", Integer.parseInt(searchValue));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input for Stock Quantity.");
                        return;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid search criteria.");
            }

            tableModel.setRowCount(0);
            for (Document product : productCollection.find(query)) {
                String barcode = getStringFromDocument(product, "barcode");
                String productName = getStringFromDocument(product, "productName");
                String category = getStringFromDocument(product, "category");

                Double purchasePrice = null;
                Double salesPrice = null;
                Double grams = null;

                if ("Emetation".equals(category)) {
                    purchasePrice = product.getDouble("purchasePrice");
                    salesPrice = product.getDouble("salesPrice");
                } else {
                    grams = product.getDouble("grams");
                }

                Integer stockQuantity = product.getInteger("stockQuantity");
                String imageUrl = product.getString("productImagePath");
                ImageIcon productImage = loadProductImage(imageUrl);

                tableModel.addRow(new Object[]{
                        barcode != null ? barcode : "N/A",
                        productName != null ? productName : "N/A",
                        productImage,
                        category != null ? category : "N/A",
                        purchasePrice,
                        salesPrice,
                        grams,
                        stockQuantity != null ? stockQuantity : 0,
                        ""  // Action column
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No products found matching the search criteria.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error performing search: " + e.getMessage());
        }
    }

    // Update product data
    private void updateProductData(int rowIndex) {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Get the data from the selected row
            String barcode = (String) tableModel.getValueAt(rowIndex, 0); // Barcode
            String productName = (String) tableModel.getValueAt(rowIndex, 1); // Product Name
            String category = (String) tableModel.getValueAt(rowIndex, 3); // Category
            Double purchasePrice = (Double) tableModel.getValueAt(rowIndex, 4); // Purchase Price
            Double salesPrice = (Double) tableModel.getValueAt(rowIndex, 5); // Sales Price
            Double grams = (Double) tableModel.getValueAt(rowIndex, 6); // Grams
            int stockQuantity = (Integer) tableModel.getValueAt(rowIndex, 7); // Stock Quantity

            // Ask the user to edit the product details
            String newProductName = JOptionPane.showInputDialog(this, "Enter new product name:", productName);
            if (newProductName == null || newProductName.isEmpty()) {
                newProductName = productName;
            }

            String newCategory = JOptionPane.showInputDialog(this, "Enter new category:", category);
            if (newCategory == null || newCategory.isEmpty()) {
                newCategory = category;
            }

            // Category-specific fields
            Double newPurchasePrice = null;
            Double newSalesPrice = null;
            Double newGrams = null;

            if ("Emetation".equalsIgnoreCase(newCategory)) {
                String purchaseStr = JOptionPane.showInputDialog(this, "Enter new purchase price:", purchasePrice);
                String salesStr = JOptionPane.showInputDialog(this, "Enter new sales price:", salesPrice);
                newPurchasePrice = Double.parseDouble(purchaseStr);
                newSalesPrice = Double.parseDouble(salesStr);
            } else {
                String gramsStr = JOptionPane.showInputDialog(this, "Enter new grams:", grams);
                newGrams = Double.parseDouble(gramsStr);
            }

            // Stock quantity (common)
            String stockStr = JOptionPane.showInputDialog(this, "Enter new stock quantity:", stockQuantity);
            int newStock = Integer.parseInt(stockStr);

            // Update the table model with the new values
            tableModel.setValueAt(newProductName, rowIndex, 1);
            tableModel.setValueAt(newCategory, rowIndex, 3);
            tableModel.setValueAt(newPurchasePrice, rowIndex, 4);
            tableModel.setValueAt(newSalesPrice, rowIndex, 5);
            tableModel.setValueAt(newGrams, rowIndex, 6);
            tableModel.setValueAt(newStock, rowIndex, 7);

            // Create the filter for finding the product in the database
            Document filter = new Document("barcode", barcode);
            Document updatedProduct = new Document()
                    .append("productName", newProductName)
                    .append("category", newCategory)
                    .append("purchasePrice", newPurchasePrice)
                    .append("salesPrice", newSalesPrice)
                    .append("grams", newGrams)
                    .append("stockQuantity", newStock);

            // Perform the update operation in the database
            productCollection.updateOne(filter, new Document("$set", updatedProduct));

            // Show confirmation message
            JOptionPane.showMessageDialog(this, "Product updated successfully!");

            // Reload the table with updated data from the database
            loadProductData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating product data: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Delete product data
    private void deleteProductData(int rowIndex) {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Get the barcode of the selected product
            String barcode = (String) tableModel.getValueAt(rowIndex, 0);
            if (barcode == null || barcode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Barcode is invalid.");
                return; // Exit if barcode is invalid
            }

            // Create the filter for finding the product in the database
            Document filter = new Document("barcode", barcode);

            // Perform the delete operation in the database
            DeleteResult result = productCollection.deleteOne(filter);

            // Check if the deletion was successful
            if (result.getDeletedCount() == 1) {
                // Remove the selected row from the table model
                tableModel.removeRow(rowIndex);
                // Show confirmation message
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            } else {
                // Show error message if no product was deleted
                JOptionPane.showMessageDialog(this, "No product found with the given barcode.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting product data: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Helper method to safely get a string from a MongoDB document
    private String getStringFromDocument(Document product, String key) {
        Object value = product.get(key);
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Integer) {
            return String.valueOf(value); // Convert Integer to String
        }
        return ""; // Return empty string if value is neither String nor Integer
    }
}