package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

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
    


 // In your ImageCellRenderer:
    private class ImageCellRenderer extends JLabel implements TableCellRenderer {
        private final int IMAGE_SIZE = 60;  // Set the size of the image (60x60 pixels)

        public ImageCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);  // Center-align the image
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) value;
                // Scale the image to a square shape with fixed width and height
                Image img = icon.getImage().getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(img));  // Set the resized image
            } else {
                setIcon(null);  // In case the value is not an ImageIcon
            }
            return this;
        }
    }
    
 // Move the ButtonCellRenderer class outside the method

    private class ButtonCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout to center content
            panel.setBackground(Color.WHITE); // Set the background color of the panel

            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0; // Put the buttons in the first column of the layout
            constraints.gridy = 0; // Align to the top row of the layout
            constraints.anchor = GridBagConstraints.CENTER; // Center-align the buttons
            constraints.insets = new Insets(5, 5, 5, 5); // Add some padding around the buttons

            if (value != null) {
                // Add the buttons to the panel only if there is a value (i.e., for non-empty rows)
                JButton updateButton = new JButton("Update");
                updateButton.setBackground(new Color(52, 152, 219)); // Blue color
                updateButton.setForeground(Color.WHITE);
                updateButton.addActionListener(e -> {
                    // Implement the update functionality
                    updateProductData(row);
                });

                JButton deleteButton = new JButton("Delete");
                deleteButton.setBackground(new Color(231, 76, 60)); // Red color
                deleteButton.setForeground(Color.WHITE);
                deleteButton.addActionListener(e -> {
                    // Implement the delete functionality
                    deleteProductData(row);
                });

                // Add buttons to the panel with constraints for centering
                panel.add(updateButton, constraints);
                constraints.gridx = 1; // Shift the next button to the right in the layout
                panel.add(deleteButton, constraints);
            }

            return panel;
        }
    }




    private void createUI() {
        // Initialize table model and set it to product table
    	// Add a new column for Action (Update, Delete)
    	tableModel = new DefaultTableModel(new Object[]{"Barcode", "Product Name", "Image", "Category", "Rate Per Piece", "Stock Quantity", "Actions"}, 0);

        productTable = new JTable(tableModel);
//        productTable.	setRowHeight(25); // Set row height for better readability
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // In your createUI method:
        productTable.setRowHeight(100);  // Increase row height to display larger images
        productTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Set width for the image column
     // Set custom renderer for the Action column (column index 6)
        productTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonCellRenderer());


        // Initialize buttons
        btnUpdate = createActionButton("Update", new Color(52, 152, 219)); // Blue
        btnRefresh = createActionButton("Refresh", new Color(46, 204, 113)); // Green
        btnDelete = createActionButton("Delete", new Color(231, 76, 60)); // Red
        btnSearch = createActionButton("Search", new Color(241, 196, 15)); // Yellow

        // Initialize search components
        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchCriteria = new JComboBox<>(new String[]{"Barcode", "Product Name","Image", "Category", "Rate Per Piece", "Stock Quantity"});
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
//        searchPanel.add(btnUpdate);
        searchPanel.add(btnRefresh);
//        searchPanel.add(btnDelete);

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

            tableModel.setRowCount(0);  // Clear existing rows

            for (Document product : productCollection.find()) {
                // Safely get fields and handle unexpected types
                String barcode = getStringFromDocument(product, "barcode");
                String productName = getStringFromDocument(product, "productName");
                String category = getStringFromDocument(product, "category");

                // Log the raw data before casting
                System.out.println("Raw Data: " + product.toJson());

                // Safely retrieve the ratePerPiece and stockQuantity
                Object ratePerPieceObj = product.get("ratePerPiece");
                Double ratePerPiece = ratePerPieceObj instanceof Double ? (Double) ratePerPieceObj :
                        ratePerPieceObj instanceof Integer ? ((Integer) ratePerPieceObj).doubleValue() : 0.0;

                Object stockQuantityObj = product.get("stockQuantity");
                Integer stockQuantity = stockQuantityObj instanceof Integer ? (Integer) stockQuantityObj : 0;

                // Log the processed data
                System.out.println("Processed Values - Rate Per Piece: " + ratePerPiece + ", Stock Quantity: " + stockQuantity);

                String imageUrl = product.getString("productImagePath");

                // Load Image from Cloudinary (or set default if missing)
                ImageIcon productImage = loadProductImage(imageUrl);

                // Add a row with the data, including the buttons column
                tableModel.addRow(new Object[]{
                        barcode != null ? barcode : "N/A",
                        productName != null ? productName : "N/A",
                        productImage,  // IMAGE COLUMN
                        category != null ? category : "N/A",
                        ratePerPiece != null ? ratePerPiece : 0.0,  // Ensure it's a valid Double
                        stockQuantity != null ? stockQuantity : 0,
                        // Add Update and Delete buttons in the last column
                        createUpdateButton(barcode),
                        createDeleteButton(barcode)
                });
            }

            // Set custom renderer for the image column (column index 2)
            productTable.getColumnModel().getColumn(2).setCellRenderer(new ImageCellRenderer());

//             Center the buttons in the last two columns (index 6 and 7)
         // Apply custom renderer for the Action column (index 6)
            productTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonCellRenderer());


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading product data: " + e.getMessage());
        }
    }



    private Object createDeleteButton(String barcode) {
    	JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(new Color(231, 76, 60)); // Red color
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deleteButton.addActionListener(e -> {
            int row = findRowByBarcode(barcode);
            if (row != -1) {
                deleteProductData(row);  // Call the delete method for the selected row
            }
        });

        return deleteButton;
	}

	private Object createUpdateButton(String barcode) {
		JButton updateButton = new JButton("Update");
	    updateButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
	    updateButton.setForeground(Color.WHITE);
	    updateButton.setBackground(new Color(52, 152, 219)); // Blue color
	    updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    
	    updateButton.addActionListener(e -> {
	        int row = findRowByBarcode(barcode);
	        if (row != -1) {
	            updateProductData(row);  // Call the update method for the selected row
	        }
	    });

	    return updateButton;
	}

	private int findRowByBarcode(String barcode) {
		 for (int i = 0; i < tableModel.getRowCount(); i++) {
		        if (tableModel.getValueAt(i, 0).equals(barcode)) {
		            return i;  // Return the row index of the matching barcode
		        }
		    }
		    return -1;  // Return -1 if barcode is not found
	}

	private String getStringFromDocument(Document product, String key) {
        Object value = product.get(key);
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Integer) {
            return String.valueOf(value);  // Convert Integer to String
        }
        return "";  // Return empty string if value is neither String nor Integer
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
                String barcode = getStringFromDocument(product, "barcode");
                String productName = getStringFromDocument(product, "productName");
                String category = getStringFromDocument(product, "category");

                // Log the raw data before processing
                System.out.println("Raw Data: " + product.toJson());

                // Safely retrieve ratePerPiece and stockQuantity
                Object ratePerPieceObj = product.get("ratePerPiece");
                Double ratePerPiece = ratePerPieceObj instanceof Double ? (Double) ratePerPieceObj :
                    ratePerPieceObj instanceof Integer ? ((Integer) ratePerPieceObj).doubleValue() : 0.0;

                Object stockQuantityObj = product.get("stockQuantity");
                Integer stockQuantity = stockQuantityObj instanceof Integer ? (Integer) stockQuantityObj : 0;

                // Log the processed data
                System.out.println("Processed Values - Rate Per Piece: " + ratePerPiece + ", Stock Quantity: " + stockQuantity);

                tableModel.addRow(new Object[]{
                    barcode != null ? barcode : "N/A",
                    productName != null ? productName : "N/A",
                    loadProductImage(product.getString("productImagePath")), // IMAGE COLUMN
                    category != null ? category : "N/A",
                    ratePerPiece != null ? ratePerPiece : 0.0, // Handle ratePerPiece as Double
                    stockQuantity != null ? stockQuantity : 0
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No products found matching the search criteria.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error performing search: " + e.getMessage());
        }
    }




    private ImageIcon loadProductImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Load image from Cloudinary URL
                Image image = new ImageIcon(new java.net.URL(imageUrl)).getImage();

                // Resize image to fit in the table cell (you can adjust the size as needed)
                Image scaledImage = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }

        // Return a default placeholder image if image fails to load
        return new ImageIcon(new ImageIcon("default-placeholder.png").getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
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