package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

    private void createUI() {
        // Initialize table model and set it to product table
        tableModel = new DefaultTableModel(new Object[]{"Barcode", "Product Name", "Category", "Rate Per Piece", "Stock Quantity"}, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25); // Set row height for better readability
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Initialize buttons
        btnUpdate = createActionButton("Update", new Color(52, 152, 219)); // Blue
        btnRefresh = createActionButton("Refresh", new Color(46, 204, 113)); // Green
        btnDelete = createActionButton("Delete", new Color(231, 76, 60)); // Red
        btnSearch = createActionButton("Search", new Color(241, 196, 15)); // Yellow

        // Initialize search components
        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchCriteria = new JComboBox<>(new String[]{"Barcode", "Product Name", "Category", "Rate Per Piece", "Stock Quantity"});
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
        searchPanel.add(btnUpdate);
        searchPanel.add(btnRefresh);
        searchPanel.add(btnDelete);

        // Add components to the main panel
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(productTable), BorderLayout.CENTER);
    }

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

    private void loadProductData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Clear the existing rows in the table model
            tableModel.setRowCount(0);

            // Fetch all product documents from MongoDB
            for (Document product : productCollection.find()) {
                // Safely retrieve fields with default values
                String barcode = product.getString("barcode");
                String productName = product.getString("productName");
                String category = product.getString("category");
                Number ratePerPiece = product.get("ratePerPiece", Number.class);
                Integer stockQuantity = product.getInteger("stockQuantity");

                // Add product data to the table model
                tableModel.addRow(new Object[]{
                        barcode != null ? barcode : "N/A",
                        productName != null ? productName : "N/A",
                        category != null ? category : "N/A",
                        ratePerPiece != null ? ratePerPiece.intValue() : 0,  // Default to 0 if null
                        stockQuantity != null ? stockQuantity : 0           // Default to 0 if null
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading product data: " + e.getMessage());
        }
    }


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
                case "Rate Per Piece":
                    try {
                        query.append("ratePerPiece", Double.parseDouble(searchValue)); // Ensure numeric input
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input for Rate Per Piece. Please enter a valid number.");
                        return;
                    }
                    break;
                case "Stock Quantity":
                    try {
                        query.append("stockQuantity", Integer.parseInt(searchValue)); // Ensure numeric input
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input for Stock Quantity. Please enter a valid number.");
                        return;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid search criteria.");
            }

            tableModel.setRowCount(0);
            for (Document product : productCollection.find(query)) {
                // Safely retrieve fields with default values
                String barcode = product.getString("barcode");
                String productName = product.getString("productName");
                String category = product.getString("category");
                Number ratePerPiece = product.get("ratePerPiece", Number.class);
                Integer stockQuantity = product.getInteger("stockQuantity");

                tableModel.addRow(new Object[]{
                        barcode != null ? barcode : "N/A",
                        productName != null ? productName : "N/A",
                        category != null ? category : "N/A",
                        ratePerPiece != null ? ratePerPiece.intValue() : 0,  // Default to 0 if null
                        stockQuantity != null ? stockQuantity : 0           // Default to 0 if null
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No products found matching the search criteria.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error performing search: " + e.getMessage());
        }
    }


    private void updateProductData(int rowIndex) {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Get the data from the selected row
            String barcode = (String) tableModel.getValueAt(rowIndex, 0);  // Barcode
            String productName = (String) tableModel.getValueAt(rowIndex, 1); // Product Name
            String category = (String) tableModel.getValueAt(rowIndex, 2); // Category
            int ratePerPiece = (Integer) tableModel.getValueAt(rowIndex, 3); // Rate Per Piece
            int stockQuantity = (Integer) tableModel.getValueAt(rowIndex, 4); // Stock Quantity

            // Ask the user to edit the product details
            String newProductName = JOptionPane.showInputDialog(this, "Enter new product name:", productName);
            if (newProductName == null || newProductName.isEmpty()) {
                newProductName = productName;
            }

            String newCategory = JOptionPane.showInputDialog(this, "Enter new category:", category);
            if (newCategory == null || newCategory.isEmpty()) {
                newCategory = category;
            }

            String newRatePerPieceStr = JOptionPane.showInputDialog(this, "Enter new rate per piece:", ratePerPiece);
            if (newRatePerPieceStr == null || newRatePerPieceStr.isEmpty()) {
                newRatePerPieceStr = String.valueOf(ratePerPiece);
            }

            String newStockQuantityStr = JOptionPane.showInputDialog(this, "Enter new stock quantity:", stockQuantity);
            if (newStockQuantityStr == null || newStockQuantityStr.isEmpty()) {
                newStockQuantityStr = String.valueOf(stockQuantity);
            }

            try {
                int newRatePerPiece = Integer.parseInt(newRatePerPieceStr);
                int newStockQuantity = Integer.parseInt(newStockQuantityStr);

                // Update the table model with the new values
                tableModel.setValueAt(newProductName, rowIndex, 1);
                tableModel.setValueAt(newCategory, rowIndex, 2);
                tableModel.setValueAt(newRatePerPiece, rowIndex, 3);
                tableModel.setValueAt(newStockQuantity, rowIndex, 4);

                // Create the filter for finding the product in the database
                Document filter = new Document("barcode", barcode);
                Document updatedProduct = new Document()
                        .append("productName", newProductName)
                        .append("category", newCategory)
                        .append("ratePerPiece", newRatePerPiece)  // Update the rate per piece
                        .append("stockQuantity", newStockQuantity);

                // Perform the update operation in the database
                productCollection.updateOne(filter, new Document("$set", updatedProduct));

                // Show confirmation message
                JOptionPane.showMessageDialog(this, "Product updated successfully!");

                // Reload the table with updated data from the database
                loadProductData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating product data: " + e.getMessage());
        }
    }

    private void deleteProductData(int rowIndex) {
        // Retrieve the product data from the selected table row
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Get the barcode of the selected product
            String barcode = (String) tableModel.getValueAt(rowIndex, 0);

            // Create the filter for finding the product in the database
            Document filter = new Document("barcode", barcode);

            // Perform the delete operation in the database
            productCollection.deleteOne(filter);

            // Remove the selected row from the table model
            tableModel.removeRow(rowIndex);

            // Show confirmation message
            JOptionPane.showMessageDialog(this, "Product deleted successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting product data: " + e.getMessage());
        }
    }
}