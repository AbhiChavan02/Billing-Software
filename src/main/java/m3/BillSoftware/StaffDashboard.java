package m3.BillSoftware;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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
        
     // Load company logo with proper scaling
        JLabel lblCompanyLogo = loadLogo("E:\\PWS\\PWS\\img\\PWS - Logo .png", 200, 100);
        
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(primaryColor);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        
        
     // In the constructor:
        if (lblCompanyLogo != null) {
            userInfoPanel.add(lblCompanyLogo);
            System.out.println("Logo added to userInfoPanel");
        } else {
            System.out.println("Logo is null, not added to userInfoPanel");
        }
        
        
        
        

        // Create Menu Panel (Left Side)
        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(4, 1, 15, 15)); // Increased to 4 rows
        menuPanel.setPreferredSize(new Dimension(280, getHeight()));
        menuPanel.setBackground(primaryColor);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        
        

        JButton btnProcessSales = createMenuButton("Start Sale");
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
    
    private JLabel loadLogo(String path, int width, int height) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Error: Image not found at " + path);
            return null;
        }

        ImageIcon originalIcon = new ImageIcon(path);
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(scaledImage));
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
