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

public class RegisteredProducts {

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> searchCriteria;
    private JButton btnUpdate, btnRefresh, btnDelete, btnExit, btnSearch;

    public RegisteredProducts() {
        // Initialize table model and set it to product table
        tableModel = new DefaultTableModel(new Object[]{"Barcode", "Product Name", "Category", "Price Per Gram", "Stock Quantity"}, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25); // Set row height for better readability

        // Initialize buttons
        btnUpdate = new JButton("Update");
        btnRefresh = new JButton("Refresh");
        btnDelete = new JButton("Delete");
        btnExit = new JButton("Exit");
        btnSearch = new JButton("Search");

        // Initialize search components
        txtSearch = new JTextField(15); // Text field for search input
        searchCriteria = new JComboBox<>(new String[]{"Barcode", "Product Name", "Category", "Price Per Gram", "Stock Quantity"}); // Dropdown for search criteria

        // Add action listeners for buttons
        btnUpdate.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                updateProductData(selectedRow);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a product to update.");
            }
        });

        btnRefresh.addActionListener(e -> loadProductData());

        btnDelete.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteProductData(selectedRow);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a product to delete.");
            }
        });

        btnExit.addActionListener(e -> exitApplication());

        btnSearch.addActionListener(e -> handleSearch());

        // Load product data into the table
        loadProductData();

        // Create a panel for search and buttons
        JPanel panel = new JPanel();
        panel.add(new JLabel("Search:"));
        panel.add(txtSearch);
        panel.add(searchCriteria);
        panel.add(btnSearch);
        panel.add(btnUpdate);
        panel.add(btnRefresh);
        panel.add(btnDelete);
        panel.add(btnExit);

        // Create and configure the main frame
        JFrame frame = new JFrame("Product Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(productTable), BorderLayout.CENTER); // Add table to the center
        frame.add(panel, BorderLayout.SOUTH); // Add panel to the bottom
        frame.pack(); // Pack components
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true); // Make the frame visible
    }

    private void loadProductData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Clear the existing rows in the table model
            tableModel.setRowCount(0);

            // Fetch all product documents from MongoDB
            for (Document product : productCollection.find()) {
                // Add product data to the table model
                tableModel.addRow(new Object[]{
                        product.getString("barcode"),
                        product.getString("productName"),
                        product.getString("category"),
                        product.getDouble("pricePerGram"),
                        product.getInteger("stockQuantity")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading product data: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String searchValue = txtSearch.getText().trim(); // Get user input
        String criteria = (String) Objects.requireNonNull(searchCriteria.getSelectedItem()); // Get selected criteria

        if (searchValue.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a value to search.");
            return;
        }

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Create the query filter based on selected criteria
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
                case "Price Per Gram":
                    query.append("pricePerGram", Double.parseDouble(searchValue));
                    break;
                case "Stock Quantity":
                    query.append("stockQuantity", Integer.parseInt(searchValue));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid search criteria.");
            }

            // Clear the table and display the search results
            tableModel.setRowCount(0);
            for (Document product : productCollection.find(query)) {
                tableModel.addRow(new Object[]{
                        product.getString("barcode"),
                        product.getString("productName"),
                        product.getString("category"),
                        product.getDouble("pricePerGram"),
                        product.getInteger("stockQuantity")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "No products found matching the search criteria.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error performing search: " + e.getMessage());
        }
    }

    private void updateProductData(int rowIndex) {
        // Retrieve the product data from the selected table row
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            // Get the data from the selected row
            String barcode = (String) tableModel.getValueAt(rowIndex, 0);  // Barcode
            String productName = (String) tableModel.getValueAt(rowIndex, 1); // Product Name
            String category = (String) tableModel.getValueAt(rowIndex, 2); // Category
            double pricePerGram = (Double) tableModel.getValueAt(rowIndex, 3); // Price per Gram
            int stockQuantity = (Integer) tableModel.getValueAt(rowIndex, 4); // Stock Quantity

            // Ask the user to edit the product details
            String newProductName = JOptionPane.showInputDialog(null, "Enter new product name:", productName);
            if (newProductName == null || newProductName.isEmpty()) {
                newProductName = productName;
            }

            String newCategory = JOptionPane.showInputDialog(null, "Enter new category:", category);
            if (newCategory == null || newCategory.isEmpty()) {
                newCategory = category;
            }

            String newPricePerGramStr = JOptionPane.showInputDialog(null, "Enter new price per gram:", pricePerGram);
            if (newPricePerGramStr == null || newPricePerGramStr.isEmpty()) {
                newPricePerGramStr = String.valueOf(pricePerGram);
            }

            String newStockQuantityStr = JOptionPane.showInputDialog(null, "Enter new stock quantity:", stockQuantity);
            if (newStockQuantityStr == null || newStockQuantityStr.isEmpty()) {
                newStockQuantityStr = String.valueOf(stockQuantity);
            }

            try {
                double newPricePerGram = Double.parseDouble(newPricePerGramStr);
                int newStockQuantity = Integer.parseInt(newStockQuantityStr);

                // Update the table model with the new values
                tableModel.setValueAt(newProductName, rowIndex, 1);
                tableModel.setValueAt(newCategory, rowIndex, 2);
                tableModel.setValueAt(newPricePerGram, rowIndex, 3);
                tableModel.setValueAt(newStockQuantity, rowIndex, 4);

                // Create the filter for finding the product in the database
                Document filter = new Document("barcode", barcode);
                Document updatedProduct = new Document()
                        .append("productName", newProductName)
                        .append("category", newCategory)
                        .append("pricePerGram", newPricePerGram)
                        .append("stockQuantity", newStockQuantity);  // Update the stock quantity

                // Perform the update operation in the database
                productCollection.updateOne(filter, new Document("$set", updatedProduct));

                // Show confirmation message
                JOptionPane.showMessageDialog(null, "Product updated successfully!");

                // Reload the table with updated data from the database
                loadProductData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter valid values.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error updating product data: " + e.getMessage());
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
            JOptionPane.showMessageDialog(null, "Product deleted successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error deleting product data: " + e.getMessage());
        }
    }

    private void exitApplication() {
        // Close the current window and open the main frame or application window
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(btnExit);
        frame.dispose();  // Close the current frame

        // Create and open a new main frame (replace this with your main frame logic)
        MainFrame mainFrame = new MainFrame();  // Assuming MainFrame is your main frame class
        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        // Create an instance of ProductManager to display the GUI
        SwingUtilities.invokeLater(RegisteredProducts::new);
    }

    public void setVisible(boolean b) {
        // TODO Auto-generated method stub
    }

}
