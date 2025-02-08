package m3.BillSoftware;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import java.io.*;
import java.net.URL;

public class SalesHistoryPanel extends JPanel {
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private Color backgroundColor = new Color(241, 242, 246);
    private Color formColor = Color.WHITE;

    public SalesHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(backgroundColor);
        createUI();
        fetchSalesData();
    }

    private void createUI() {
        tableModel = new DefaultTableModel(new Object[]{
            "Barcode", "Product Name", "Product Image", "Total Price", "Final Price",
            "Customer Name", "Seller", "Date", "Invoice"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return (column == 2) ? ImageIcon.class : Object.class;
            }
        };

        salesTable = new JTable(tableModel);
        salesTable.setRowHeight(70);
        salesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        salesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        salesTable.getColumnModel().getColumn(8).setPreferredWidth(100);

        // Set custom renderer and editor for the "Invoice" column (column index 8)
        salesTable.getColumnModel().getColumn(8).setCellRenderer(new ButtonCellRenderer());
        salesTable.getColumnModel().getColumn(8).setCellEditor(new ButtonCellEditor());

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(formColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnRefresh = createActionButton("Refresh", new Color(52, 152, 219));
        btnRefresh.addActionListener((ActionEvent e) -> {
            tableModel.setRowCount(0);
            fetchSalesData();
        });
        buttonPanel.add(btnRefresh);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchSalesData() {
        try (MongoClient mongoClient = MongoClients.create("mongodb+srv://abhijeetchavan212002:Abhi%40212002@cluster0.dkki2.mongodb.net/")) {
            MongoDatabase database = mongoClient.getDatabase("testDB");
            MongoCollection<Document> salesCollection = database.getCollection("Sales");
            MongoCollection<Document> productCollection = database.getCollection("Product");

            List<Document> salesList = salesCollection.find().into(new java.util.ArrayList<>());
            for (Document sale : salesList) {
                String productName = sale.getString("productName");
                Document product = productCollection.find(new Document("productName", productName)).first();

                String barcode = (product != null && product.containsKey("barcode")) ? product.getString("barcode") : "N/A";
                String productImagePath = (product != null && product.containsKey("productImagePath")) ? product.getString("productImagePath") : "";
                double totalPrice = getDoubleValue(sale, "totalPrice");
                double finalPrice = getDoubleValue(sale, "finalPrice");
                String customerName = sale.containsKey("customerName") ? sale.getString("customerName") : "N/A";
                String staff = sale.containsKey("staff") ? sale.getString("staff") : "Admin";
                String timestamp = sale.containsKey("timestamp") ? sale.getDate("timestamp").toString() : "N/A";

                ImageIcon productImage = loadProductImage(productImagePath);
                JButton downloadInvoiceButton = createActionButton("Download", new Color(52, 152, 219));
                downloadInvoiceButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        generateInvoice(barcode, productName, totalPrice, finalPrice, customerName, staff, timestamp, productImagePath);
                    }
                });

                tableModel.addRow(new Object[]{
                    barcode,
                    productName,
                    productImage,
                    totalPrice,
                    finalPrice,
                    customerName,
                    staff,
                    timestamp,
                    downloadInvoiceButton
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching sales records: " + e.getMessage());
        }
    }

    private void generateInvoice(String barcode, String productName, double totalPrice, double finalPrice, String customerName, String staff, String timestamp, String imageUrl) {
        try {
            // Calculate savings
            double savings = totalPrice - finalPrice;

            // Create a dialog for the invoice
            JDialog invoiceDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Invoice", true);
            invoiceDialog.setLayout(new BorderLayout());

            JPanel invoicePanel = new JPanel();
            invoicePanel.setLayout(new BoxLayout(invoicePanel, BoxLayout.Y_AXIS));
            invoicePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Add invoice details using JLabels
            invoicePanel.add(new JLabel("<html><h2>Invoice</h2></html>"));
            invoicePanel.add(new JLabel("Barcode: " + barcode));
            invoicePanel.add(new JLabel("Product: " + productName));
            invoicePanel.add(new JLabel("Total Price: " + totalPrice));
            invoicePanel.add(new JLabel("Final Price: " + finalPrice));
            invoicePanel.add(new JLabel("Savings: " + savings)); // Add savings here
            invoicePanel.add(new JLabel("Customer: " + customerName));
            invoicePanel.add(new JLabel("Seller: " + staff));
            invoicePanel.add(new JLabel("Date: " + timestamp));

            // Load product image
            JLabel imageLabel = new JLabel(loadProductImage(imageUrl));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            invoicePanel.add(Box.createVerticalStrut(10));
            invoicePanel.add(imageLabel);

            // Add "Download" button inside the invoice
            JButton downloadButton = createActionButton("Download", new Color(52, 152, 219));
            invoicePanel.add(Box.createVerticalStrut(10));
            invoicePanel.add(downloadButton);

            downloadButton.addActionListener(e -> {
                downloadInvoiceAsPDF(barcode, productName, totalPrice, finalPrice, customerName, staff, timestamp, imageUrl);
                JOptionPane.showMessageDialog(invoiceDialog, "Invoice Downloaded!", "Success", JOptionPane.INFORMATION_MESSAGE);
            });

            invoiceDialog.add(invoicePanel, BorderLayout.CENTER);

            // Close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> invoiceDialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            invoiceDialog.add(buttonPanel, BorderLayout.SOUTH);

            invoiceDialog.setSize(350, 450);
            invoiceDialog.setLocationRelativeTo(this);
            invoiceDialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating invoice: " + e.getMessage());
        }
    }


private void downloadInvoiceAsPDF(String barcode, String productName, double totalPrice, double finalPrice, String customerName, String staff, String timestamp, String imageUrl) {
    try {
        // Show dialog with "Print" and "E-Invoice" options
        String[] options = {"Print", "E-Invoice"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Select an option:",
            "Download Invoice",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        // Calculate savings
        double savings = totalPrice - finalPrice;

        // Create PDF document
        com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
        File file;

        if (choice == 1) { // E-Invoice (Save to file)
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save E-Invoice");

            fileChooser.setSelectedFile(new File(productName + "_E-Invoice.pdf"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }
            } else {
                return;
            }
        } else { // Print (Save temporarily)
            file = new File(System.getProperty("java.io.tmpdir"), productName + "_Invoice.pdf");
        }

        PdfWriter.getInstance(pdfDoc, new FileOutputStream(file));
        pdfDoc.open();

        pdfDoc.add(new Paragraph("Invoice"));
        pdfDoc.add(new Paragraph(" "));
        pdfDoc.add(new Paragraph("Barcode: " + barcode));
        pdfDoc.add(new Paragraph("Product Name: " + productName));
        pdfDoc.add(new Paragraph("Total Price: ₹" + totalPrice));
        pdfDoc.add(new Paragraph("Final Price: ₹" + finalPrice));
        pdfDoc.add(new Paragraph("Savings: ₹" + savings));
        pdfDoc.add(new Paragraph("Customer Name: " + customerName));
        pdfDoc.add(new Paragraph("Seller: " + staff));
        pdfDoc.add(new Paragraph("Date: " + timestamp));
        pdfDoc.add(new Paragraph(" "));
        pdfDoc.add(new Paragraph("Thank you for your business!"));

        pdfDoc.close();

        if (choice == 0) { // Print option selected
            printPDF(file); // Call the new print method
        } else {
            JOptionPane.showMessageDialog(this, "E-Invoice saved successfully: " + file.getAbsolutePath());
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage());
    }
}

// Method to print the PDF directly
private void printPDF(File file) {
    try {
        PDDocument document = PDDocument.load(file);
        PrinterJob job = PrinterJob.getPrinterJob();

        // Let user select a printer
        if (job.printDialog()) {
            job.setPageable(new PDFPageable(document));
            job.print();
            JOptionPane.showMessageDialog(this, "Printing started...");
        } else {
            JOptionPane.showMessageDialog(this, "Printing cancelled.");
        }

        document.close();
    } catch (IOException | PrinterException e) {
        JOptionPane.showMessageDialog(this, "Printing error: " + e.getMessage());
    }
}
    
    private ImageIcon loadProductImage(String imageUrl) {
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Image image = new ImageIcon(new URL(imageUrl)).getImage();
                Image scaledImage = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
        }
        return new ImageIcon(new ImageIcon("default-placeholder.png").getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
    }

    private double getDoubleValue(Document doc, String key) {
        if (doc.containsKey(key)) {
            Object value = doc.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        return 0.0;
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
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private class ButtonCellRenderer extends JPanel implements TableCellRenderer {
        private JButton downloadButton;

        public ButtonCellRenderer() {
            setLayout(new GridBagLayout());
            setOpaque(true);

            downloadButton = new JButton("Download PDF");
            downloadButton.setBackground(new Color(46, 204, 113)); // Green
            downloadButton.setForeground(Color.WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 5, 2, 5);
            gbc.gridx = 0;
            add(downloadButton, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    private class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JPanel panel;
        private JButton downloadButton;
        private int currentRow;

        public ButtonCellEditor() {
            panel = new JPanel(new GridBagLayout());
            downloadButton = new JButton("Download PDF");
            downloadButton.setBackground(new Color(46, 204, 113)); // Green
            downloadButton.setForeground(Color.WHITE);

            downloadButton.addActionListener(this);
            
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 5, 2, 5);
            gbc.gridx = 0;
            panel.add(downloadButton, gbc);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == downloadButton) {
                downloadProductPDF(currentRow);
            }
            fireEditingStopped();
        }
    }

    private void downloadProductPDF(int row) {
        String barcode = (String) tableModel.getValueAt(row, 0);
        String productName = (String) tableModel.getValueAt(row, 1);
        double totalPrice = (Double) tableModel.getValueAt(row, 3);
        double finalPrice = (Double) tableModel.getValueAt(row, 4);
        String customerName = (String) tableModel.getValueAt(row, 5);
        String staff = (String) tableModel.getValueAt(row, 6);
        String timestamp = (String) tableModel.getValueAt(row, 7);
        String imageUrl = ""; // Fetch image URL if needed

        // Calculate savings
        double savings = totalPrice - finalPrice;

        downloadInvoiceAsPDF(barcode, productName, totalPrice, finalPrice, customerName, staff, timestamp, imageUrl);
    }
}