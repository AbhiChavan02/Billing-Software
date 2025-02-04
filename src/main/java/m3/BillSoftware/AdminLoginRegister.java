package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminLoginRegister extends JFrame {
    private JPanel mainPanel, formPanel;
    private JTextField txtFirstName, txtLastName, txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister, btnToggle, btnAdmin;
    private JLabel lblFirstName, lblLastName, headerLabel;
    private boolean isLoginMode = true;

    public AdminLoginRegister() {
        setTitle("Admin Login & Register");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(241, 242, 246));
        add(mainPanel, BorderLayout.CENTER);

        initializeUI();
        toggleMode();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form Panel
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(40, 58, 82), 2));
        formPanel.setPreferredSize(new Dimension(400, 400));

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.anchor = GridBagConstraints.CENTER;
        gbcMain.weightx = gbcMain.weighty = 1;
        mainPanel.add(formPanel, gbcMain);

        // Header
        headerLabel = new JLabel("Staff Login");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(headerLabel, gbc);

        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = createTextField();
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 2;
        gbc.gridx = 0;
        formPanel.add(createLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
        formPanel.add(txtPassword, gbc);

        // First Name
        gbc.gridy = 3;
        gbc.gridx = 0;
        lblFirstName = createLabel("First Name:");
        formPanel.add(lblFirstName, gbc);
        gbc.gridx = 1;
        txtFirstName = createTextField();
        formPanel.add(txtFirstName, gbc);

        // Last Name
        gbc.gridy = 4;
        gbc.gridx = 0;
        lblLastName = createLabel("Last Name:");
        formPanel.add(lblLastName, gbc);
        gbc.gridx = 1;
        txtLastName = createTextField();
        formPanel.add(txtLastName, gbc);

     // Buttons
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnLogin = createButton("Login", new Color(46, 204, 113));
        btnRegister = createButton("Register", new Color(52, 152, 219));
        btnToggle = createButton("Switch to Register", new Color(241, 196, 15));
        btnAdmin = createButton("Staff Login", new Color(155, 89, 182));
        
     // Add both btnLogin and btnRegister to the button panel
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnToggle);
        buttonPanel.add(btnAdmin);

        formPanel.add(buttonPanel, gbc);

        // Action Listeners
        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> handleRegister());
        btnToggle.addActionListener(e -> toggleMode());
        btnAdmin.addActionListener(e -> {
            dispose();
            new StaffLoginRegister().setVisible(true); // Assume AdminLogin class exists
        });
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        lblFirstName.setVisible(!isLoginMode);
        lblLastName.setVisible(!isLoginMode);
        txtFirstName.setVisible(!isLoginMode);
        txtLastName.setVisible(!isLoginMode);
        btnLogin.setVisible(isLoginMode);
        btnRegister.setVisible(!isLoginMode);
        btnToggle.setText(isLoginMode ? "Switch to Register" : "Switch to Login");
        headerLabel.setText(isLoginMode ? "Admin Login" : "Admin Register");
        
        // Change border color
        Color borderColor = isLoginMode ? new Color(40, 58, 82) : new Color(52, 152, 219);
        formPanel.setBorder(BorderFactory.createLineBorder(borderColor, 2));
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> collection = database.getCollection("User");

            Document user = collection.find(new Document("username", username).append("password", password)).first();
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Login Successful");
                dispose();
                
                // Get user details from database
                String loggedInUsername = user.getString("username");
                String loggedInFirstName = user.getString("firstname");
                String loggedInLastName = user.getString("lastname");
                
                // Open ProductRegistration with user details
                new ProductRegistration(loggedInUsername, loggedInFirstName, loggedInLastName).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }


    private void handleRegister() {
        String firstName = txtFirstName.getText();
        String lastName = txtLastName.getText();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> collection = database.getCollection("User");

            Document doc = new Document("firstname", firstName)
                    .append("lastname", lastName)
                    .append("username", username)
                    .append("password", password);

            collection.insertOne(doc);
            JOptionPane.showMessageDialog(this, "Registration Successful");
            toggleMode();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(80, 80, 80));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return textField;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    public static void main(String[] args) {
        new AdminLoginRegister();
    }
}

class AdminLogin extends JFrame {
    // Similar implementation for admin login
    public AdminLogin() {
        setTitle("Admin Login");
        // Add similar login components
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}