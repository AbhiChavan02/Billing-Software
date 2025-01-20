package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RegisteredProducts {

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JButton btnUpdate, btnRefresh, btnExit;

    public RegisteredProducts() {
        // Initialize table model and set it to productTable
        tableModel = new DefaultTableModel(new Object[]{"Barcode", "Product Name", "Category", "Price Per Gram", "Stock Quantity"}, 0);
        productTable = new JTable(tableModel);
        btnUpdate = new JButton("Update");
        btnRefresh = new JButton("Refresh");
        btnExit = new JButton("Exit");

        // Set the button action listener for update
        btnUpdate.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                updateProductData(selectedRow);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a product to update.");
            }
        });

        // Set the button action listener for refresh
        btnRefresh.addActionListener(e -> loadProductData());

        // Set the button action listener for exit
        btnExit.addActionListener(e -> exitApplication());

        // Load product data into table
        loadProductData();

        // Setting up JFrame
        JPanel panel = new JPanel();
        panel.add(btnUpdate);
        panel.add(btnRefresh);
        panel.add(btnExit);

        JFrame frame = new JFrame("Product Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(productTable), "Center");
        frame.add(panel, "South");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
                        product.getString("category"),  // Category added
                        product.getDouble("pricePerGram"),
                        product.getInteger("stockQuantity")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading product data: " + e.getMessage());
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

	public Object setVisible(boolean b) {
		// TODO Auto-generated method stub
		return null;
	}
}

