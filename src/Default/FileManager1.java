
package Default;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FileUtils;

public class FileManager1 extends javax.swing.JInternalFrame { // Declare root node outside the method
    // Declare root node outside the method
private DefaultListModel<String> listModel = new DefaultListModel<>();


    public FileManager1() {
    initComponents();
    TextFieldPath.setEnabled(false);
    displayFileSystemRoots();
    jTable1.setModel(new DefaultTableModel());
    jTable1.setEnabled(true); // Enable the JTable initially
    jTable1.setAutoCreateRowSorter(true);
    jTree1.setCellRenderer(new FileManager1.FileTreeCellRenderer());
    jTree1.setRootVisible(false);
    jTree1.expandRow(0);
    initializeRadioFileState();
    initializeRadioDirectoryState();
    CreateDatabaseAndTable();
    // Start monitoring the desktop directory, for example
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    File desktopDir = fileSystemView.getHomeDirectory();
    startFileMonitoring(desktopDir);

    // Display contents of the desktop directory
    displayDirectoryContents(desktopDir);
     Searchbar.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            searchFiles();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            searchFiles();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            searchFiles();
        }
    });
     
    }

    
private void displayFileSystemRoots() {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(); // No name needed for the root

    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    File[] roots = fileSystemView.getRoots(); // Get all root directories

    // Populate root directories directly to the tree
    for (File rootFile : roots) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootFile);
        root.add(rootNode);

        traverseSubfolders(rootNode, rootFile); // Populate subfolders lazily
    }

    // Set the model for the tree
    jTree1.setModel(new DefaultTreeModel(root));

    // Add tree expansion listener to load subfolders lazily
    jTree1.addTreeExpansionListener(new TreeExpansionListener() {
        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            TreePath path = event.getPath();
            DefaultMutableTreeNode expandedNode = (DefaultMutableTreeNode) path.getLastPathComponent();

            // Check if the node contains a dummy node (for lazy loading)
            if (expandedNode.getChildCount() == 1 &&
                    ((DefaultMutableTreeNode) expandedNode.getFirstChild()).getUserObject() instanceof Boolean) {

                expandedNode.removeAllChildren(); // Remove the dummy node

                // Load subfolders lazily for the expanded node
                File expandedFile = (File) expandedNode.getUserObject();
                traverseSubfolders(expandedNode, expandedFile);

                // Update the tree UI
                ((DefaultTreeModel) jTree1.getModel()).reload(expandedNode);
            }
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            // Not needed for lazy loading
        }
    });
    // Add tree selection listener
    jTree1.addTreeSelectionListener((javax.swing.event.TreeSelectionEvent evt) -> {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (selectedNode != null) {
            Object userObject = selectedNode.getUserObject();
            if (userObject instanceof File selectedFile) {
                if (selectedFile.isDirectory()) {
                    displayDirectoryContents(selectedFile);
                }
            }
        }
    });

    // Set the "Path" TextField to the selected file path in the tree
    jTree1.addTreeSelectionListener(e -> {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (selectedNode != null) {
            Object userObject = selectedNode.getUserObject();
            if (userObject instanceof File) {
                File selectedFile = (File) userObject;
                // Set the selected file path to the "Path" TextField
                TextFieldPath.setText(selectedFile.getAbsolutePath());
            }
        }
    });
    
    // Set the display name and icon to the "File" JLabel based on the selected file
    jTree1.addTreeSelectionListener(e -> {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (selectedNode != null) {
            Object userObject = selectedNode.getUserObject();
            if (userObject instanceof File) {
                File selectedFile = (File) userObject;
                ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(selectedFile);
                String displayName = FileSystemView.getFileSystemView().getSystemDisplayName(selectedFile);

                // Set the display name and icon to the "File" JLabel
                File.setText(displayName);
                File.setIcon(icon);
            }
        }
    });
  jTree1.addTreeSelectionListener(e -> {
    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
    if (selectedNode != null) {
        Object userObject = selectedNode.getUserObject();
        if (userObject instanceof File) {
            File selectedFile = (File) userObject;
            updateLatestModifiedInfo(selectedFile);
        }
    }
});
  // Inside the displayFileSystemRoots() method or where you handle tree selection
jTree1.addTreeSelectionListener(e -> {
    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
    if (selectedNode != null) {
        Object userObject = selectedNode.getUserObject();
        if (userObject instanceof File) {
            File selectedFile = (File) userObject;
            updateSizeInfo(selectedFile);
        }
    }
});

}

// Traverse subfolders and populate the tree lazily
private void traverseSubfolders(DefaultMutableTreeNode parentNode, File parentDirectory) {
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    File[] files = fileSystemView.getFiles(parentDirectory, true);

    for (File file : files) {
        if (file.isDirectory()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
            parentNode.add(node);

            // Add a dummy node to trigger lazy loading when the node is expanded
            node.add(new DefaultMutableTreeNode(true));
        }
    }
}

private class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    private FileSystemView fileSystemView;

    public FileTreeCellRenderer() {
        fileSystemView = FileSystemView.getFileSystemView();
    }

    @Override
    public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                          boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof File) {
                File file = (File) userObject;
                if (node.isRoot()) {
                    // Handle the root node differently
                    setText(fileSystemView.getSystemDisplayName(file)); // Set the text for the root node
                    setIcon(null); // Remove icon for the root node
                    setOpenIcon(null); // Remove the open folder icon
                    setClosedIcon(null); // Remove the closed folder icon
                    setLeafIcon(null); // Remove the leaf icon
                } else {
                    // For other nodes, display the icons and text
                    setIcon(fileSystemView.getSystemIcon(file));
                    setText(fileSystemView.getSystemDisplayName(file));
                    setToolTipText(file.getPath());
                }
            } else if (userObject instanceof String) {
                String text = (String) userObject;
                setText(text);
            }
        }

        // Handle selection colors
        if (sel) {
            setBackground(getBackgroundSelectionColor());
            setForeground(getTextSelectionColor());
        } else {
            setBackground(getBackgroundNonSelectionColor());
            setForeground(getTextNonSelectionColor());
        }

        return this;
    }
}

private void startFileMonitoring(File directory) {
    Thread fileMonitorThread = new Thread(() -> {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path dirPath = directory.toPath();
            dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take(); // Wait for key to be signaled
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return; // Exit the thread if interrupted
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind != StandardWatchEventKinds.OVERFLOW) {
                        SwingUtilities.invokeLater(() -> displayDirectoryContents(directory));
                    }
                }

                boolean valid = key.reset(); // Reset the key
                if (!valid) {
                    break; // Exit the loop if the key is no longer valid
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log the exception
            // Handle the exception or provide user feedback as needed
        }
    });
    fileMonitorThread.start();
}

// Your existing method for displaying directory contents
private void displayDirectoryContents(File directory) {
    // Ensure the GUI isn't frozen by performing file system operations in a background thread
    SwingUtilities.invokeLater(() -> {
        // Use SwingWorker for background tasks
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                DefaultTableModel model = prepareTableModel(directory);
                setTableModelToTable(model);
                return null;
            }
        };

        worker.execute(); // Start the background task
    });
}

// Method to prepare the table model
private DefaultTableModel prepareTableModel(File directory) {
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();
    File[] files = directory.listFiles();
    DefaultTableModel model = new DefaultTableModel();
    model.setColumnIdentifiers(new Object[]{"Icon", "Name", "Path/Dir", "Type", "Size", "Last Modified"});

    if (files != null) {
        Object[][] rowData = new Object[files.length][6];
        int count = 0;

        for (File file : files) {
            try {
                ImageIcon icon = (ImageIcon) fileSystemView.getSystemIcon(file);
                if (icon == null || icon.getImage() == null) {
                    icon = getDefaultIcon();
                }

                String displayName = fileSystemView.getSystemDisplayName(file);
                String path = file.getAbsolutePath();
                String fileType = file.isDirectory() ? "Folder" : "File";
                long fileSizeValue = file.isDirectory() ? 0 : file.length();
                String fileSize = file.isDirectory() ? "-" : getFileSizeString(fileSizeValue);
                String lastModified = file.isDirectory() ? "-" : String.valueOf(new Date(file.lastModified()));

                if (displayName == null || displayName.isEmpty()) {
                    displayName = "Unknown";
                }

                rowData[count][0] = icon;
                rowData[count][1] = displayName;
                rowData[count][2] = path;
                rowData[count][3] = fileType;
                rowData[count][4] = fileSize;
                rowData[count][5] = lastModified;

                count++;
            } catch (Exception ex) {
                // Handle the exception if required
            }
        }

        String[] columnNames = new String[]{"Icon", "Name", "Path/Dir", "Type", "Size", "Last Modified"};
        model.setDataVector(rowData, columnNames);
    }

    return model;
}

// Method to set the table model to the JTable
private void setTableModelToTable(DefaultTableModel model) {
    jTable1.setModel(model);
    jTable1.setEnabled(true);
    jTable1.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());
    jTable1.getColumnModel().getColumn(0).setMinWidth(50);
    jTable1.getColumnModel().getColumn(0).setMaxWidth(50);

 jTable1.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int selectedRow = jTable1.getSelectedRow();
        int columnCount = jTable1.getColumnCount(); // Get the number of columns in the table
        if (selectedRow != -1 && columnCount > 2) { // Check if the selected row is valid and the column count is sufficient
            String pathDir = (String) jTable1.getValueAt(selectedRow, 2); // Get the "Path/Dir" from the third column
            if (pathDir != null) {
                // Set the path directory to the "Path" TextField
                TextFieldPath.setText(pathDir);
            }
        }
    }
});

     
// Set display name and icon to the "File" JLabel based on table selection
jTable1.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            String displayName = (String) jTable1.getValueAt(selectedRow, 1); // Get the "Name" from the second column
            ImageIcon icon = (ImageIcon) jTable1.getValueAt(selectedRow, 0); // Get the "Icon" from the first column
            if (displayName != null && icon != null) {
                // Set the display name and icon to the "File" JLabel
                File.setText(displayName);
                File.setIcon(icon);
            }
        }
    }
});

// Update latest modified information based on table selection
jTable1.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            String pathDir = (String) jTable1.getValueAt(selectedRow, 2);
            File file = new File(pathDir);
            updateLatestModifiedInfo(file);
        }
    }
});

// Update size information based on table selection
jTable1.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            String pathDir = (String) jTable1.getValueAt(selectedRow, 2);
            File file = new File(pathDir);
            updateSizeInfo(file);
        }
    }
});

}

// Your existing method for getting the default icon
private ImageIcon getDefaultIcon() {
    ImageIcon defaultIcon = null;
    try {
        java.net.URL imgUrl = FileManager1.class.getResource("/Images/icons.png");
        if (imgUrl != null) {
            defaultIcon = new ImageIcon(imgUrl);
        } else {
            defaultIcon = new ImageIcon(); // Create a default icon if necessary
        }
    } catch (Exception e) {
        // Log or handle the exception as needed
    }
    return defaultIcon;
}

// Custom cell renderer to display icons in JTable
private class IconCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof ImageIcon imageIcon) {
            label.setIcon(imageIcon);
            label.setText(""); // Clear the text when displaying an icon
        }
        return label;
    }
}

private String getFileSizeString(long sizeInBytes) {
    final String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB"};
    int unitIndex = 0;
    double fileSize = sizeInBytes;

    while (fileSize > 1024 && unitIndex < units.length - 1) {
        fileSize /= 1024;
        unitIndex++;
    }

    return String.format("%.2f %s", fileSize, units[unitIndex]);
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelLastModified = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        Edit = new javax.swing.JButton();
        Archive = new javax.swing.JButton();
        Open = new javax.swing.JButton();
        New = new javax.swing.JButton();
        Unarchive = new javax.swing.JButton();
        Copy = new javax.swing.JButton();
        Delete = new javax.swing.JButton();
        Encrypt = new javax.swing.JButton();
        TextFieldPath = new javax.swing.JTextField();
        File = new javax.swing.JLabel();
        Path = new javax.swing.JLabel();
        LastModified = new javax.swing.JLabel();
        Size = new javax.swing.JLabel();
        Type = new javax.swing.JLabel();
        Directory = new javax.swing.JRadioButton();
        RadioFile = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        panelSize = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        Searchbar = new javax.swing.JTextField();
        Backupbutton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setIconifiable(true);
        setResizable(true);

        panelLastModified.setBackground(new java.awt.Color(102, 102, 102));
        panelLastModified.setForeground(new java.awt.Color(255, 255, 255));

        jTree1.setBackground(new java.awt.Color(204, 204, 204));
        jTree1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jScrollPane1.setViewportView(jTree1);

        jTable1.setBackground(new java.awt.Color(102, 102, 102));
        jTable1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jTable1.setForeground(new java.awt.Color(255, 255, 255));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "TItle 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        Edit.setBackground(new java.awt.Color(0, 0, 0));
        Edit.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Edit.setForeground(new java.awt.Color(255, 255, 255));
        Edit.setText("Edit");
        Edit.setBorder(null);
        Edit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EditActionPerformed(evt);
            }
        });

        Archive.setBackground(new java.awt.Color(0, 0, 0));
        Archive.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Archive.setForeground(new java.awt.Color(255, 255, 255));
        Archive.setText("Zip");
        Archive.setBorder(null);
        Archive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ArchiveActionPerformed(evt);
            }
        });

        Open.setBackground(new java.awt.Color(0, 0, 0));
        Open.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Open.setForeground(new java.awt.Color(255, 255, 255));
        Open.setText("Open");
        Open.setBorder(null);
        Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenActionPerformed(evt);
            }
        });

        New.setBackground(new java.awt.Color(0, 0, 0));
        New.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        New.setForeground(new java.awt.Color(255, 255, 255));
        New.setText("New");
        New.setBorder(null);
        New.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewActionPerformed(evt);
            }
        });

        Unarchive.setBackground(new java.awt.Color(0, 0, 0));
        Unarchive.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Unarchive.setForeground(new java.awt.Color(255, 255, 255));
        Unarchive.setText("Unzip");
        Unarchive.setBorder(null);
        Unarchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UnarchiveActionPerformed(evt);
            }
        });

        Copy.setBackground(new java.awt.Color(0, 0, 0));
        Copy.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Copy.setForeground(new java.awt.Color(255, 255, 255));
        Copy.setText("Copy");
        Copy.setBorder(null);
        Copy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CopyActionPerformed(evt);
            }
        });

        Delete.setBackground(new java.awt.Color(0, 0, 0));
        Delete.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Delete.setForeground(new java.awt.Color(255, 255, 255));
        Delete.setText("Delete");
        Delete.setBorder(null);
        Delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteActionPerformed(evt);
            }
        });

        Encrypt.setBackground(new java.awt.Color(0, 0, 0));
        Encrypt.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Encrypt.setForeground(new java.awt.Color(255, 255, 255));
        Encrypt.setText("Encrypt");
        Encrypt.setBorder(null);
        Encrypt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EncryptActionPerformed(evt);
            }
        });

        TextFieldPath.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        TextFieldPath.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        File.setForeground(new java.awt.Color(255, 255, 255));

        Path.setForeground(new java.awt.Color(255, 255, 255));
        Path.setText("Path:");

        LastModified.setForeground(new java.awt.Color(255, 255, 255));
        LastModified.setText("Last Modified:");

        Size.setForeground(new java.awt.Color(255, 255, 255));
        Size.setText("Size:");

        Type.setForeground(new java.awt.Color(255, 255, 255));
        Type.setText("Type:");

        Directory.setForeground(new java.awt.Color(255, 255, 255));
        Directory.setText("Directory");

        RadioFile.setForeground(new java.awt.Color(255, 255, 255));
        RadioFile.setText("File");
        RadioFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RadioFileActionPerformed(evt);
            }
        });

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("File:");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        Backupbutton.setBackground(new java.awt.Color(0, 0, 0));
        Backupbutton.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 14)); // NOI18N
        Backupbutton.setForeground(new java.awt.Color(255, 255, 255));
        Backupbutton.setText("Backup");
        Backupbutton.setBorder(null);
        Backupbutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackupbuttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLastModifiedLayout = new javax.swing.GroupLayout(panelLastModified);
        panelLastModified.setLayout(panelLastModifiedLayout);
        panelLastModifiedLayout.setHorizontalGroup(
            panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLastModifiedLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLastModifiedLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLastModifiedLayout.createSequentialGroup()
                                .addComponent(Type)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Directory)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(RadioFile, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLastModifiedLayout.createSequentialGroup()
                                .addComponent(LastModified)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel1))
                            .addGroup(panelLastModifiedLayout.createSequentialGroup()
                                .addComponent(Size)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(panelSize))
                            .addGroup(panelLastModifiedLayout.createSequentialGroup()
                                .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(Path)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(TextFieldPath, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(File, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelLastModifiedLayout.createSequentialGroup()
                        .addComponent(Searchbar, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLastModifiedLayout.createSequentialGroup()
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2)
                            .addGroup(panelLastModifiedLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(Open, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Edit, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Copy, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(New, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Delete, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Backupbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Encrypt, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Archive, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(Unarchive, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(23, 23, 23))))
        );
        panelLastModifiedLayout.setVerticalGroup(
            panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLastModifiedLayout.createSequentialGroup()
                .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLastModifiedLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(Searchbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(Encrypt)
                                    .addComponent(Open)
                                    .addComponent(Edit)
                                    .addComponent(New)
                                    .addComponent(Copy)
                                    .addComponent(Delete)
                                    .addComponent(Unarchive)
                                    .addComponent(Archive))
                                .addComponent(jSeparator1)
                                .addComponent(jSeparator2))
                            .addComponent(Backupbutton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(File, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Path)
                            .addComponent(TextFieldPath, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(LastModified)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Size)
                            .addComponent(panelSize))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelLastModifiedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Type)
                            .addComponent(Directory)
                            .addComponent(RadioFile)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelLastModified, javax.swing.GroupLayout.PREFERRED_SIZE, 747, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(panelLastModified, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void EditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EditActionPerformed
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            String filePath = model.getValueAt(selectedRow, 2).toString(); // Assuming the file path is in the third column

            File selectedFile = new File(filePath);
            editFile(selectedFile);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file from the table.", "No file selected", JOptionPane.WARNING_MESSAGE);
        }
        }//GEN-LAST:event_EditActionPerformed

    private void ArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ArchiveActionPerformed
     JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setDialogTitle("Save Archive");

    int[] selectedRows = jTable1.getSelectedRows();
    String commonExtension = getCommonFileExtension(selectedRows);

    if (selectedRows.length > 0 && commonExtension != null && !commonExtension.isEmpty()) {
        String firstFileName = (String) jTable1.getValueAt(selectedRows[0], 1);
        String suggestedFileName = firstFileName.substring(0, firstFileName.lastIndexOf('.')) + ".zip";
        fileChooser.setSelectedFile(new File(suggestedFileName));
    }

    int option = fileChooser.showSaveDialog(this);

    if (option == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        String zipFilePath = selectedFile.getAbsolutePath();

        if (!zipFilePath.toLowerCase().endsWith(".zip")) {
            zipFilePath += ".zip";
        }

        List<File> selectedFiles = new ArrayList<>();

        // Get the files corresponding to the selected rows
        for (int selectedRow : selectedRows) {
            String pathDir = (String) jTable1.getValueAt(selectedRow, 2);
            File file = new File(pathDir);
            selectedFiles.add(file);
        }

        // Perform the archive operation using the selected files
        if (!selectedFiles.isEmpty()) {
            File zipFile = new File(zipFilePath);
            boolean isSuccessful = archiveFiles(selectedFiles, zipFile);

            // Display dialog based on the success of archiving
            if (isSuccessful) {
                JOptionPane.showMessageDialog(this, "Files zipped successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to zip files.");
            }
        }
    }
    }//GEN-LAST:event_ArchiveActionPerformed

    private void OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenActionPerformed
    int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            String filePath = model.getValueAt(selectedRow, 2).toString(); // Assuming the file path is in the third column

            File selectedFile = new File(filePath);
            openFile(selectedFile);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file from the table.", "No file selected", JOptionPane.WARNING_MESSAGE);
        
    }

    }//GEN-LAST:event_OpenActionPerformed

    private void NewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewActionPerformed
         FileDialog dialog = new FileDialog((Frame)null, "Create New", FileDialog.SAVE);
        dialog.setVisible(true);

        String directory = dialog.getDirectory();
        String file = dialog.getFile();

        if (file != null) {
            String fullPath = directory + file;
            File newFile = new File(fullPath);

            if (newFile.exists()) {
                // File or folder with the same name already exists
                // Handle accordingly
            } else {
                if (dialog.getFile() != null && dialog.getFile().contains(".")) {
                    try {
                        if (newFile.createNewFile()) {
                            // File created successfully
                            // Show success message or perform further actions
                        } else {
                            // Failed to create file
                            // Show error message or perform error handling
                        }
                    } catch (IOException e) {
                        // Exception occurred during file creation
                        // Handle the exception appropriately
                    }
                } else {
                    if (newFile.mkdir()) {
                        // Directory created successfully
                        // Show success message or perform further actions
                    } else {
                        // Failed to create directory
                        // Show error message or perform error handling
                    }
                }
            }
        }
    
    }//GEN-LAST:event_NewActionPerformed

    private void UnarchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UnarchiveActionPerformed
   JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setDialogTitle("Select Zip File to Unarchive");

    // Filter to show only ZIP files
    FileNameExtensionFilter filter = new FileNameExtensionFilter("ZIP files", "zip");
    fileChooser.setFileFilter(filter);

    int option = fileChooser.showOpenDialog(this);

    if (option == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();

        if (selectedFile != null && selectedFile.exists() && selectedFile.isFile()) {
            boolean isSuccessful = unarchiveFile(selectedFile);

            // Display dialog based on the success of unarchiving
            if (isSuccessful) {
                JOptionPane.showMessageDialog(this, "Files unzipped successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to unzip files.");
            }
        }
    }
    }//GEN-LAST:event_UnarchiveActionPerformed

    private void CopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CopyActionPerformed
   int[] selectedRows = jTable1.getSelectedRows();

    if (selectedRows.length > 0) {
        List<File> selectedFiles = getSelectedFiles(selectedRows);

        if (!selectedFiles.isEmpty()) {
            FileDialog fileDialog = new FileDialog((Frame) null, "Select Destination Folder", FileDialog.SAVE);
            fileDialog.setFile(selectedFiles.get(0).getName()); // Set the default file name in the FileDialog
            fileDialog.setVisible(true);

            String destinationFolder = fileDialog.getDirectory();
            String newFileName = fileDialog.getFile();

            if (destinationFolder != null && !destinationFolder.isEmpty() && newFileName != null && !newFileName.isEmpty()) {
                File destinationFile = new File(destinationFolder, newFileName);

                // Perform the copy operation using the selected files to the chosen destination folder
                copyFiles(selectedFiles, destinationFile);
                JOptionPane.showMessageDialog(this, "File copied successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Canceled or No Directory Selected.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a file or folder to copy.");
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select a file or folder to copy.");
    }
    }//GEN-LAST:event_CopyActionPerformed

    private void DeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteActionPerformed
       // Get selected files/folders from jtable1
        int[] selectedRows = jTable1.getSelectedRows();
        
        for (int row : selectedRows) {
            String filePath = jTable1.getValueAt(row, 2).toString();
            File fileToDelete = new File(filePath);
            
            if (fileToDelete.exists()) {
                // Copy file/folder to the Recycle Bin folder
                File recycleBinFolder = new File(System.getProperty("user.dir") + "/Kronosphere/Recycle Bin");
                if (!recycleBinFolder.exists()) {
                    recycleBinFolder.mkdirs(); // Create the Recycle Bin folder if it doesn't exist
                }
                
                File destinationFile = new File(recycleBinFolder, fileToDelete.getName());
                try {
                    if (fileToDelete.isDirectory()) {
                        copyFolder(fileToDelete, destinationFile);
                    } else {
                        copyFile(fileToDelete, destinationFile);
                    }
                    
                    // Delete the original file/folder
                    deleteFileOrFolder(fileToDelete);
                    
                    // Insert file information into the database
                    insertIntoRecycleBin(destinationFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle copy/delete errors
                }
            }
        } 
    }//GEN-LAST:event_DeleteActionPerformed

    private void EncryptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EncryptActionPerformed
   int[] selectedRows = jTable1.getSelectedRows();

    if (selectedRows.length > 0) {
        String projectDirectory = System.getProperty("user.dir");
        String kronospherePath = projectDirectory + "/Kronosphere";
        String encryptedFilesDirectory = createEncryptedFilesDirectory(kronospherePath);

        if (encryptedFilesDirectory == null) {
            JOptionPane.showMessageDialog(null, "Error creating encrypted files directory.");
            return;
        }

        for (int row : selectedRows) {
            try {
                String filePath = (String) jTable1.getValueAt(row, 2);
                File selectedFile = new File(filePath);

                String key = GenerateKey();
                byte[] encryptedData = EncryptFile(selectedFile, key);

                String encryptedFileName = selectedFile.getName();
                Path encryptedFilePath = Paths.get(encryptedFilesDirectory, encryptedFileName);
                Files.write(encryptedFilePath, encryptedData);

                long fileSize = selectedFile.length();
                Date currentDate = new Date();

                StoreFileInformation(selectedFile.getName(), key, fileSize, filePath, currentDate);

                if (deleteOriginalFile(selectedFile)) {
                    JOptionPane.showMessageDialog(null, "File encrypted and original file deleted.\nEncryption Key: " + key);
                } else {
                    JOptionPane.showMessageDialog(null, "File encrypted, but failed to delete the original file.\nEncryption Key: " + key);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error encrypting file: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    } else {
        JOptionPane.showMessageDialog(null, "Please select a file or folder to encrypt.");
    }
    }//GEN-LAST:event_EncryptActionPerformed

    private void RadioFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RadioFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RadioFileActionPerformed

    private void BackupbuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackupbuttonActionPerformed
         int[] selectedRows = jTable1.getSelectedRows();

    if (selectedRows.length == 0) {
        JOptionPane.showMessageDialog(this, "Please select file(s) to backup.");
    } else {
        // Get Kronosphere directory path
        String projectDirectory = System.getProperty("user.dir");
        String kronospherePath = projectDirectory + "/Kronosphere";

        // Create a directory for Back Up Files inside Kronosphere if it doesn't exist
        String backupFolderPath = kronospherePath + "/Back Up Files";
        File backupFolder = new File(backupFolderPath);
        if (!backupFolder.exists()) {
            boolean created = backupFolder.mkdirs();
            if (!created) {
                System.err.println("Failed to create Back Up Files folder in Kronosphere.");
                return;
            }
        }

        for (int row : selectedRows) {
            String pathDir = (String) jTable1.getValueAt(row, 2); // Get the "Path/Dir" from the third column
            File file = new File(pathDir);

            // Create a destination path for backup
            String destinationPath = backupFolderPath + "/" + file.getName();
            File destinationFile = new File(destinationPath);

            // Perform file copy operation to the Back Up Files folder
            try {
                Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File backed up successfully to Back Up Files folder.");

                // Insert into the database
                BackupAndInsert(destinationFile);
            } catch (IOException ex) {
                System.err.println("Error during file backup: " + ex.getMessage());
            }
        }
        JOptionPane.showMessageDialog(this, "Selected file(s) backed up successfully!");
    }
    }//GEN-LAST:event_BackupbuttonActionPerformed

private void openFile(File file) {
        try {
            Desktop.getDesktop().open(file); // Opens the file with the default application
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

private void editFile(File file) {
    try {
        Desktop.getDesktop().open(file); // Opens the file with the default associated application
    } catch (IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

private boolean archiveFiles(List<File> files, File zipFile) {
    // Create a zip archive of selected files
    try (FileOutputStream fos = new FileOutputStream(zipFile);
         ZipOutputStream zipOut = new ZipOutputStream(fos)) {

        for (File file : files) {
            addToZipFile(file, file.getName(), zipOut);
            // Delete the original file after zipping
            if (!file.delete()) {
                System.out.println("Failed to delete file: " + file.getName());
            }
        }

        // Change the icon of the saved ZIP file
        ImageIcon zipIcon = new ImageIcon("path/to/zip-icon.png"); // Provide the path to your zip icon
        JLabel fileLabel = new JLabel(zipFile.getName(), zipIcon, SwingConstants.LEADING);
        jTable1.setValueAt(fileLabel.getIcon(), jTable1.getSelectedRow(), 0);
        return true;
    } catch (IOException e) {
        e.printStackTrace();
        // Handle the exception appropriately
        return false;
    }
}

private String getCommonFileExtension(int[] selectedRows) {
    String commonExtension = null;

    if (selectedRows.length > 0) {
        String firstFileName = (String) jTable1.getValueAt(selectedRows[0], 1); // Assuming file names are in column 2

        if (firstFileName != null && !firstFileName.isEmpty()) {
            int lastDotIndex = firstFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                commonExtension = firstFileName.substring(lastDotIndex + 1).toLowerCase();
            }
        }

        for (int i = 1; i < selectedRows.length; i++) {
            String fileName = (String) jTable1.getValueAt(selectedRows[i], 1);
            if (fileName != null && !fileName.isEmpty()) {
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    String extension = fileName.substring(dotIndex + 1).toLowerCase();
                    if (!extension.equals(commonExtension)) {
                        // If extensions don't match, there's no common extension
                        return null;
                    }
                }
            }
        }
    }

    return commonExtension;
}

private void addToZipFile(File file, String fileName, ZipOutputStream zipOut) throws IOException {
    try (FileInputStream fis = new FileInputStream(file)) {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        zipOut.closeEntry();
    }
}

private List<File> getSelectedFiles(int[] selectedRows) {
    List<File> selectedFiles = new ArrayList<>();

    // Get the files corresponding to the selected rows
    for (int selectedRow : selectedRows) {
        String pathDir = (String) jTable1.getValueAt(selectedRow, 2);
        File file = new File(pathDir);
        selectedFiles.add(file);
    }

    return selectedFiles;
}

private void copyFiles(List<File> files, File destinationFile) {
    for (File file : files) {
        try {
            String fileName = destinationFile.getName();
            String extension = "";
            
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = fileName.substring(dotIndex);
                fileName = fileName.substring(0, dotIndex);
            }

            int count = 1;
            File destFile = new File(destinationFile.getParent(), fileName + extension);
            
            while (destFile.exists()) {
                destFile = new File(destinationFile.getParent(), fileName + " (" + count + ")" + extension);
                count++;
            }

            FileUtils.copyFile(file, destFile);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }
}

private boolean unarchiveFile(File zipFile) {
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
        byte[] buffer = new byte[1024];
        ZipEntry zipEntry;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Directory to Save Unzipped Files");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int option = fileChooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return false; // User canceled or didn't select a directory
        }

        File saveDirectory = fileChooser.getSelectedFile();

        while ((zipEntry = zis.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(saveDirectory, fileName);

            // Create parent directories if they do not exist
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(newFile);
            int length;
            while ((length = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.close();
        }

        // Close ZipInputStream
        zis.close();

        // Delete the original ZIP file
        if (zipFile.exists() && zipFile.isFile()) {
            boolean deleted = zipFile.delete();
            if (!deleted) {
                System.err.println("Failed to delete original ZIP file.");
            }
        }

        return true; // Successfully unarchived files
    } catch (IOException e) {
        e.printStackTrace();
        return false; // Failed to unzip files
    }
}



private void updateSizeInfo(File file) {
    String fileSize = "-";
    if (file != null && file.exists()) {
        if (file.isDirectory()) {
            fileSize = "-";
        } else {
            long bytes = file.length();
            fileSize = formatFileSize(bytes);
        }
    }
    Size.setText("Size: " + fileSize);
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


private void updateLatestModifiedInfo(File file) {
    String lastModified = "-";
    if (file != null && file.exists()) {
        lastModified = String.valueOf(new Date(file.lastModified()));
    }
    LastModified.setText("Last Modified: " + lastModified);
}  

private void initializeRadioFileState() {
    RadioFile.setEnabled(false); // Initially set to disabled

    // Add a listener to the tree selection event
    jTree1.addTreeSelectionListener(e -> {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (selectedNode != null) {
            Object userObject = selectedNode.getUserObject();
            if (userObject instanceof File) {
                File selectedFile = (File) userObject;
                RadioFile.setSelected(selectedFile.isFile());
            } else {
                RadioFile.setSelected(false);
            }
        } else {
            RadioFile.setSelected(false);
        }
    });

    // Add a listener to the table selection event
    jTable1.getSelectionModel().addListSelectionListener(e -> {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            String pathDir = (String) jTable1.getValueAt(selectedRow, 2);
            File selectedFile = new File(pathDir);
            RadioFile.setSelected(selectedFile.isFile());
        } else {
            RadioFile.setSelected(false);
        }
    });
}

private void initializeRadioDirectoryState() {
    Directory.setEnabled(false); // Initially set to disabled

    // Add a listener to the tree selection event
    jTree1.addTreeSelectionListener(e -> {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (selectedNode != null) {
            Object userObject = selectedNode.getUserObject();
            if (userObject instanceof File) {
                File selectedFile = (File) userObject;
                Directory.setSelected(selectedFile.isDirectory());
            } else {
                Directory.setSelected(false);
            }
        } else {
            Directory.setSelected(false);
        }
    });

    // Add a listener to the table selection event
    jTable1.getSelectionModel().addListSelectionListener(e -> {
        int selectedRow = jTable1.getSelectedRow();
        if (selectedRow != -1) {
            String pathDir = (String) jTable1.getValueAt(selectedRow, 2);
            File selectedFile = new File(pathDir);
            Directory.setSelected(selectedFile.isDirectory());
        } else {
            Directory.setSelected(false);
        }
    });
}

private void CreateDatabaseAndTable() {
    String SQLURL = "jdbc:mysql://localhost:3307/"; // Adjust the URL for your database system
    String Username = "root";
    String Password = "kronosphereTSM";
    String DatabaseName = "Kronosphere";
    Connection connection = null;
    
    try {
        connection = DriverManager.getConnection(SQLURL, Username, Password);
        Statement statement = connection.createStatement();

        // Create the database if it doesn't exist
        statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DatabaseName);

        // Switch to the new database
        statement.executeUpdate("USE " + DatabaseName);

        // Create the "Information_Files" table with Path column added and Date as TIMESTAMP
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Information_Files (" +
            "ID INT AUTO_INCREMENT PRIMARY KEY," +
            "File_Name VARCHAR(255) NOT NULL," +
            "Path VARCHAR(1000) NOT NULL," +
            "Encryption_Key VARCHAR(255) NOT NULL," +
            "File_Size BIGINT NOT NULL," +
            "Date TIMESTAMP NOT NULL)";

        statement.executeUpdate(createTableSQL);

        System.out.println("Database and table created successfully.");
    } catch (SQLException ex) {
        System.err.println("Error creating the database and table: " + ex.getMessage());
    } finally {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}



private void StoreFileInformation(String fileName, String key, long fileSize, String path, Date date) {
    String jdbcURL = "jdbc:mysql://localhost:3307/Kronosphere";
    String username = "root";
    String password = "kronosphereTSM";
    Connection connection = null;
    
    try {
        connection = DriverManager.getConnection(jdbcURL, username, password);

        String insertSQL = "INSERT INTO Information_Files (File_Name, Encryption_Key, File_Size, Path, Date) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
        preparedStatement.setString(1, fileName);
        preparedStatement.setString(2, key);
        preparedStatement.setLong(3, fileSize);
        preparedStatement.setString(4, path); // Correctly set the path value
        preparedStatement.setTimestamp(5, new java.sql.Timestamp(date.getTime())); // Use Timestamp for storing date as a timestamp
        preparedStatement.executeUpdate();

        System.out.println("File information stored in the database.");
    } catch (SQLException ex) {
        System.err.println("Error storing file information in the database: " + ex.getMessage());
    } finally {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}



// Method to encrypt data from a file using AES/CBC mode with a 256-bit key
private byte[] EncryptFile(File inputFile, String key) throws Exception {
  // Read input file into byte array
  byte[] InputData = Files.readAllBytes(inputFile.toPath());

  // Convert key string to byte array
  byte[] KeyData = Base64.getDecoder().decode(key); // Decode the base64-encoded key

  // Generate a 128-bit initialization vector (IV) as required by AES/CBC
  byte[] IVData = GenerateIV();

  // Create cipher and encrypt data
  Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
  SecretKeySpec keySpec = new SecretKeySpec(KeyData, "AES");
  IvParameterSpec ivSpec = new IvParameterSpec(IVData);
  cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
  byte[] EncryptedData = cipher.doFinal(InputData);

  // Concatenate initialization vector and encrypted data into a single byte array
  byte[] outputData = new byte[IVData.length + EncryptedData.length];
  System.arraycopy(IVData, 0, outputData, 0, IVData.length);
  System.arraycopy(EncryptedData, 0, outputData, IVData.length, EncryptedData.length);

  // Return the concatenated byte array
  return outputData;
}


// Method to generate a random initialization vector
private byte[] GenerateIV() {
    SecureRandom random = new SecureRandom();
    byte[] IVData = new byte[16];
    random.nextBytes(IVData);
    return IVData;
}

// Method to generate a 256-bit random key and return it as a base64-encoded string
private String GenerateKey() {
  SecureRandom random = new SecureRandom();
  byte[] KeyData = new byte[32]; // 256 bits
  random.nextBytes(KeyData);
  return Base64.getEncoder().encodeToString(KeyData);
}

private boolean deleteOriginalFile(File fileToDelete) {
    if (fileToDelete.exists()) {
        return fileToDelete.delete();
    }
    return false;
}



 public String createEncryptedFilesDirectory(String kronospherePath) {
        String encryptedFilesDirectory = kronospherePath + "/Encrypted Files";

        try {
            Path encryptedDirPath = Paths.get(encryptedFilesDirectory);

            // Create the directory if it doesn't exist
            if (!Files.exists(encryptedDirPath)) {
                Files.createDirectories(encryptedDirPath);
                JOptionPane.showMessageDialog(null, "Encrypted Files directory created.");
            } 

            return encryptedFilesDirectory;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error creating encrypted files directory: " + ex.getMessage());
            return null;
        }
    }

    public ArrayList<String> getEncryptedFiles(String encryptedFilesDirectory) {
        ArrayList<String> encryptedFilesList = new ArrayList<>();

        try {
            // Fetch all files in the encrypted files directory
            File[] files = new File(encryptedFilesDirectory).listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        // Add encrypted file paths to the list
                        encryptedFilesList.add(file.getAbsolutePath());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // Handle exception accordingly (show message, log, etc.)
        }

        return encryptedFilesList;
    }

 private void BackupAndInsert(File file) {
    // Database connection details
    String SQLURL = "jdbc:mysql://localhost:3307/Kronosphere";
    String Username = "root";
    String Password = "kronosphereTSM";
    Connection connection = null;

    try {
        connection = DriverManager.getConnection(SQLURL, Username, Password);

        // Insertion SQL statement
        String insertSQL = "INSERT INTO Backup_Files (File_Name, File_Size, File_Content) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            // Set values for the prepared statement
            preparedStatement.setString(1, file.getName());
            preparedStatement.setLong(2, file.length());

            // Read the file content into a byte array
            byte[] fileContent = Files.readAllBytes(file.toPath());

            // Set the file content as a parameter in the prepared statement
            preparedStatement.setBytes(3, fileContent);

            // Execute the insert statement
            preparedStatement.executeUpdate();

            // Add the filename to the listModel
            listModel.addElement(file.getName());

            System.out.println("File backed up successfully!");
        }
    } catch (IOException | SQLException ex) {
        System.err.println("Error during backup and insertion: " + ex.getMessage());
    } finally {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
 
private void searchFiles() {
    String query = Searchbar.getText().trim(); // Get the search query from the text field
    RowSorter<?> rowSorter = jTable1.getRowSorter();

    if (rowSorter instanceof TableRowSorter) {
        @SuppressWarnings("unchecked")
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) rowSorter;
        //DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        if (query.length() == 0) {
            // If the search query is empty, show all rows
            sorter.setRowFilter(null);
        } else {
            // Perform case-insensitive filtering based on the search query
            RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + query);
            sorter.setRowFilter(rf);
        }
    }
    // Add an else condition if needed to handle cases where the row sorter isn't of type TableRowSorter<DefaultTableModel>
}


   
private void copyFile(File source, File destination) throws IOException {
        try (InputStream inputStream = new FileInputStream(source);
             OutputStream outputStream = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
    
    private void copyFolder(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(source, file);
                    File destFile = new File(destination, file);
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            copyFile(source, destination);
        }
    }
    
    private void deleteFileOrFolder(File file) throws IOException {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteFileOrFolder(f);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);
        }
    }
    
    private void insertIntoRecycleBin(File deletedFile) {
        // JDBC connection parameters
        String url = "jdbc:mysql://localhost:3307/Kronosphere"; // Update URL with your database details
        String username = "root";
        String password = "kronosphereTSM";
        
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            // Prepare SQL statement to insert data into recyclebin table
            String sql = "INSERT INTO recyclebin (file_name, file_path, deletion_date) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // Set values for insertion
                preparedStatement.setString(1, deletedFile.getName());
                preparedStatement.setString(2, deletedFile.getAbsolutePath());
                
                // Use current date and time for deletion date
                java.util.Date deletionDate = new java.util.Date();
                preparedStatement.setObject(3, new java.sql.Timestamp(deletionDate.getTime()));
                
                // Execute the insertion query
                preparedStatement.executeUpdate();
                System.out.println("File information inserted into recyclebin table successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database connection issues or SQL errors
        }
    }



/**
 * Decrypts the contents of a file using the AES encryption algorithm and the
 * given key.
 * 
 * @param inputFile the file to be decrypted
 * @param key the encryption key to use
 * @return the decrypted data
 * @throws Exception if an error occurs during decryption
 */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Archive;
    private javax.swing.JButton Backupbutton;
    private javax.swing.JButton Copy;
    private javax.swing.JButton Delete;
    private javax.swing.JRadioButton Directory;
    private javax.swing.JButton Edit;
    private javax.swing.JButton Encrypt;
    private javax.swing.JLabel File;
    private javax.swing.JLabel LastModified;
    private javax.swing.JButton New;
    private javax.swing.JButton Open;
    private javax.swing.JLabel Path;
    private javax.swing.JRadioButton RadioFile;
    private javax.swing.JTextField Searchbar;
    private javax.swing.JLabel Size;
    private javax.swing.JTextField TextFieldPath;
    private javax.swing.JLabel Type;
    private javax.swing.JButton Unarchive;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTree jTree1;
    private javax.swing.JPanel panelLastModified;
    private javax.swing.JLabel panelSize;
    // End of variables declaration//GEN-END:variables
}
