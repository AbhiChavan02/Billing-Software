package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class AdminStaffLogin extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel adminPanel, staffPanel;
    private JTextField adminUsername, staffUsername;
    private JPasswordField adminPassword, staffPassword;
    private JTextField adminFirstName, adminLastName;
    private JButton btnAdminLogin, btnAdminRegister, btnStaffLogin;
    private boolean isAdminLoginMode = true;

    public AdminStaffLogin() {
        setTitle("Admin & Staff Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        adminPanel = createAdminPanel();
        staffPanel = createStaffPanel();
        
        tabbedPane.addTab("Admin Login", adminPanel);
        tabbedPane.addTab("Staff Login", staffPanel);
        
        add(tabbedPane);
        setVisible(true);
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new LineBorder(Color.BLUE, 3, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        adminUsername = new JTextField(20);
        panel.add(adminUsername, gbc);

        // Password
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        adminPassword = new JPasswordField(20);
        panel.add(adminPassword, gbc);

        // First Name (Hidden in login mode)
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        adminFirstName = new JTextField(20);
        panel.add(adminFirstName, gbc);

        // Last Name (Hidden in login mode)
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        adminLastName = new JTextField(20);
        panel.add(adminLastName, gbc);

        // Buttons
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        btnAdminLogin = new JButton("Login");
        btnAdminRegister = new JButton("Register");
        buttonPanel.add(btnAdminLogin);
        buttonPanel.add(btnAdminRegister);
        panel.add(buttonPanel, gbc);

        btnAdminRegister.setVisible(false);

        btnAdminLogin.addActionListener(e -> handleAdminLogin());
        btnAdminRegister.addActionListener(e -> handleAdminRegister());
        
        return panel;
    }

    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new LineBorder(Color.GREEN, 3, true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        staffUsername = new JTextField(20);
        panel.add(staffUsername, gbc);

        // Password
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        staffPassword = new JPasswordField(20);
        panel.add(staffPassword, gbc);

        // Login Button
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        btnStaffLogin = new JButton("Login");
        panel.add(btnStaffLogin, gbc);

        btnStaffLogin.addActionListener(e -> handleStaffLogin());
        return panel;
    }

    private void handleAdminLogin() {
        String username = adminUsername.getText();
        String password = new String(adminPassword.getPassword());
        // MongoDB login logic here
        JOptionPane.showMessageDialog(this, "Admin Login Successful");
    }

    private void handleAdminRegister() {
        String firstName = adminFirstName.getText();
        String lastName = adminLastName.getText();
        String username = adminUsername.getText();
        String password = new String(adminPassword.getPassword());
        // MongoDB registration logic here
        JOptionPane.showMessageDialog(this, "Admin Registration Successful");
    }

    private void handleStaffLogin() {
        String username = staffUsername.getText();
        String password = new String(staffPassword.getPassword());
        // MongoDB login logic here
        JOptionPane.showMessageDialog(this, "Staff Login Successful");
    }

    public static void main(String[] args) {
        new AdminStaffLogin();
    }
}
