/*
 * Copyright (C) 2021 André Gewert <agewert@ubergeek.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.ubergeek.amigaguideviewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Simple AmigaGuide viewer - main window
 * @author André Gewert <agewert@ubergeek.de>
 */
public class MainWindow extends javax.swing.JFrame {

    // <editor-fold desc="Properties">
    
    /**
     * Prefix for window title
     */
    private final static String TITLE_PREFIX = "AmigaGuideViewer";
    
    /**
     * Currently loaded (and parsed) AmigaGuide document
     */
    private Document currentDocument = null;
    
    /**
     * Selected / shown document node
     */
    private Node selectedDocumentNode = null;
    
    /**
     * View stack for navigation back
     */
    private final Stack<Node> viewStack = new Stack<>();

    /**
     * Instance of JFileChooser for "Open file" command
     */
    private final JFileChooser fileChooser = new JFileChooser();
            
    // </editor-fold>
    
    
    // <editor-fold desc="Public methods">
    
    /**
     * Sets an already parsed document that should be displayed
     * @param document The document to be shown
     */
    public void setDocument(Document document) {
        currentDocument = document;
        documentNodesTree.setModel(createNodesList(currentDocument));
        viewStack.clear();
        
        if (currentDocument != null && currentDocument.getTitleNode() != null) {
            cmdNavigateToToc();
        }
    }
    
    /**
     * Loads a new AmigaGuide from given file name in a SwingWorker
     * @param filePath Path of the file to be opened
     */
    public void openDocumentFile(Path filePath) {
        statusBarTextLabel.setText("Opening document ...");
        statusBarProgressBar.setVisible(true);
        navOpenButton.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            
            private Document loadedDocument = null;
            private Exception loadingException = null;
            
            @Override
            protected Boolean doInBackground() throws InterruptedException, IOException {
                try {
                    var parser = new Parser();
                    loadedDocument = parser.parseAmigaGuideFromFile(filePath);
                } catch (IOException ex) {
                    loadingException = ex;
                    System.out.println("Error while opening file: " + ex.getMessage());
                    System.out.println(ex.toString());
                }
                return true;
            }
            
            @Override
            protected void done() {
                statusBarTextLabel.setText(" ");
                statusBarProgressBar.setVisible(false);
                navOpenButton.setEnabled(true);
                
                if (loadedDocument != null && loadingException == null) {
                    setDocument(loadedDocument);
                }
            }
        };
        
        worker.execute();
    }
    
    // </editor-fold>
    
    
    // <editor-fold desc="Internal methods">

    private TreeModel createNodesList(Document document) {
        var treeTopNode = new DefaultMutableTreeNode("Contents");
        
        if (document != null) {
            for (var node : document.getNodesList()) {
                var treeNode = new DefaultMutableTreeNode(node, false);
                treeTopNode.add(treeNode);
            }
        }
        return new DefaultTreeModel(treeTopNode);
    }
    
    private void renderDocumentNode(Node node) {
        if (currentDocument != null && node != null && currentDocument.getNodesList().contains(node)) {
            selectedDocumentNode = node;
            mainContentPane.setText(selectedDocumentNode.toHtmlString());
            mainContentPane.setCaretPosition(0);
            viewStack.push(node);
            updateUserInterfaceState();
        } else {
            showDefaultDocument();
        }
    }
    
    private boolean selectDocumentNode(Node node) {
        var treeNode = findTreeNodeByDocumentNode(node);
        if (treeNode == null) return false;
        documentNodesTree.setSelectionPath(new TreePath(treeNode));
        return true;
    }
    
    private boolean selectDocumentNodeByIdentifier(String nodeIdentifier) {
        var node = currentDocument.getNodeByIdentifier(nodeIdentifier);
        if (node == null) return false;
        return selectDocumentNode(node);
    }
    
    private void showDefaultDocument() {
        mainContentPane.setText("<html><body></body></html>");
    }
    
    private TreeNode[] findTreeNodeByDocumentNode(Node node) {
        var model = documentNodesTree.getModel();
        var root = (DefaultMutableTreeNode)model.getRoot();
        
        var count = root.getChildCount();
        
        for (var i = 0; i < count; i++) {
            var treeNode = (DefaultMutableTreeNode)root.getChildAt(i);
            if (treeNode.getUserObject() == node) {
                return treeNode.getPath();
            }
        }
        return null;
    }
    
    private void updateUserInterfaceState() {
        navContentsButton.setEnabled(currentDocument != null);
        navIndexButton.setEnabled(getIndexNodeIdentifier() != null);
        navPreviousButton.setEnabled(getPreviousNodeIdentifier() != null);
        navNextButton.setEnabled(getNextNodeIdentifier() != null);
        navBackButton.setEnabled(currentDocument != null && viewStack.size() >= 2);
        if (selectedDocumentNode != null) {
            setTitle(TITLE_PREFIX + " - " + selectedDocumentNode.getTitle());
        } else {
            setTitle(TITLE_PREFIX);
        }
    }
    
    private void cmdOpenFileDialog() {
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                openDocumentFile(file.toPath());
            }
        }
    }
    
    private String getPreviousNodeIdentifier() {
        if (currentDocument == null || selectedDocumentNode == null) return null;
        return selectedDocumentNode.getPreviousNodeIdentifier();
    }
    
    private String getNextNodeIdentifier() {
        if (currentDocument == null || selectedDocumentNode == null) return null;
        return selectedDocumentNode.getNextNodeIdentifier();
    }
    
    private String getTocNodeIdentifier() {
        if (currentDocument == null) return null;
        String nodeIdentifier = (selectedDocumentNode == null)? currentDocument.getTocNodeIdentifier() : selectedDocumentNode.getTocNodeIdentifier();        
        if (nodeIdentifier == null) {
            var node = currentDocument.getTitleNode();
            if (node != null) nodeIdentifier = node.getIdentifier();
        }
        return nodeIdentifier;
    }
    
    private String getIndexNodeIdentifier() {
        if (currentDocument == null) return null;
        return (selectedDocumentNode == null)? currentDocument.getIndexNodeIdentifier() : selectedDocumentNode.getIndexNodeIdentifier();
    }
    
    private void cmdNavigateToPreviousNode() {
        String nodeIdentifier = getPreviousNodeIdentifier();
        if (nodeIdentifier != null) {
            selectDocumentNodeByIdentifier(nodeIdentifier);
        }
    }
    
    private void cmdNavigateToNextNode() {
        String nodeIdentifier = getNextNodeIdentifier();
        if (nodeIdentifier != null) {
            selectDocumentNodeByIdentifier(nodeIdentifier);
        }
    }
    
    private void cmdNavigateBack() {
        if (currentDocument != null && viewStack.size() >= 2) {
            viewStack.pop();
            var node = viewStack.pop();
            selectDocumentNode(node);
        }
    }
    
    private void cmdNavigateToToc() {
        var nodeIdentifier = getTocNodeIdentifier();
        if (nodeIdentifier != null) {
            selectDocumentNodeByIdentifier(nodeIdentifier);
        }
    }
    
    private void cmdNavigateToIndex() {
        String nodeIdentifier = getIndexNodeIdentifier();
        if (nodeIdentifier != null) {
            selectDocumentNodeByIdentifier(nodeIdentifier);
        }
    }
    
    private void cmdOpenAboutDialog() {
        var dialog = new AboutDialog(this, true);
        dialog.setVisible(true);
    }
    
    // </editor-fold>

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();

        setIconImage(
            new ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/book_picture.png")).getImage()
        );
        
        statusBarProgressBar.setVisible(false);
        
        // Event handler for clicked links
        mainContentPane.setDropTarget(null);
        mainContentPane.addHyperlinkListener((HyperlinkEvent he) -> {
            if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

                var element = he.getSourceElement();
                var attrSet = element.getAttributes();
                
                String href = ((AttributeSet)attrSet.getAttribute(HTML.Tag.A)).getAttribute(HTML.Attribute.HREF).toString();
                var parts = java.net.URLDecoder.decode(href, Charset.defaultCharset()).split("\\:\\/\\/");

                if (parts.length == 2) {
                    if (parts[0].equals("link")) {
                        selectDocumentNodeByIdentifier(parts[1]);
                        
                        // TODO It could be possible to link to external files
                        // If another amiga guide file is linked it should be opened
                        // within this application; otherwise it should be opened
                        // within the system's default application
                        
                        // Desktop.getDesktop().open(...);
                    }
                }                
            }
        });
        showDefaultDocument();
        
        // Event handler for selection changes in the content tree
        documentNodesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        documentNodesTree.addTreeSelectionListener((var tse) -> {
            
            // No node selected
            if (tse.getNewLeadSelectionPath() == null) {
                showDefaultDocument();
                return;
            }

            var selectedTreeNode = (DefaultMutableTreeNode)tse.getNewLeadSelectionPath().getLastPathComponent();
            if (selectedTreeNode != null) {

                var o = selectedTreeNode.getUserObject();
                if (o instanceof Node) {
                    renderDocumentNode((Node)o);
                } else {
                    cmdNavigateToToc();
                }
            }
        });
        
        // Support for dragging files onto application window
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
		    Transferable tr = dtde.getTransferable();
		    DataFlavor[] flavors = tr.getTransferDataFlavors();

                    for (DataFlavor flavor : flavors) {
                        if (flavor.isFlavorJavaFileListType()) {
                            dtde.acceptDrop(dtde.getDropAction());
                            
                            @SuppressWarnings(value = "unchecked")
                            java.util.List<File> files = (java.util.List<File>)tr.getTransferData(flavor);
                            if (files.size() == 1) {
                                openDocumentFile(files.get(0).toPath());
                            }

                            dtde.dropComplete(true);
                        }
                    }
		    return;
		} catch (UnsupportedFlavorException | IOException t) {
                    // Ignore errors
		}
		dtde.rejectDrop();
            }
        });
        
        updateUserInterfaceState();
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
        mainToolBar = new javax.swing.JToolBar();
        navOpenButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        navIndexButton = new javax.swing.JButton();
        navContentsButton = new javax.swing.JButton();
        navPreviousButton = new javax.swing.JButton();
        navNextButton = new javax.swing.JButton();
        navBackButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        navAboutButton = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        documentNodesTree = new javax.swing.JTree();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainContentPane = new javax.swing.JEditorPane();
        statusBarPanel = new javax.swing.JPanel();
        statusBarTextLabel = new javax.swing.JLabel();
        statusBarProgressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setPreferredSize(new java.awt.Dimension(840, 640));
        setSize(new java.awt.Dimension(840, 640));

        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);

        navOpenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/folder_page.png"))); // NOI18N
        navOpenButton.setText("Open");
        navOpenButton.setFocusable(false);
        navOpenButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        navOpenButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        navOpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                navOpenButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(navOpenButton);
        mainToolBar.add(jSeparator1);

        navIndexButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/bookmark.png"))); // NOI18N
        navIndexButton.setText("Index");
        navIndexButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        navIndexButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        navIndexButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                navIndexButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(navIndexButton);

        navContentsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/book_open.png"))); // NOI18N
        navContentsButton.setText("Contents");
        navContentsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        navContentsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        navContentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                navContentsButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(navContentsButton);

        navPreviousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/book_previous.png"))); // NOI18N
        navPreviousButton.setText("Previous");
        navPreviousButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        navPreviousButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        navPreviousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                navPreviousButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(navPreviousButton);

        navNextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/book_next.png"))); // NOI18N
        navNextButton.setText("Next");
        navNextButton.setFocusable(false);
        navNextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        navNextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        navNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                navNextButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(navNextButton);

        navBackButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/arrow_undo.png"))); // NOI18N
        navBackButton.setText("Back");
        navBackButton.setFocusable(false);
        navBackButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        navBackButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        navBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                navBackButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(navBackButton);
        mainToolBar.add(jSeparator2);

        navAboutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/ubergeek/amigaguideviewer/res/info_rhombus.png"))); // NOI18N
        navAboutButton.setText("About");
        navAboutButton.setFocusable(false);
        navAboutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        navAboutButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        navAboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                navAboutButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(navAboutButton);

        jSplitPane1.setDividerLocation(200);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(120, 382));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        documentNodesTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane2.setViewportView(documentNodesTree);

        jSplitPane1.setLeftComponent(jScrollPane2);

        mainContentPane.setEditable(false);
        mainContentPane.setBackground(new java.awt.Color(255, 255, 255));
        mainContentPane.setContentType("text/html"); // NOI18N
        mainContentPane.setText("");
        jScrollPane1.setViewportView(mainContentPane);

        jSplitPane1.setRightComponent(jScrollPane1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(mainToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 228, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSplitPane1)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(mainToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE))
        );

        statusBarPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        statusBarPanel.setPreferredSize(new java.awt.Dimension(0, 22));
        statusBarPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 2));

        statusBarTextLabel.setText(" ");
        statusBarPanel.add(statusBarTextLabel);

        statusBarProgressBar.setIndeterminate(true);
        statusBarPanel.add(statusBarProgressBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(statusBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void navPreviousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navPreviousButtonActionPerformed
        cmdNavigateToPreviousNode();
    }//GEN-LAST:event_navPreviousButtonActionPerformed

    private void navNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navNextButtonActionPerformed
        cmdNavigateToNextNode();
    }//GEN-LAST:event_navNextButtonActionPerformed

    private void navBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navBackButtonActionPerformed
        cmdNavigateBack();
    }//GEN-LAST:event_navBackButtonActionPerformed

    private void navIndexButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navIndexButtonActionPerformed
        cmdNavigateToIndex();
    }//GEN-LAST:event_navIndexButtonActionPerformed

    private void navContentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navContentsButtonActionPerformed
        cmdNavigateToToc();
    }//GEN-LAST:event_navContentsButtonActionPerformed

    private void navOpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navOpenButtonActionPerformed
        cmdOpenFileDialog();
    }//GEN-LAST:event_navOpenButtonActionPerformed

    private void navAboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_navAboutButtonActionPerformed
        cmdOpenAboutDialog();
    }//GEN-LAST:event_navAboutButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree documentNodesTree;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JEditorPane mainContentPane;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JButton navAboutButton;
    private javax.swing.JButton navBackButton;
    private javax.swing.JButton navContentsButton;
    private javax.swing.JButton navIndexButton;
    private javax.swing.JButton navNextButton;
    private javax.swing.JButton navOpenButton;
    private javax.swing.JButton navPreviousButton;
    private javax.swing.JPanel statusBarPanel;
    private javax.swing.JProgressBar statusBarProgressBar;
    private javax.swing.JLabel statusBarTextLabel;
    // End of variables declaration//GEN-END:variables
}
