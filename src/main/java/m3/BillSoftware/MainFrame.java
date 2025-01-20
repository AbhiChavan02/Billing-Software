package m3.BillSoftware;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author abhij
 */
public class MainFrame extends javax.swing.JFrame {
    
    public MainFrame() {
        setTitle("Jewelry POS System - Main Menu");
        setSize(800, 400);
        setLayout(new GridLayout(4, 1));

        JButton btnRegisterProduct = new JButton("Product Registration");
        JButton btnProcessSales = new JButton("Process Sales");
        JButton btnRegisteredProduct = new JButton("Registered Product");
        JButton btnLogout = new JButton("Logout");

        add(btnRegisterProduct);
        add(btnProcessSales);
        add(btnRegisteredProduct);
        add(btnLogout);

        // Product Registration Button - open ProductRegistration
        btnRegisterProduct.addActionListener(e -> {
            dispose();  // Close MainFrame window
            new ProductRegistration(this).setVisible(true);  // Open ProductRegistration window and pass MainFrame as reference
        });

        // Process Sales Button - open ProcessSales
        btnProcessSales.addActionListener(e -> {
            dispose();  // Close MainFrame
            new ProcessSales(null).setVisible(true);  // Open ProcessSales
        });

        // Registered Product Button - open RegisteredProducts
        btnRegisteredProduct.addActionListener(e -> {
            dispose();  // Close MainFrame
            new RegisteredProducts().setVisible(true);  // Open RegisteredProducts
        });

        // Logout Button - open Login and close MainFrame
        btnLogout.addActionListener(e -> {
            dispose();  // Close MainFrame
            new Login().setVisible(true);  // Open Login screen
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>                        

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    // End of variables declaration                   
    
}
