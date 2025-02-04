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

public class StaffRegisteredProductsPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> searchCriteria;
    private JButton btnRefresh, btnSearch;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;

    public StaffRegisteredProductsPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        createUI();
        loadProductData();
    }

    private void createUI() {
        tableModel = new DefaultTableModel(new Object[]{"Barcode", "Product Name", "Category", "Rate Per Piece", "Stock Quantity"}, 0);
        productTable = new JTable(tableModel);
        productTable.setRowHeight(25);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        btnRefresh = createActionButton("Refresh", new Color(46, 204, 113));
        btnSearch = createActionButton("Search", new Color(241, 196, 15));

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchCriteria = new JComboBox<>(new String[]{"Barcode", "Product Name", "Category", "Rate Per Piece", "Stock Quantity"});
        searchCriteria.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnRefresh.addActionListener(e -> loadProductData());
        btnSearch.addActionListener(e -> handleSearch());

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(formColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(searchCriteria);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

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
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private void loadProductData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");
            tableModel.setRowCount(0);

            for (Document product : productCollection.find()) {
                String barcode = product.getString("barcode");
                String productName = product.getString("productName");
                String category = product.getString("category");
                Number ratePerPiece = product.get("ratePerPiece", Number.class);
                Integer stockQuantity = product.getInteger("stockQuantity");
                
                tableModel.addRow(new Object[]{
                    barcode != null ? barcode : "N/A",
                    productName != null ? productName : "N/A",
                    category != null ? category : "N/A",
                    ratePerPiece != null ? ratePerPiece.intValue() : 0,
                    stockQuantity != null ? stockQuantity : 0
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
                        query.append("ratePerPiece", Double.parseDouble(searchValue));
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input for Rate Per Piece.");
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
                tableModel.addRow(new Object[]{
                    product.getString("barcode"),
                    product.getString("productName"),
                    product.getString("category"),
                    product.get("ratePerPiece", Number.class),
                    product.getInteger("stockQuantity")
                });
            }

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No products found matching the search criteria.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error performing search: " + e.getMessage());
        }
    }
}
