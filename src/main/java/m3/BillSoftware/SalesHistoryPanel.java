package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SalesHistoryPanel extends JPanel {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private Color backgroundColor = new Color(241, 242, 246); // Light gray background
    private Color formColor = Color.WHITE; // White for form background

    public SalesHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        createUI();
        fetchSalesData();
    }

    private void createUI() {
        // Create table model with columns for sales data
        tableModel = new DefaultTableModel(new Object[]{"Product Name", "Quantity", "Weight", "Total Price", "Seller", "Date"}, 0);
        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(25); // Set row height for better readability
        salesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        salesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a panel for the back button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(formColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));



        // Add components to the main panel
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
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

    private void fetchSalesData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> salesCollection = database.getCollection("Sales");

            // Fetch all sales documents from MongoDB
            List<Document> salesList = salesCollection.find().into(new java.util.ArrayList<>());
            for (Document sale : salesList) {
                // Ensure proper field extraction with type checks
                String productName = sale.getString("productName");
                if (productName == null) productName = "Unknown Product";

                int quantity = sale.getInteger("quantity", 0);

                double weight = 0.0;
                if (sale.containsKey("weight")) {
                    Object weightObj = sale.get("weight");
                    if (weightObj instanceof Number) {
                        weight = ((Number) weightObj).doubleValue(); // Works for both Integer and Double
                    }
                }

                double totalPrice = 0.0;
                if (sale.containsKey("totalPrice")) {
                    Object totalPriceObj = sale.get("totalPrice");
                    if (totalPriceObj instanceof Number) {
                        totalPrice = ((Number) totalPriceObj).doubleValue(); // Works for both Integer and Double
                    }
                }

                String timestamp = sale.containsKey("timestamp") ? sale.getDate("timestamp").toString() : "N/A";
                String staff = sale.containsKey("staff") ? sale.getString("staff") : "Admin";

                // Add row to table
                tableModel.addRow(new Object[]{productName, quantity, weight, totalPrice, staff, timestamp});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching sales records: " + e.getMessage());
        }
    }
}