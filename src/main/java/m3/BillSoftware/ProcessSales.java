package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javax.swing.*;

public class ProcessSales extends javax.swing.JFrame {

    private MainFrame MainFrame;  // Reference to the MainFrame class
    private javax.swing.JButton btnLastTransaction;

    public ProcessSales(MainFrame MainFrame) {
        this.MainFrame = MainFrame;
        if (this.MainFrame == null) {
            System.out.println("DEBUG: MainFrame passed to ProcessSales is null!");
        } else {
            System.out.println("DEBUG: MainFrame passed to ProcessSales is not null.");
        }
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
    	
        lblBarcode = new javax.swing.JLabel();
        txtBarcode = new javax.swing.JTextField();
        lblProductName = new javax.swing.JLabel();
        txtProductName = new javax.swing.JTextField();
        lblPricePerGram = new javax.swing.JLabel();
        txtPricePerGram = new javax.swing.JTextField();
        lblStockQuantity = new javax.swing.JLabel();
        txtStockQuantity = new javax.swing.JTextField();
        lblQuantity = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        lblWeight = new javax.swing.JLabel();
        txtWeight = new javax.swing.JTextField();
        lblTotalPrice = new javax.swing.JLabel();
        txtTotalPrice = new javax.swing.JTextField();
        btnProcess = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();  // Exit button added

        // Last Transaction Button
        btnLastTransaction = new javax.swing.JButton();
        btnLastTransaction.setText("Last Transaction");
        btnLastTransaction.addActionListener(evt -> showLastTransaction());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Process Sales");
        
        

        lblBarcode.setText("Barcode:");
        txtBarcode.setPreferredSize(new java.awt.Dimension(200, 30));

        lblProductName.setText("Product Name:");
        txtProductName.setEditable(false);

        lblPricePerGram.setText("Price per Gram:");
        txtPricePerGram.setEditable(false);

        lblStockQuantity.setText("Stock Quantity:");
        txtStockQuantity.setEditable(false);

        lblQuantity.setText("Quantity:");

        lblWeight.setText("Weight (grams):");

        lblTotalPrice.setText("Total Price:");
        txtTotalPrice.setEditable(false);

        btnProcess.setText("Process Sale");
        btnProcess.addActionListener(evt -> processSale());

        btnClear.setText("Clear");
        btnClear.addActionListener(evt -> clearFields());

        // Refresh button - fetch product details based on barcode
        btnRefresh.setText("Refresh");
        btnRefresh.setPreferredSize(new java.awt.Dimension(80, 25));
        btnRefresh.addActionListener(evt -> fetchProductDetails());

        // Exit button - close the current window and show the main frame
        btnExit.setText("Exit");
        btnExit.addActionListener(evt -> exitApplication());

        // Layout setup
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblBarcode)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtBarcode)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnRefresh))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblProductName)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtProductName))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblPricePerGram)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtPricePerGram))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblStockQuantity)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtStockQuantity))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblQuantity)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtQuantity))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblWeight)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtWeight))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(lblTotalPrice)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtTotalPrice)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addGap(52, 52, 52)
                    .addComponent(btnProcess)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                    .addComponent(btnClear)
                    .addGap(52, 52, 52))
                .addGroup(layout.createSequentialGroup()
                    .addGap(200, 200, 200)
                    .addComponent(btnExit)
                    .addGap(200, 200, 200))
                .addGroup(layout.createSequentialGroup()
                    .addGap(200, 200, 200)
                    .addComponent(btnLastTransaction)  // Last Transaction button added here
                    .addGap(200, 200, 200))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblBarcode)
                        .addComponent(txtBarcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRefresh))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblProductName)
                        .addComponent(txtProductName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPricePerGram)
                        .addComponent(txtPricePerGram, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblStockQuantity)
                        .addComponent(txtStockQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblQuantity)
                        .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblWeight)
                        .addComponent(txtWeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblTotalPrice)
                        .addComponent(txtTotalPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnProcess)
                        .addComponent(btnClear))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLastTransaction))  // Last Transaction button
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(btnExit)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }

    private void fetchProductDetails() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            String barcode = txtBarcode.getText();
            if (barcode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a barcode.");
                return;
            }

            Document product = productCollection.find(new Document("barcode", barcode)).first();
            if (product != null) {
                txtProductName.setText(product.getString("productName"));
                txtPricePerGram.setText(String.valueOf(product.getDouble("pricePerGram")));
                txtStockQuantity.setText(String.valueOf(product.getInteger("stockQuantity")));
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to the database: " + e.getMessage());
        }
    }

    private void showLastTransaction() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> salesCollection = database.getCollection("Sales");

            Document lastTransaction = salesCollection.find().sort(new Document("timestamp", -1)).first();
            if (lastTransaction != null) {
                String message = String.format("Last Transaction:\nProduct Name: %s\nQuantity: %d\nWeight: %.2f grams\nTotal Price: %.2f\nDate: %s",
                        lastTransaction.getString("productName"),
                        lastTransaction.getInteger("quantity"),
                        lastTransaction.getDouble("weight"),
                        lastTransaction.getDouble("totalPrice"),
                        lastTransaction.getDate("timestamp").toString());
                JOptionPane.showMessageDialog(this, message);
            } else {
                JOptionPane.showMessageDialog(this, "No transactions found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching last transaction: " + e.getMessage());
        }
    }

    private void processSale() {
        try {
            double pricePerGram = Double.parseDouble(txtPricePerGram.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());
            double weight = Double.parseDouble(txtWeight.getText());
            double totalPrice = pricePerGram * weight * quantity; // Adjusted formula
            txtTotalPrice.setText(String.valueOf(totalPrice));

            // Save sale to MongoDB and update stock quantity
            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");
                MongoCollection<Document> salesCollection = database.getCollection("Sales");
                MongoCollection<Document> productCollection = database.getCollection("Product");

                String barcode = txtBarcode.getText();

                // Fetch product details
                Document product = productCollection.find(new Document("barcode", barcode)).first();
                if (product != null) {
                    int currentStock = product.getInteger("stockQuantity");

                    // Check if sufficient stock is available
                    if (quantity > currentStock) {
                        JOptionPane.showMessageDialog(this, "Insufficient stock available!");
                        return;
                    }

                    // Update stock quantity
                    int newStock = currentStock - quantity;
                    productCollection.updateOne(new Document("barcode", barcode),
                            new Document("$set", new Document("stockQuantity", newStock)));

                    // Save sale to sales collection
                    Document sale = new Document("productName", txtProductName.getText())
                            .append("quantity", quantity)
                            .append("weight", weight)
                            .append("totalPrice", totalPrice)
                            .append("timestamp", new java.util.Date());
                    salesCollection.insertOne(sale);

                    JOptionPane.showMessageDialog(this, "Sale processed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found.");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter valid numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing sale: " + e.getMessage());
        }
    }


    private void clearFields() {
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerGram.setText("");
        txtStockQuantity.setText("");
        txtQuantity.setText("");
        txtWeight.setText("");
        txtTotalPrice.setText("");
    }

    private void exitApplication() {
        this.dispose(); // Close the current window
        if (MainFrame == null) {
            System.out.println("MainFrame is NULL, creating a new instance...");
            MainFrame = new MainFrame(); // Create a new MainFrame instance
        }
        MainFrame.setVisible(true); // Show the MainFrame
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(); // Create the MainFrame instance
            ProcessSales processSales = new ProcessSales(mainFrame); // Pass MainFrame to ProcessSales
            processSales.setVisible(true); // Show the ProcessSales window
        });
    }

    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLastTransaction1;  // Last Transaction button
    private javax.swing.JButton btnProcess;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JLabel lblBarcode;
    private javax.swing.JLabel lblPricePerGram;
    private javax.swing.JLabel lblProductName;
    private javax.swing.JLabel lblQuantity;
    private javax.swing.JLabel lblStockQuantity;
    private javax.swing.JLabel lblTotalPrice;
    private javax.swing.JLabel lblWeight;
    private javax.swing.JTextField txtBarcode;
    private javax.swing.JTextField txtPricePerGram;
    private javax.swing.JTextField txtProductName;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtStockQuantity;
    private javax.swing.JTextField txtTotalPrice;
    private javax.swing.JTextField txtWeight;
}
