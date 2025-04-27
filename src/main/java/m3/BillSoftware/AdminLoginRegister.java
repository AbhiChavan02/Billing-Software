package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class AdminLoginRegister extends JFrame {
    private JPanel mainPanel, formPanel, buttonPanel;
    private JTextField txtFirstName, txtLastName, txtUsername;
    private JPasswordField txtPassword;
    private JButton btnRegister, btnToggle, btnLogin;
    private JLabel lblFirstName, lblLastName, headerLabel, imageLabel;
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

        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(40, 58, 82), 2));
        formPanel.setPreferredSize(new Dimension(600, 600));

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.anchor = GridBagConstraints.CENTER;
        gbcMain.weightx = gbcMain.weighty = 1;
        mainPanel.add(formPanel, gbcMain);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        try {
            Image image = ImageIO.read(getClass().getResource("/images/admin1.jpg"));
            image = image.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(image));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
            headerPanel.add(imageLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            imageLabel = new JLabel("LOGO");
            imageLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            headerPanel.add(imageLabel, BorderLayout.CENTER);
        }

        headerLabel = new JLabel("Staff Login", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));
        headerPanel.add(headerLabel, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(headerPanel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        formPanel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = createTextField();
        formPanel.add(txtUsername, gbc);

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

        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            void toggleLoginButton() {
                btnLogin.setVisible(isLoginMode && txtPassword.getPassword().length > 0);
            }

            public void insertUpdate(DocumentEvent e) { toggleLoginButton(); }
            public void removeUpdate(DocumentEvent e) { toggleLoginButton(); }
            public void changedUpdate(DocumentEvent e) { toggleLoginButton(); }
        });

        gbc.gridy = 3;
        gbc.gridx = 0;
        lblFirstName = createLabel("First Name:");
        formPanel.add(lblFirstName, gbc);
        gbc.gridx = 1;
        txtFirstName = createTextField();
        formPanel.add(txtFirstName, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        lblLastName = createLabel("Last Name:");
        formPanel.add(lblLastName, gbc);
        gbc.gridx = 1;
        txtLastName = createTextField();
        formPanel.add(txtLastName, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegister = createButton("Register", new Color(82, 196, 26));
        btnLogin = createButton("Login", new Color(46, 204, 113));
        btnLogin.setVisible(false);
        btnToggle = createButton("Switch to Login", new Color(24, 144, 255));

        btnToggle.setPreferredSize(new Dimension(200, 40));
        btnToggle.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2, true));

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnLogin);
        formPanel.add(buttonPanel, gbc);

        gbc.gridy = 6;
        JPanel togglePanel = new JPanel(new BorderLayout());
        JLabel loginHint = new JLabel("You have an account? Then click below:");
        loginHint.setHorizontalAlignment(JLabel.CENTER);
        loginHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginHint.setForeground(new Color(52, 73, 94));
        togglePanel.add(loginHint, BorderLayout.NORTH);
        togglePanel.add(btnToggle, BorderLayout.CENTER);
        formPanel.add(togglePanel, gbc);

        gbc.gridy = 7;
        JLabel switchPanel = new JLabel("Change to Staff Panel");
        switchPanel.setForeground(new Color(41, 128, 185));
        switchPanel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        switchPanel.setHorizontalAlignment(SwingConstants.CENTER);
        switchPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formPanel.add(switchPanel, gbc);

        switchPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                new StaffLoginRegister().setVisible(true);
            }
        });

        btnToggle.addActionListener(e -> toggleMode());
        btnRegister.addActionListener(e -> handleRegister());
        btnLogin.addActionListener(e -> handleLogin());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;

        lblFirstName.setVisible(!isLoginMode);
        lblLastName.setVisible(!isLoginMode);
        txtFirstName.setVisible(!isLoginMode);
        txtLastName.setVisible(!isLoginMode);

        btnRegister.setVisible(!isLoginMode);
        btnLogin.setVisible(isLoginMode && txtPassword.getPassword().length > 0);

        btnToggle.setText(isLoginMode ? "Switch to Register" : "Switch to Login");
        headerLabel.setText(isLoginMode ? "Admin Login" : "Admin Register");
        formPanel.setBorder(BorderFactory.createLineBorder(
                isLoginMode ? new Color(40, 58, 82) : new Color(52, 152, 219), 2));
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
                new ProductRegistration(username, username, username).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void handleRegister() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> collection = database.getCollection("User");

            Document doc = new Document("firstname", txtFirstName.getText())
                    .append("lastname", txtLastName.getText())
                    .append("username", txtUsername.getText())
                    .append("password", new String(txtPassword.getPassword()));

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
                new EmptyBorder(5, 10, 5, 10)));
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
        SwingUtilities.invokeLater(() -> new AdminLoginRegister());
    }
}