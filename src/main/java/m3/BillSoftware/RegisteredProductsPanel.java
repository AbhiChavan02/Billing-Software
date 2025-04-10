package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import javax.swing.border.LineBorder;

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
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;
    private MongoClient mongoClient;
    private MongoCollection<Document> productCollection;

    public RegisteredProductsPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        initializeMongoDB();
        createUI();
        loadProductData();
    }

    // Image renderer for displaying product images in the table
    private class ImageCellRenderer extends JLabel implements TableCellRenderer {
        private final int IMAGE_SIZE = 60;
        private final LineBorder border = new LineBorder(Color.LIGHT_GRAY, 1);

        public ImageCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setOpaque(true);
            setBorder(border);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) value;
                Image img = icon.getImage().getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(img));
            } else {
                setIcon(null);
            }
            
            // Handle selection colors
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            return this;
        }
    }

 // In ButtonCellRenderer class - ALWAYS SHOW BUTTONS
    private class ButtonCellRenderer extends JPanel implements TableCellRenderer {
        private JButton updateButton;
        private JButton deleteButton;
        private final LineBorder border = new LineBorder(Color.LIGHT_GRAY, 1);

        public ButtonCellRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);
            setBorder(border);
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
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
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
        
        productTable.setShowGrid(false); // Hide default grid
        productTable.setIntercellSpacing(new Dimension(0, 0)); // Remove spacing between cells

        // Column width settings
        productTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Barcode
        productTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Image
        productTable.getColumnModel().getColumn(8).setPreferredWidth(150); // Actions

        // Set custom renderer for Actions column
        productTable.getColumnModel().getColumn(8).setCellRenderer(new ButtonCellRenderer());
        productTable.getColumnModel().getColumn(8).setCellEditor(new ButtonCellEditor());

        // Set image renderer for column 2 (Image)
        productTable.getColumnModel().getColumn(2).setCellRenderer(new ImageCellRenderer());

        // Center-align all columns with borders
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            private final LineBorder border = new LineBorder(Color.LIGHT_GRAY, 1);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(border); // Add border to all cells
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // Handle specific formatting for numeric columns
                if (value == null) {
                    setText("N/A");
                } else if (value instanceof Double) {
                    switch (column) {
                        case 4: setText(String.format("₹%.2f", value)); break; // Purchase Price
                        case 5: setText(String.format("₹%.2f", value)); break; // Sales Price
                        case 6: setText(String.format("%.2f g", value)); break; // Grams
                    }
                } else if (value instanceof Integer) {
                    setText(value.toString()); // Stock Quantity
                }
                return this;
            }
        };

        // Apply the centerRenderer to all columns except image and actions
        for (int i = 0; i < productTable.getColumnCount(); i++) {
            if (i != 2 && i != 8) { // Skip image and actions columns
                productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // Initialize buttons and search components
        btnUpdate = createActionButton("Update", new Color(52, 152, 219)); // Blue
        btnRefresh = createActionButton("Refresh", new Color(46, 204, 113)); // Green
        btnDelete = createActionButton("Delete", new Color(231, 76, 60)); // Red
        btnSearch = createActionButton("Search", new Color(241, 196, 15)); // Yellow

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
    
    
    private void initializeMongoDB() {
        mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/");
        MongoDatabase database = mongoClient.getDatabase("testDB");
        productCollection = database.getCollection("Product");
    }

    // Load product data from MongoDB
    private void loadProductData() {
        try {
            tableModel.setRowCount(0);
            for (Document product : productCollection.find()) {
                String barcodeNumber = getStringFromDocument(product, "barcodeNumber");
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
                    barcodeNumber != null ? barcodeNumber : "N/A",
                    productName != null ? productName : "N/A",
                    productImage,
                    category != null ? category : "N/A",
                    purchasePrice,
                    salesPrice,
                    grams,
                    stockQuantity != null ? stockQuantity : 0,
                    ""
                });
            }

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (value == null) setText("N/A");
                    else if (value instanceof Double) {
                        switch (column) {
                            case 4: setText(String.format("₹%.2f", value)); break;
                            case 5: setText(String.format("₹%.2f", value)); break;
                            case 6: setText(String.format("%.2f g", value)); break;
                        }
                    }
                    return this;
                }
            };

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

        try {
            Document query = new Document();
            switch (criteria) {
                case "Barcode": query.append("barcodeNumber", searchValue); break;
                case "Product Name": query.append("productName", new Document("$regex", searchValue).append("$options", "i")); break;
                case "Category": query.append("category", new Document("$regex", searchValue).append("$options", "i")); break;
                case "Purchase Price": query.append("purchasePrice", Double.parseDouble(searchValue)); break;
                case "Sales Price": query.append("salesPrice", Double.parseDouble(searchValue)); break;
                case "Grams": query.append("grams", Double.parseDouble(searchValue)); break;
                case "Stock Quantity": query.append("stockQuantity", Integer.parseInt(searchValue)); break;
            }

            tableModel.setRowCount(0);
            for (Document product : productCollection.find(query)) {
                String barcodeNumber = getStringFromDocument(product, "barcodeNumber");
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
                        barcodeNumber != null ? barcodeNumber : "N/A",
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
            String barcodeNumber = (String) tableModel.getValueAt(rowIndex, 0); // Barcode
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
                // Handle Emetation category fields
                String purchaseStr = JOptionPane.showInputDialog(this, "Enter new purchase price:", purchasePrice);
                String salesStr = JOptionPane.showInputDialog(this, "Enter new sales price:", salesPrice);
                try {
                    newPurchasePrice = Double.parseDouble(purchaseStr);
                    newSalesPrice = Double.parseDouble(salesStr);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid input for purchase or sales price.");
                    return;
                }
            } else {
                // Handle other categories
                String gramsStr = JOptionPane.showInputDialog(this, "Enter new grams:", grams);
                try {
                    newGrams = Double.parseDouble(gramsStr);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid input for grams.");
                    return;
                }
            }

            // Stock quantity (common)
            String stockStr = JOptionPane.showInputDialog(this, "Enter new stock quantity:", stockQuantity);
            int newStock;
            try {
                newStock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid input for stock quantity.");
                return;
            }

            // Update the table model with the new values
            tableModel.setValueAt(newProductName, rowIndex, 1);
            tableModel.setValueAt(newCategory, rowIndex, 3);
            tableModel.setValueAt(newPurchasePrice, rowIndex, 4);
            tableModel.setValueAt(newSalesPrice, rowIndex, 5);
            tableModel.setValueAt(newGrams, rowIndex, 6);
            tableModel.setValueAt(newStock, rowIndex, 7);

            // Create the filter for finding the product in the database
            Document filter = new Document("barcodeNumber", barcodeNumber);

            // Create the update document based on the category
            Document updatedProduct = new Document()
                    .append("productName", newProductName)
                    .append("category", newCategory)
                    .append("stockQuantity", newStock);

            if ("Emetation".equalsIgnoreCase(newCategory)) {
                updatedProduct.append("purchasePrice", newPurchasePrice)
                              .append("salesPrice", newSalesPrice);
            } else {
                updatedProduct.append("grams", newGrams);
            }

            // Perform the update operation in the database
            productCollection.updateOne(filter, new Document("$set", updatedProduct));

            // Show confirmation message
            JOptionPane.showMessageDialog(this, "Product updated successfully!");

            // Reload the table with updated data from the database
            loadProductData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating product data: " + e.getMessage());

        }
    }

    // Delete product data
    private void deleteProductData(int rowIndex) {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Get the barcodeNumber of the selected product
            String barcodeNumber = (String) tableModel.getValueAt(rowIndex, 0);
            if (barcodeNumber == null || barcodeNumber.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error: Barcode is invalid.");
                return; // Exit if barcodeNumber is invalid
            }

            // Create the filter for finding the product in the database
            Document filter = new Document("barcodeNumber", barcodeNumber);

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
                JOptionPane.showMessageDialog(this, "No product found with the given barcodeNumber.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting product data: " + e.getMessage());
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