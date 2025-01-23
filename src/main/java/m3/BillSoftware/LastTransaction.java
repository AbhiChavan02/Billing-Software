package m3.BillSoftware;

import javax.swing.*;

public class LastTransaction extends JFrame {

    private JLabel lblProductName, lblQuantity, lblWeight, lblTotalPrice;
    private JTextField txtProductName, txtQuantity, txtWeight, txtTotalPrice;
    private JButton btnBackToSales;

    public LastTransaction(String productName, int quantity, double weight, double totalPrice) {
        initComponents(productName, quantity, weight, totalPrice);
    }

    private void initComponents(String productName, int quantity, double weight, double totalPrice) {
        lblProductName = new JLabel("Product Name:");
        lblQuantity = new JLabel("Quantity:");
        lblWeight = new JLabel("Weight (grams):");
        lblTotalPrice = new JLabel("Total Price:");

        txtProductName = new JTextField(productName);
        txtQuantity = new JTextField(String.valueOf(quantity));
        txtWeight = new JTextField(String.valueOf(weight));
        txtTotalPrice = new JTextField(String.valueOf(totalPrice));

        txtProductName.setEditable(false);
        txtQuantity.setEditable(false);
        txtWeight.setEditable(false);
        txtTotalPrice.setEditable(false);

        btnBackToSales = new JButton("Back to Sales");
        btnBackToSales.addActionListener(e -> {
            dispose(); // Close LastTransaction frame
            new ProcessSales(null).setVisible(true); // Reopen the sales page
        });

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridLayout(5, 2, 10, 10));
        panel.add(lblProductName);
        panel.add(txtProductName);
        panel.add(lblQuantity);
        panel.add(txtQuantity);
        panel.add(lblWeight);
        panel.add(txtWeight);
        panel.add(lblTotalPrice);
        panel.add(txtTotalPrice);
        panel.add(btnBackToSales);

        add(panel);

        setTitle("Last Transaction");
        setSize(400, 300);
        setLocationRelativeTo(null); // Center the frame
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LastTransaction("Test Product", 2, 500.0, 25000.0).setVisible(true));
    }
}
