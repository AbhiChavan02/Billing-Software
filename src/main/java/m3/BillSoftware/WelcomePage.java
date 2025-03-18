package m3.BillSoftware;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class WelcomePage extends JFrame {
    public WelcomePage() {
        // Set FlatLaf theme
        FlatLightLaf.setup();
        setTitle("Welcome to Bill Software");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); 

        // Main Panel with Background Image
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon bgImage = loadImage("/images/back3.jpg");
                if (bgImage != null) {
                    g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    System.out.println("❌ Background image not found!");
                }
            }
        };
        
        mainPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // 🔵 Logo (Row 0)
        gbc.gridy = 0;
        JLabel logoLabel = createCircularLogo("/images/logo.jpg", 150);
        if (logoLabel != null) {
            mainPanel.add(logoLabel, gbc);		
        }

        // 🏷️ Title (Row 1)
        gbc.gridy = 1;
        JLabel title = new JLabel("RajLaxmi Jewellers", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 50));
        title.setForeground(Color.WHITE);
        mainPanel.add(title, gbc);

        // 🔘 Button Panel (Row 2)
        gbc.gridy = 2;
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 50, 50));
        buttonPanel.setOpaque(false);

        // Circular Buttons for Admin & Staff
        JButton btnAdmin = createStyledButton("Admin Login", "/images/admin1.jpg");
        JButton btnStaff = createStyledButton("Staff Login", "/images/staff.jpg");
        buttonPanel.add(btnAdmin);
        buttonPanel.add(btnStaff);

        // Add Button Panel Below Title
        mainPanel.add(buttonPanel, gbc);

        // Add Main Panel to Frame
        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Create Circular Logo for Main Logo, Admin & Staff
    private JLabel createCircularLogo(String imagePath, int size) {
        BufferedImage img = loadBufferedImage(imagePath);
        if (img == null) {
            return null;
        }

        // Create circular image
        BufferedImage circularImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circularImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(img, 0, 0, size, size, null);
        g2.dispose();

        return new JLabel(new ImageIcon(circularImg));
    }

    // Create Circular Styled Button with Image
    private JButton createStyledButton(String text, String imagePath) {
        BufferedImage img = loadBufferedImage(imagePath);
        if (img == null) {
            return new JButton(text); // Return normal button if image not found
        }

        // Convert Image to Circular
        int size = 100; // Set circular size for button icons
        BufferedImage circularImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circularImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(img, 0, 0, size, size, null);
        g2.dispose();

        // Create button with circular icon
        JButton button = new JButton(text, new ImageIcon(circularImg));
        button.setFont(new Font("Segoe UI", Font.BOLD, 22));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 63, 65, 0)); // Transparent background
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 83, 85, 100));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 63, 65, 0));
            }
        });

        button.addActionListener(e -> {
            dispose();
            if (text.equals("Admin Login")) {
                new AdminLoginRegister().setVisible(true);
            } else {
                new StaffLoginRegister().setVisible(true);
            }
        });

        return button;
    }

    // Helper method to load images as ImageIcon
    private ImageIcon loadImage(String path) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl == null) {
            System.out.println("❌ Image not found: " + path);
            return null;
        }
        return new ImageIcon(imageUrl);
    }

    // Helper method to load images as BufferedImage
    private BufferedImage loadBufferedImage(String path) {
        try {
            URL imageUrl = getClass().getResource(path);
            if (imageUrl == null) {
            	System.out.println(" Image not found: " + path);
                return null;
            }
            return ImageIO.read(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WelcomePage::new);
    }
}
