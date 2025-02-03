package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class SalesHistory extends JFrame {

    private JTable salesTable;
    private DefaultTableModel tableModel;

    public SalesHistory() {
        setTitle("Sales History");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create table model with an additional column for Seller (staff)
        tableModel = new DefaultTableModel(new Object[]{"Product Name", "Quantity", "Weight", "Total Price", "Seller", "Date"}, 0);
        salesTable = new JTable(tableModel);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, "Center");

        // Fetch and display sales data
        fetchSalesData();

        // Create 'Back to Main Menu' button
        JButton btnBackToMainMenu = new JButton("Back to Main Menu");
        btnBackToMainMenu.addActionListener(e -> {
            dispose();  // Close SalesHistory window
            new MainFrame().setVisible(true);  // Open MainFrame
        });

        // Add button to the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnBackToMainMenu);
        add(buttonPanel, "South");  // Add panel to the bottom of the frame

        setLocationRelativeTo(null);
    }

    private void fetchSalesData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> salesCollection = database.getCollection("Sales");

            List<Document> salesList = salesCollection.find().into(new java.util.ArrayList<>());
            for (Document sale : salesList) {
                String productName = sale.getString("productName");
                int quantity = sale.getInteger("quantity");
                double weight = sale.getDouble("weight");
                double totalPrice = sale.getDouble("totalPrice");
                String timestamp = sale.getDate("timestamp").toString();
                String staff = sale.getString("staff");  // Get the staff name from the sale document

                // Add row to the table including staff information
                tableModel.addRow(new Object[]{productName, quantity, weight, totalPrice, staff, timestamp});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching sales records: " + e.getMessage());
        }
    }
}
