package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class SalesHistoryPanel extends JPanel {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;

    public SalesHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        createUI();
        fetchSalesData();
    }

    private void createUI() {
        // Updated table model with Customer Name column
        tableModel = new DefaultTableModel(new Object[]{
            "Product Name", "Quantity", "Total Price", 
            "Customer Name", "Seller", "Date"
        }, 0);
        
        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(25);
        salesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        salesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create button panel with refresh button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(formColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add refresh button
        JButton btnRefresh = createActionButton("Refresh", new Color(52, 152, 219));
        btnRefresh.addActionListener((ActionEvent e) -> {
            tableModel.setRowCount(0); // Clear existing data
            fetchSalesData();
        });
        buttonPanel.add(btnRefresh);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchSalesData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> salesCollection = database.getCollection("Sales");

            List<Document> salesList = salesCollection.find().into(new java.util.ArrayList<>());
            for (Document sale : salesList) {
                // Extract customer name with null check
                String customerName = sale.containsKey("customerName") 
                    ? sale.getString("customerName") 
                    : "N/A";

                // Existing field extraction
                String productName = sale.getString("productName");
                int quantity = sale.getInteger("quantity", 0);
                
                double totalPrice = getDoubleValue(sale, "totalPrice");
                
                String timestamp = sale.containsKey("timestamp") 
                    ? sale.getDate("timestamp").toString() 
                    : "N/A";
                String staff = sale.containsKey("staff") 
                    ? sale.getString("staff") 
                    : "Admin";

                // Add row with customer name
                tableModel.addRow(new Object[]{
                    productName, 
                    quantity, 
                    totalPrice,
                    customerName,  // New column
                    staff,
                    timestamp
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching sales records: " + e.getMessage());
        }
    }

    // Helper method to safely extract double values
    private double getDoubleValue(Document doc, String key) {
        if (doc.containsKey(key)) {
            Object value = doc.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.0;
    }

    // Existing createActionButton method remains unchanged
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
}