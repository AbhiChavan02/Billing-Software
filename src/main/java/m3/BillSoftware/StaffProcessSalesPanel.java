package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StaffProcessSalesPanel extends JPanel {
    private JTextField txtBarcode, txtProductName, txtPricePerPiece, txtQuantity, 
                       txtTotalPrice, txtStockQuantity, txtCustomerName, txtFinalPrice;
    private JComboBox<String> cmbStaff;
    private JButton btnProcessSale, btnClear, btnRefresh;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;

    public StaffProcessSalesPanel() {
        setLayout(new GridBagLayout());
        setBackground(backgroundColor);
        initializeUI();
        loadStaffDropdown();
        setupActionListeners();
    }

    private void initializeUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(formColor);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header
        JLabel headerLabel = new JLabel("Start Sale - Staff");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(headerLabel, gbc);

        // Form Components
        String[] labels = {"Customer Name:", "Staff:", "Barcode:", "Product Name:", 
                "Rate Per Piece:", "Stock Quantity:", "Quantity:", "Total Price:", "Final Price:"};

        Component[] fields = {
        	    txtCustomerName = createFormTextField(),
        	    cmbStaff = new JComboBox<>(),
        	    createInputPanel(txtBarcode = createFormTextField(), btnRefresh = createActionButton("Refresh", new Color(52, 152, 219))),
        	    txtProductName = createFormTextField(),
        	    txtPricePerPiece = createFormTextField(),
        	    txtStockQuantity = createFormTextField(),
        	    txtQuantity = createFormTextField(),
        	    txtTotalPrice = createFormTextField(),
        	    txtFinalPrice = createFormTextField() // ✅ Ensure this is included!
        	};


        // Make fields uneditable where needed
        txtProductName.setEditable(false);
        txtPricePerPiece.setEditable(false);
        txtStockQuantity.setEditable(false);
        txtTotalPrice.setEditable(false);

        // Add components to form
        for(int i=0; i<labels.length; i++) {
            gbc.gridy = i+1;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            formPanel.add(createFormLabel(labels[i]), gbc);
            
            gbc.gridx = 1;
            formPanel.add(fields[i], gbc);
        }

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(formColor);
        btnProcessSale = createActionButton("Process Sale", new Color(46, 204, 113));
        btnClear = createActionButton("Clear", new Color(231, 76, 60));
        buttonPanel.add(btnProcessSale);
        buttonPanel.add(btnClear);

        gbc.gridy = labels.length+1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel);
    }

    private void setupActionListeners() {
        btnRefresh.addActionListener(e -> fetchProductDetails());
        btnProcessSale.addActionListener(e -> processSale());
        btnClear.addActionListener(e -> clearFields());
    }

    private JTextField createFormTextField() {
        JTextField tf = new JTextField(18);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker()),
            new EmptyBorder(10, 25, 10, 25)
        ));
        
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

    private JPanel createInputPanel(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(formColor);
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private void loadStaffDropdown() {
        try(MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> staffCollection = database.getCollection("Staff");
            
            List<String> staffNames = new ArrayList<>();
            for(Document doc : staffCollection.find()) {
                staffNames.add(doc.getString("firstname"));
            }
            cmbStaff.setModel(new DefaultComboBoxModel<>(staffNames.toArray(new String[0])));
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading staff: " + e.getMessage());
        }
    }

    private void fetchProductDetails() {
        String barcode = txtBarcode.getText().trim();
        if (barcode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a barcode");
            return;
        }

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            Document product = database.getCollection("Product")
                                    .find(new Document("barcode", barcode))
                                    .first();

            if (product != null) {
                txtProductName.setText(product.getString("productName"));

                // Handle both Integer and Double safely
                Object rateObj = product.get("ratePerPiece");
                double ratePerPiece = (rateObj instanceof Number) ? ((Number) rateObj).doubleValue() : 0.0;
                txtPricePerPiece.setText(String.valueOf(ratePerPiece));

                Object stockObj = product.get("stockQuantity");
                int stockQuantity = (stockObj instanceof Number) ? ((Number) stockObj).intValue() : 0;
                txtStockQuantity.setText(String.valueOf(stockQuantity));
            } else {
                JOptionPane.showMessageDialog(this, "Product not found!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }


    private void processSale() {
        try {
            // Validate inputs
            if (txtCustomerName.getText().trim().isEmpty() ||
                cmbStaff.getSelectedItem() == null ||
                txtBarcode.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields");
                return;
            }
            

            double ratePerPiece = Double.parseDouble(txtPricePerPiece.getText());
            int quantity = Integer.parseInt(txtQuantity.getText());



            // Fetch stock quantity and check availability
            int stockQuantity = Integer.parseInt(txtStockQuantity.getText());
            if (stockQuantity < quantity) {
                JOptionPane.showMessageDialog(this, "❌ Insufficient stock! Available: " + stockQuantity);
                return; // Prevents processing the sale if stock is not enough
            }

            // Calculate total price
            double totalPrice = ratePerPiece * quantity;
            txtTotalPrice.setText(String.format("₹%.2f", totalPrice));
            double finalPrice = Double.parseDouble(txtFinalPrice.getText());
            double savings = totalPrice - finalPrice;
            

            // Get product name
            String productName = txtProductName.getText().trim();

            // Update database
            try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
                MongoDatabase database = mongoClient.getDatabase("testDB");

                // Update stock (only if there's enough stock)
                Document productUpdate = new Document("$inc", new Document("stockQuantity", -quantity));
                database.getCollection("Product")
                       .updateOne(new Document("barcode", txtBarcode.getText()), productUpdate);

                // Create sales record (including product name)
                Document saleRecord = new Document()
                	    .append("customerName", txtCustomerName.getText()) // ✅ Ensure this exists
                	    .append("staff", cmbStaff.getSelectedItem())
                	    .append("barcode", txtBarcode.getText())
                	    .append("productName", productName)
                	    .append("quantity", quantity)
                	    .append("totalPrice", totalPrice)
                	    .append("finalPrice", finalPrice)
                	    .append("savings", savings)
                	    .append("timestamp", new java.util.Date());

                	database.getCollection("Sales").insertOne(saleRecord);

                JOptionPane.showMessageDialog(this, "✅ Sale processed successfully!");
                clearFields();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "⚠️ Invalid numeric values!\nPlease check quantity/weight");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "⚠️ Error processing sale: " + e.getMessage());
        }
    }




    private void clearFields() {
        txtCustomerName.setText("");	
        txtBarcode.setText("");
        txtProductName.setText("");
        txtPricePerPiece.setText("");
        txtStockQuantity.setText("");
        txtQuantity.setText("");
        txtTotalPrice.setText("");
        txtFinalPrice.setText("");
        cmbStaff.setSelectedIndex(0);
        
    }
}