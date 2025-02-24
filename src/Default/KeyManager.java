
package Default;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
/**
 *
 * @author lhevy
 */
public class KeyManager extends javax.swing.JFrame {
    private JPasswordField passwordField;
    private JDialog passwordDialog;
    private JButton resetButton;
    private JButton loginButton;

    /**
     * Creates new form KeyManager
     */
 public KeyManager() {
        initComponents();
        initButtons();
        displayKeys();
        startAutoRefresh();
        checkAndCreateKeyManagerTable(); // Check and create keymanagerpassword table if it doesn't exist
        checkAndCreateInitialPassword();
        setVisible(false);
        showPasswordDialog();
    }
 
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            KeyManager mainFrame = new KeyManager();
            mainFrame.setVisible(true);
            mainFrame.setLocationRelativeTo(null);
        });
    }
    
    private void checkAndCreateKeyManagerTable() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/Kronosphere", "root", "kronosphereTSM")) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, "keymanagerpassword", null);

            if (!resultSet.next()) {
                // Table does not exist, create it
                Statement statement = connection.createStatement();
                String createTableQuery = "CREATE TABLE keymanagerpassword (id INT AUTO_INCREMENT PRIMARY KEY, user_type VARCHAR(50), password VARCHAR(255))";
                statement.executeUpdate(createTableQuery);
                JOptionPane.showMessageDialog(this, "Table 'keymanagerpassword' created successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
   private void initButtons() {
        resetButton = new JButton("Reset Password");
        loginButton = new JButton("Login");

        resetButton.setBackground(Color.BLUE);
        resetButton.setForeground(Color.WHITE);
        resetButton.addActionListener(this::resetButtonActionPerformed);

        loginButton.setBackground(Color.BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(this::loginButtonActionPerformed);
    }

  private void startAutoRefresh() {
        int refreshInterval = 5000;
        Timer timer = new Timer(refreshInterval, e -> {
            displayKeys();
        });
        timer.start();
    }
  
private void displayKeys() {
    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/Kronosphere", "root", "kronosphereTSM")) {
        Statement statement = connection.createStatement();
        String query = "SELECT File_Name, Encryption_Key FROM information_files";
        ResultSet resultSet = statement.executeQuery(query);

        JTable table = new JTable(buildTableModel(resultSet));
        JScrollPane scrollPane = new JScrollPane(table);

        // Set up constraints for centering the table within the frame
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        getContentPane().removeAll(); // Clear existing content
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().add(scrollPane, gbc);
        revalidate(); // Refresh the frame layout
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    // Method to convert ResultSet to TableModel
    private DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        // Get column names
        int columnCount = 2; // Only File_Name and Encryption_Key columns
        String[] columnNames = {"File Name", "Encryption Key"};

        // Get data rows
        Object[][] data = new Object[100][columnCount]; // Assuming a maximum of 100 rows initially
        int row = 0;
        while (resultSet.next()) {
            data[row][0] = resultSet.getObject("File_Name");
            data[row][1] = resultSet.getObject("Encryption_Key");
            row++;
        }

        return new DefaultTableModel(data, columnNames);
    }
    
private void showPasswordDialog() {
    passwordDialog = new JDialog(this, "Password", Dialog.ModalityType.APPLICATION_MODAL);
    passwordDialog.setUndecorated(true); // Removes the border and title bar buttons

    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
    inputPanel.setBackground(Color.DARK_GRAY);

    passwordField = new JPasswordField(20); // Set desired columns for password field
    passwordField.setEchoChar('*'); // Mask input
    JLabel passwordLabel = new JLabel("Enter Password: ");
    passwordLabel.setForeground(Color.WHITE);

    JPanel passwordFieldPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    passwordFieldPanel.setBackground(Color.DARK_GRAY);
    passwordFieldPanel.add(passwordLabel);
    passwordFieldPanel.add(passwordField);

    inputPanel.add(Box.createVerticalStrut(5)); // Reduce vertical spacing
    inputPanel.add(passwordFieldPanel);
    inputPanel.add(Box.createVerticalStrut(5)); // Reduce vertical spacing

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.setBackground(Color.DARK_GRAY);

    // Add login and reset buttons
    loginButton = new JButton("Login");
    resetButton = new JButton("Reset Password");

    loginButton.setBackground(Color.BLUE);
    loginButton.setForeground(Color.WHITE);
    loginButton.addActionListener(this::loginButtonActionPerformed);

    resetButton.setBackground(Color.BLUE);
    resetButton.setForeground(Color.WHITE);
    resetButton.addActionListener(this::resetButtonActionPerformed);

    buttonPanel.add(loginButton);
    buttonPanel.add(Box.createHorizontalStrut(5)); // Reduce horizontal spacing
    buttonPanel.add(resetButton);
    buttonPanel.add(Box.createHorizontalStrut(5)); // Reduce horizontal spacing

    // Add an exit button
    JButton exitButton = new JButton("Exit");
    exitButton.setBackground(Color.RED);
    exitButton.setForeground(Color.WHITE);
    exitButton.addActionListener(e -> {
        passwordDialog.dispose(); // Close the password dialog
        disposeKeyManager(); // Close the KeyManager frame
    });
    buttonPanel.add(exitButton);

    inputPanel.add(buttonPanel);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(Color.DARK_GRAY);
    mainPanel.add(inputPanel, BorderLayout.CENTER);

    passwordDialog.add(mainPanel);
    passwordDialog.setSize(300, 150);
    passwordDialog.setLocationRelativeTo(null); // Center the dialog
    passwordDialog.setVisible(true);
}




private void disposeKeyManager() {
    EventQueue.invokeLater(() -> {
        setVisible(false); // Hide the KeyManager frame
        dispose(); // Dispose of the KeyManager frame
    });
}




private void loginButtonActionPerformed(ActionEvent evt) {
    String password = String.valueOf(passwordField.getPassword());

    try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/Kronosphere", "root", "kronosphereTSM")) {
        String query = "SELECT * FROM keymanagerpassword WHERE password = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, password);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            passwordDialog.dispose(); // Close password dialog on successful login

            // Show the KeyManager UI
            setVisible(true);

            // Refresh the table upon successful login
            displayKeys();
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect password. Try again.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private void resetButtonActionPerformed(ActionEvent evt) {
    String currentPassword = promptForPassword("Enter Current Password:");
    if (currentPassword != null) {
        String newPassword = promptForPassword("Enter New Password:");

        if (newPassword != null) {
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/Kronosphere", "root", "kronosphereTSM")) {
                String query = "SELECT * FROM keymanagerpassword WHERE password = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, currentPassword);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    // Update the password
                    String updateQuery = "UPDATE keymanagerpassword SET password = ? WHERE password = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                    updateStatement.setString(1, newPassword);
                    updateStatement.setString(2, currentPassword);
                    updateStatement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Password updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect current password. Reset failed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}



private String promptForPassword(String message) {
    JPasswordField passwordField = new JPasswordField(20);
    Object[] obj = { message, passwordField };
    int option = JOptionPane.showConfirmDialog(this, obj, "Password Setup", JOptionPane.OK_CANCEL_OPTION);

    if (option == JOptionPane.OK_OPTION) {
        return String.valueOf(passwordField.getPassword());
    }
    return null;
}

    
private void checkAndCreateInitialPassword() {
    try {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/Kronosphere", "root", "kronosphereTSM")) {
            Statement statement = connection.createStatement();
            String query = "SELECT COUNT(*) FROM keymanagerpassword";
            ResultSet resultSet = statement.executeQuery(query);
            resultSet.next();
            int count = resultSet.getInt(1);
            if (count == 0) {
                String userType = "Secretary";

                // No existing password, prompt the Secretary to create a new one
                String newPassword = promptForPassword("Create New Password for Secretary:");
                if (newPassword != null) {
                    String insertQuery = "INSERT INTO keymanagerpassword (user_type, password) VALUES (?, ?)";
                    PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                    insertStatement.setString(1, userType);
                    insertStatement.setString(2, newPassword);
                    insertStatement.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Password for Secretary set successfully!");
                }
            }
            // Close the connection
        }
    } catch (SQLException e) {
        e.printStackTrace();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        Keys = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));

        Keys.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "File Name", "Encryption Key"
            }
        ));
        jScrollPane1.setViewportView(Keys);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Keys;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
