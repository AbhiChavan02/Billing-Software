package m3.BillSoftware;

import javax.swing.*;
import java.awt.*;

public class StaffDashboard extends JFrame {
    private JPanel menuPanel, contentPanel;
    private JLabel lblUser;
    private JSplitPane splitPane;
    private Color primaryColor = new Color(40, 58, 82);
    private Color secondaryColor = new Color(241, 242, 246);

    public StaffDashboard(String username) { // Accept username
        setTitle("Jewelry POS System - Staff Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create Menu Panel (Left Side)
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(4, 1, 15, 15)); // Increased to 4 rows
        menuPanel.setPreferredSize(new Dimension(280, getHeight()));
        menuPanel.setBackground(primaryColor);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JButton btnProcessSales = createMenuButton("Process Sales");
        JButton btnRegisteredProducts = createMenuButton("Registered Products");
        JButton btnLogout = createMenuButton("Logout");

        // Display Logged-in User
        lblUser = new JLabel("Logged in: " + username, SwingConstants.CENTER);
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        menuPanel.add(lblUser); // Add username label at the top
        menuPanel.add(btnProcessSales);
        menuPanel.add(btnRegisteredProducts);
        menuPanel.add(btnLogout);

        // Create Content Panel (Right Side)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(secondaryColor);

        // SplitPane to hold menu and content
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, contentPanel);
        splitPane.setDividerLocation(280);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(3);
        add(splitPane, BorderLayout.CENTER);

        // Button Actions
        btnProcessSales.addActionListener(e -> openProcessSales());
        btnRegisteredProducts.addActionListener(e -> openRegisteredProducts());
        btnLogout.addActionListener(e -> {
            dispose();
            new StaffLoginRegister().setVisible(true);
        });
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 73, 102));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(67, 91, 124));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 73, 102));
            }
        });
        return btn;
    }

    private void openProcessSales() {
        contentPanel.removeAll();
        contentPanel.add(new StaffProcessSalesPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void openRegisteredProducts() {
        contentPanel.removeAll();
        contentPanel.add(new StaffRegisteredProductsPanel(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StaffDashboard("Guest").setVisible(true));
    }
}
