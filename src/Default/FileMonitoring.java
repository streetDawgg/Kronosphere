/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JInternalFrame.java to edit this template
 */
package Default;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JFrame;
import javax.swing.Timer;
import java.sql.Timestamp;


/**
 *
 * @author pc
 */
public class FileMonitoring extends javax.swing.JInternalFrame {
   


     public FileMonitoring() {
        initComponents();
        startAutoRefresh();
        initializeSearchBar();
        displayEncryptedFilesFromDatabase();
        KeyManager.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                KeyManager keyManagerFrame = new KeyManager();
                keyManagerFrame.setVisible(true);
                keyManagerFrame.setLocationRelativeTo(null);
                keyManagerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            }
        });
                
     }



// ... (other import statements)

private void displayEncryptedFilesFromDatabase() {
    DefaultTableModel tableModel = new DefaultTableModel();
    tableModel.addColumn("Encrypted Files");
    tableModel.addColumn("Path");
    tableModel.addColumn("Size");
    tableModel.addColumn("Last Modified"); // Updated column name to Timestamp

    // JDBC connection parameters
    String url = "jdbc:mysql://localhost:3307/Kronosphere"; // Update URL with your database details
    String username = "root";
    String password = "kronosphereTSM";

    try (Connection connection = DriverManager.getConnection(url, username, password)) {
        String sql = "SELECT File_Name, Path, File_Size, Date FROM information_files";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fileName = resultSet.getString("File_Name");
                String path = resultSet.getString("Path");
                long fileSize = resultSet.getLong("File_Size"); // Retrieve the file size in bytes

                // Format file size
                String formattedSize = formatFileSize(fileSize);

                // Fetching timestamp from the database
                Timestamp timestamp = resultSet.getTimestamp("Date");
                String formattedTimestamp = timestamp.toString(); // Convert timestamp to string

                tableModel.addRow(new Object[]{fileName, path, formattedSize, formattedTimestamp});
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        // Handle database connection issues or SQL errors
    }

    EncryptedTable.setModel(tableModel);
}

private String formatFileSize(long bytes) {
    final long kiloBytes = 1024;
    final long megaBytes = kiloBytes * 1024;
    final long gigaBytes = megaBytes * 1024;

    if (bytes < kiloBytes) {
        return bytes + " B";
    } else if (bytes < megaBytes) {
        return String.format("%.2f KB", (double) bytes / kiloBytes);
    } else if (bytes < gigaBytes) {
        return String.format("%.2f MB", (double) bytes / megaBytes);
    } else {
        return String.format("%.2f GB", (double) bytes / gigaBytes);
    }
}



     

private void initializeSearchBar() {
        searchbar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchTable();
            }
        });
    }

    private void searchTable() {
        String searchText = searchbar.getText().toLowerCase();
        DefaultTableModel tableModel = (DefaultTableModel) EncryptedTable.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        EncryptedTable.setRowSorter(sorter);

        if (searchText.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            ArrayList<RowFilter<Object, Object>> filters = new ArrayList<>();
            filters.add(RowFilter.regexFilter("(?i)" + searchText)); // Case-insensitive search
            RowFilter<Object, Object> rowFilter = RowFilter.andFilter(filters);
            sorter.setRowFilter(rowFilter);
        }
    }

  

    private byte[] decryptFile(File inputFile, String base64Key) {
    try {
        // Read input file into byte array
        byte[] inputData = Files.readAllBytes(inputFile.toPath());

        // Decode the base64-encoded key to obtain the actual key bytes
        byte[] keyData = Base64.getDecoder().decode(base64Key);

        // Split input data into initialization vector and encrypted data
        byte[] ivData = Arrays.copyOfRange(inputData, 0, 16);
        byte[] encryptedData = Arrays.copyOfRange(inputData, 16, inputData.length);

        // Create cipher and decrypt data
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyData, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivData);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        return decryptedData;
    } catch (Exception ex) {
        System.err.println("Error during decryption: " + ex.getMessage());
        ex.printStackTrace();
        return null; // Return null in case of decryption failure
    }
}
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        searchbar = new javax.swing.JTextField();
        KeyManager = new javax.swing.JLabel();
        Decrypt = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        EncryptedTable = new javax.swing.JTable();

        setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setIconifiable(true);
        setResizable(true);

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setPreferredSize(new java.awt.Dimension(738, 477));

        searchbar.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        searchbar.setDragEnabled(true);
        searchbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchbarActionPerformed(evt);
            }
        });

        KeyManager.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 18)); // NOI18N
        KeyManager.setForeground(new java.awt.Color(255, 255, 255));
        KeyManager.setText("Key Manager");

        Decrypt.setBackground(new java.awt.Color(0, 153, 153));
        Decrypt.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Decrypt.setForeground(new java.awt.Color(255, 255, 255));
        Decrypt.setText("Decrypt");
        Decrypt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DecryptActionPerformed(evt);
            }
        });

        EncryptedTable.setBackground(new java.awt.Color(102, 102, 102));
        EncryptedTable.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        EncryptedTable.setForeground(new java.awt.Color(255, 255, 255));
        EncryptedTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(EncryptedTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(searchbar, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(Decrypt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(KeyManager)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(searchbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Decrypt)
                    .addComponent(KeyManager))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 742, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void searchbarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchbarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_searchbarActionPerformed

    private void DecryptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DecryptActionPerformed
   int[] selectedRows = EncryptedTable.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(this, "Please select file(s) to decrypt.");
        return; // Return if no file is selected
    }

    String encryptionKey = JOptionPane.showInputDialog(this, "Enter encryption key:");

    if (encryptionKey == null || encryptionKey.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a valid encryption key.");
        return;
    }

    String projectDirectory = System.getProperty("user.dir");
    String encryptedFilesPath = projectDirectory + "/Kronosphere/Encrypted Files";
    String decryptedFilesPath = projectDirectory + "/Kronosphere/Decrypted Files";

    File decryptedFilesDirectory = new File(decryptedFilesPath);

    if (!decryptedFilesDirectory.exists()) {
        boolean created = decryptedFilesDirectory.mkdirs();
        if (!created) {
            System.err.println("Failed to create Decrypted Files folder in Kronosphere.");
            return;
        }
    }

    for (int row : selectedRows) {
        String fileName = (String) EncryptedTable.getValueAt(row, 0); // Assuming the filename is in the first column

        String encryptedFilePath = encryptedFilesPath + File.separator + fileName;

        byte[] decryptedData;
        try {
            decryptedData = decryptFile(new File(encryptedFilePath), encryptionKey);

            if (decryptedData != null) {
                File decryptedFile = new File(decryptedFilesPath, fileName);
                try (FileOutputStream fos = new FileOutputStream(decryptedFile)) {
                    fos.write(decryptedData);
                }

                // Delete the original encrypted file
                File encryptedFile = new File(encryptedFilePath);
                if (encryptedFile.delete()) {
                    System.out.println("Encrypted file deleted successfully.");

                    // Remove file from the database
                    removeFromDatabase(fileName);

                    // Update the table after decryption by re-displaying the encrypted files
                    displayEncryptedFilesFromDatabase();

                    JOptionPane.showMessageDialog(this, "Selected file(s) decrypted and moved to Decrypted Files folder successfully!");
                } else {
                    System.err.println("Failed to delete encrypted file.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Decryption failed. Incorrect encryption key or decryption error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error decrypting file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    }//GEN-LAST:event_DecryptActionPerformed

private void removeFromDatabase(String filename) {
     String url = "jdbc:mysql://localhost:3307/Kronosphere"; // Update URL with your database details
    String username = "root";
    String password = "kronosphereTSM";

    try (Connection connection = DriverManager.getConnection(url, username, password)) {
        Statement statement = connection.createStatement();

        // Execute a SQL DELETE query to remove the file entry
        String deleteQuery = "DELETE FROM information_files WHERE File_Name = '" + filename + "'";
        statement.executeUpdate(deleteQuery);

        System.out.println("File removed from information_files  database.");
        
        // Close resources
        statement.close();
        connection.close();
    } catch (SQLException ex) {
        System.err.println("Error removing file from Recycle Bin database: " + ex.getMessage());
    }
}
private void startAutoRefresh() {
        int refreshInterval = 10000; // Refresh interval in milliseconds (e.g., every 10 seconds)
        Timer timer = new Timer(refreshInterval, e -> {
            displayEncryptedFilesFromDatabase(); // Refresh table contents
        });
        timer.start();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Decrypt;
    private javax.swing.JTable EncryptedTable;
    private javax.swing.JLabel KeyManager;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField searchbar;
    // End of variables declaration//GEN-END:variables
}
