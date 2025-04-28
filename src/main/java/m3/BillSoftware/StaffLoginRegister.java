package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StaffLoginRegister extends JFrame {
    private JPanel mainPanel, formPanel;
    private JTextField txtFirstName, txtLastName, txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnRegister, btnToggle, btnAdmin;
    private JLabel lblFirstName, lblLastName, headerLabel, imageLabel;
    private boolean isLoginMode = true;

    public StaffLoginRegister() {
        setTitle("Staff Login & Register");
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

        // Form Panel with Increased Width and Height to 700px
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(new Color(40, 58, 82), 2));
        formPanel.setPreferredSize(new Dimension(700, 700)); // Set to 700px width and height

        GridBagConstraints gbcMain = new GridBagConstraints();
        gbcMain.anchor = GridBagConstraints.CENTER;
        gbcMain.gridx = 0;
        gbcMain.gridy = 0;
        gbcMain.weightx = 0;
        gbcMain.weighty = 0;
        mainPanel.add(formPanel, gbcMain);

        // Header Image
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        ImageIcon headerImage = new ImageIcon(getClass().getResource("/images/staff.jpg")); // Update with your image path
        Image image = headerImage.getImage(); // Get the Image object from the ImageIcon
        Image resizedImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH); // Resize to width 150px and height 150px
        ImageIcon resizedImageIcon = new ImageIcon(resizedImage);
        imageLabel = new JLabel(resizedImageIcon);
        formPanel.add(imageLabel, gbc);

        // Header Text
        headerLabel = new JLabel("Staff Register", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(40, 58, 82));
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        formPanel.add(headerLabel, gbc);

        // Username
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = createTextField();
        formPanel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 3;
        gbc.gridx = 0;
        formPanel.add(createLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);  // Increased width
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
        formPanel.add(txtPassword, gbc);

        // First Name
        gbc.gridy = 4;
        gbc.gridx = 0;
        lblFirstName = createLabel("First Name:");
        formPanel.add(lblFirstName, gbc);
        gbc.gridx = 1;
        txtFirstName = createTextField();
        formPanel.add(txtFirstName, gbc);

        // Last Name
        gbc.gridy = 5;
        gbc.gridx = 0;
        lblLastName = createLabel("Last Name:");
        formPanel.add(lblLastName, gbc);
        gbc.gridx = 1;
        txtLastName = createTextField();
        formPanel.add(txtLastName, gbc);

        // Buttons (Centered)
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegister = createButton("Register", new Color(46, 204, 113));
        btnLogin = createButton("Login", new Color(46, 204, 113));
        btnLogin.setVisible(false);
        btnToggle = createButton("Switch to Login", new Color(0, 0, 255));

        buttonPanel.add(btnRegister);
        buttonPanel.add(btnLogin);
        formPanel.add(buttonPanel, gbc);

        // Already have an account text and toggle button
        gbc.gridy = 7;
        JPanel togglePanel = new JPanel(new BorderLayout());
        JLabel loginHint = new JLabel("You have already an account?");
        loginHint.setHorizontalAlignment(JLabel.CENTER);
        loginHint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginHint.setForeground(new Color(52, 73, 94));
        togglePanel.add(loginHint, BorderLayout.NORTH);
        togglePanel.add(btnToggle, BorderLayout.CENTER);
        formPanel.add(togglePanel, gbc);

     // Admin Login Button
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        btnAdmin = createButton("Change into Admin", null); // Background color removed
        btnAdmin.setForeground(new Color(41, 128, 185)); // Set text color to blue
        formPanel.add(btnAdmin, gbc);

        // Action Listeners
        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> handleRegister());
        btnToggle.addActionListener(e -> toggleMode());
        btnAdmin.addActionListener(e -> {
            dispose();
            new AdminLoginRegister().setVisible(true); 
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
        headerLabel.setText(isLoginMode ? "Staff Login" : "Staff Register");
        
        // Change border color
        Color borderColor = isLoginMode ? new Color(40, 58, 82) : new Color(52, 152, 219);
        formPanel.setBorder(BorderFactory.createLineBorder(borderColor, 2));

        // Refresh the panel after toggling
        formPanel.revalidate();
        formPanel.repaint();
    }

    private void handleLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> collection = database.getCollection("Staff");

            Document user = collection.find(new Document("username", username).append("password", password)).first();
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Login Successful");
                dispose();
                new StaffDashboard(username).setVisible(true);
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
            MongoCollection<Document> collection = database.getCollection("Staff");

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
        if (bgColor != null) {
            btn.setBackground(bgColor); // Only set background color if not null
        }
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    public static void main(String[] args) {
        new StaffLoginRegister();
    }
}