package m3.BillSoftware;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class StaffRegisteredProductsPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> searchCriteria;
    private JButton btnRefresh, btnSearch;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;
    private MongoClient mongoClient;
    private ConcurrentHashMap<String, ImageIcon> imageCache = new ConcurrentHashMap<>();

    public StaffRegisteredProductsPanel() {
        initializeMongoClient();
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        setPreferredSize(new Dimension(1200, 800));
        createUI();
        loadProductData();
    }

    private void initializeMongoClient() {
        try {
            mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + e.getMessage());
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    private class ImageCellRenderer extends JLabel implements TableCellRenderer {
        private final int IMAGE_SIZE = 60;

        public ImageCellRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                ImageIcon icon = (ImageIcon) value;
                setIcon(icon);
            } else {
                setIcon(null);
            }
            return this;
        }
    }

    private void createUI() {
        tableModel = new DefaultTableModel(new Object[]{"Barcode", "Product Name", "Image", "Category", "Rate/Price", "Stock Quantity"}, 0);
        
        productTable = new JTable(tableModel);
        productTable.setRowHeight(70);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (column == 4) {
                    String category = (String) tableModel.getValueAt(row, 3);
                    if ("Emetation".equalsIgnoreCase(category)) {
                        setText(value != null ? String.format("₹%.2f", value) : "N/A");
                    } else {
                        setText(value != null ? String.format("%.2f g", value) : "N/A");
                    }
                }
                return this;
            }
        };

        for (int i = 0; i < productTable.getColumnCount(); i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        productTable.getColumnModel().getColumn(2).setCellRenderer(new ImageCellRenderer());
        productTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setPreferredSize(new Dimension(1150, 700));

        btnRefresh = createActionButton("Refresh", new Color(46, 204, 113));
        btnSearch = createActionButton("Search", new Color(241, 196, 15));

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchCriteria = new JComboBox<>(new String[]{"Barcode", "Product Name", "Category", "Stock Quantity"});
        searchCriteria.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnRefresh.addActionListener(e -> loadProductData());
        btnSearch.addActionListener(e -> handleSearch());

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(formColor);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(txtSearch);
        searchPanel.add(searchCriteria);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    private void loadProductData() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    MongoDatabase database = mongoClient.getDatabase("testDB");
                    MongoCollection<Document> collection = database.getCollection("Product");
                    tableModel.setRowCount(0); // Clear existing data

                    for (Document product : collection.find()) {
                        String barcode = product.getString("barcode");
                        String productName = product.getString("productName");
                        String category = product.getString("category");
                        Object rateValue = getRateValue(product, category);
                        Integer stockQuantity = product.getInteger("stockQuantity");
                        String imageUrl = product.getString("productImagePath");

                        SwingUtilities.invokeLater(() -> {
                            tableModel.addRow(new Object[]{
                                barcode != null ? barcode : "N/A",
                                productName != null ? productName : "N/A",
                                createPlaceholderImage(),
                                category != null ? category : "N/A",
                                rateValue,
                                stockQuantity != null ? stockQuantity : 0
                            });
                        });

                        loadImageAsync(imageUrl, tableModel.getRowCount() - 1);
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(StaffRegisteredProductsPanel.this, "Error loading product data: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
    }

    private ImageIcon createPlaceholderImage() {
        return new ImageIcon(new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB));
    }

    private void loadImageAsync(String imageUrl, int rowIndex) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                if (imageCache.containsKey(imageUrl)) {
                    return imageCache.get(imageUrl);
                }
                
                try {
                    ImageIcon original = new ImageIcon(new URL(imageUrl));
                    Image scaled = original.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(scaled);
                    imageCache.put(imageUrl, scaledIcon);
                    return scaledIcon;
                } catch (Exception e) {
                    return createPlaceholderImage();
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    tableModel.setValueAt(icon, rowIndex, 2);
                } catch (Exception e) {
                    // Handle exception
                }
            }
        }.execute();
    }

    private Object getRateValue(Document product, String category) {
        if ("Emetation".equalsIgnoreCase(category)) {
            return product.getDouble("salesPrice");
        } else {
            return product.getDouble("grams");
        }
    }

    private void handleSearch() {
        String searchValue = txtSearch.getText().trim();
        String criteria = (String) Objects.requireNonNull(searchCriteria.getSelectedItem());
        
        if (searchValue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a value to search.");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    MongoDatabase database = mongoClient.getDatabase("testDB");
                    MongoCollection<Document> collection = database.getCollection("Product");

                    Document query = new Document();
                    switch (criteria) {
                        case "Barcode":
                            query.append("barcode", searchValue);
                            break;
                        case "Product Name":
                            query.append("productName", new Document("$regex", searchValue).append("$options", "i"));
                            break;
                        case "Category":
                            query.append("category", new Document("$regex", searchValue).append("$options", "i"));
                            break;
                        case "Stock Quantity":
                            try {
                                query.append("stockQuantity", Integer.parseInt(searchValue));
                            } catch (NumberFormatException e) {
                                SwingUtilities.invokeLater(() -> 
                                    JOptionPane.showMessageDialog(StaffRegisteredProductsPanel.this, "Invalid input for Stock Quantity."));
                                return null;
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid search criteria.");
                    }

                    tableModel.setRowCount(0);
                    for (Document product : collection.find(query)) {
                        String category = product.getString("category");
                        SwingUtilities.invokeLater(() -> {
                            tableModel.addRow(new Object[]{
                                product.getString("barcode"),
                                product.getString("productName"),
                                createPlaceholderImage(),
                                category,
                                getRateValue(product, category),
                                product.getInteger("stockQuantity")
                            });
                        });

                        loadImageAsync(product.getString("productImagePath"), tableModel.getRowCount() - 1);
                    }

                    if (tableModel.getRowCount() == 0) {
                        SwingUtilities.invokeLater(() -> 
                            JOptionPane.showMessageDialog(StaffRegisteredProductsPanel.this, "No products found matching the search criteria."));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(StaffRegisteredProductsPanel.this, "Error performing search: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bgColor); }
        });
        return btn;
    }
}