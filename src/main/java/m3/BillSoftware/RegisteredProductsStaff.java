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

public class RegisteredProductsStaff {

    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> searchCriteria;
    private JButton btnRefresh, btnExit, btnSearch;

    public RegisteredProductsStaff() {
        // Initialize table model and set it to product table
        tableModel = new DefaultTableModel(new Object[]{"Barcode", "Product Name", "Category", "Price Per Gram", "Stock Quantity"}, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25); // Set row height for better readability

        // Initialize buttons
        btnRefresh = new JButton("Refresh");
        btnExit = new JButton("Back");
        btnSearch = new JButton("Search");

        // Initialize search components
        txtSearch = new JTextField(15); // Text field for search input
        searchCriteria = new JComboBox<>(new String[]{"Barcode", "Product Name", "Category", "Price Per Gram", "Stock Quantity"}); // Dropdown for search criteria

        btnRefresh.addActionListener(e -> loadProductData());


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
        panel.add(btnRefresh);
//        panel.add(btnDelete);
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


    private void exitApplication() {
        // Close the current window and open the main frame or application window
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(btnExit);
        frame.dispose();  // Close the current frame

        // Create and open a new main frame (replace this with your main frame logic)
        MainFrameStaff mainFrame = new MainFrameStaff();  // Assuming MainFrame is your main frame class
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
