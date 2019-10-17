/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.philosophofee.farctool2.windows;

import com.philosophofee.farctool2.utilities.Entry;
import com.philosophofee.farctool2.utilities.FarUtils;
import com.philosophofee.farctool2.utilities.MiscUtils;
import java.io.File;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 *
 * @author Aidan
 */
public class EntryAdditionWindow extends javax.swing.JFrame {

    /**
     * Creates new form EntryAdditionWindowPreview
     */
    
    public MainWindow Window;
    public File SelectedFile;
    public ArrayList Entries = new ArrayList<Entry>();
    public DefaultListModel TableEntries = new DefaultListModel<String>();
    
    public EntryAdditionWindow(MainWindow Window) {
        initComponents();
        if (Window.FARC == null) this.FFFARC.setEnabled(false);
        setTitle("Entry Adder");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(getClass().getResource("resources/farctool2_icon.png")).getImage());
        this.Window = Window;
        
    }

    private EntryAdditionWindow() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        EntryList = new javax.swing.JList<>();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        FFFilePath = new javax.swing.JTextField();
        FFGUID = new javax.swing.JTextField();
        FFFARC = new javax.swing.JCheckBox();
        FFFileSelect = new javax.swing.JButton();
        FFAddEntry = new javax.swing.JButton();
        FFFileSelected = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        ManualFilePath = new javax.swing.JTextField();
        ManualSize = new javax.swing.JTextField();
        ManualGUID = new javax.swing.JTextField();
        ManualSHA1 = new javax.swing.JTextField();
        ManualAddEntryToTable = new javax.swing.JButton();
        RemoveSelectedEntry = new javax.swing.JButton();
        AddEntries = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        EntryList.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        EntryList.setModel(TableEntries);
        EntryList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        EntryList.setToolTipText("");
        EntryList.setFixedCellWidth(10);
        jScrollPane1.setViewportView(EntryList);

        FFFilePath.setText("File Path");

        FFGUID.setText("GUID");

        FFFARC.setText("FARC");
        FFFARC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FFFARCActionPerformed(evt);
            }
        });

        FFFileSelect.setText("Select File...");
        FFFileSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FFFileSelectActionPerformed(evt);
            }
        });

        FFAddEntry.setText("Add Entry to Table");
        FFAddEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FFAddEntryActionPerformed(evt);
            }
        });

        FFFileSelected.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        FFFileSelected.setText("No File Selected.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(FFFileSelected, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FFFilePath)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(FFGUID)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(FFFARC, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4))
                    .addComponent(FFAddEntry, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addComponent(FFFileSelect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(FFFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FFGUID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FFFARC, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FFFileSelect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FFFileSelected, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FFAddEntry)
                .addGap(33, 33, 33))
        );

        jTabbedPane1.addTab("From File", jPanel1);

        ManualFilePath.setText("File Path");
        ManualFilePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ManualFilePathActionPerformed(evt);
            }
        });

        ManualSize.setText("Size");
        ManualSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ManualSizeActionPerformed(evt);
            }
        });

        ManualGUID.setText("GUID");

        ManualSHA1.setText("SHA1 Hash");

        ManualAddEntryToTable.setText("Add Entry to Table");
        ManualAddEntryToTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ManualAddEntryToTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ManualFilePath)
                    .addComponent(ManualSHA1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(ManualSize, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ManualGUID, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                    .addComponent(ManualAddEntryToTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ManualFilePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ManualGUID)
                    .addComponent(ManualSize))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ManualSHA1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ManualAddEntryToTable)
                .addContainerGap(64, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Manual", jPanel2);

        RemoveSelectedEntry.setText("Remove Selected Entry");
        RemoveSelectedEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveSelectedEntryActionPerformed(evt);
            }
        });

        AddEntries.setText("Add Entries to MAP!");
        AddEntries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddEntriesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(RemoveSelectedEntry, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(AddEntries, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTabbedPane1)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AddEntries, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(RemoveSelectedEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ManualFilePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ManualFilePathActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ManualFilePathActionPerformed

    private void ManualSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ManualSizeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ManualSizeActionPerformed

    private void FFFARCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FFFARCActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FFFARCActionPerformed

    private void FFFileSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FFFileSelectActionPerformed
        File file = Window.fileDialogue.openFile("", "", "", false);
        if (file == null) { System.out.println("File access cancelled by user."); return; }
        SelectedFile = file;
        FFFileSelected.setText(SelectedFile.getName());
    }//GEN-LAST:event_FFFileSelectActionPerformed

    private void FFAddEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FFAddEntryActionPerformed
        Entry newEntry = new Entry(FFFilePath.getText(), FFGUID.getText(), SelectedFile, FFFARC.isSelected());
        Entries.add(newEntry);
        TableEntries.addElement(newEntry.Path);
        
    }//GEN-LAST:event_FFAddEntryActionPerformed

    private void RemoveSelectedEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveSelectedEntryActionPerformed
        int Index = EntryList.getSelectedIndex();
        TableEntries.remove(Index);
        Entries.remove(Index);
    }//GEN-LAST:event_RemoveSelectedEntryActionPerformed

    private void AddEntriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddEntriesActionPerformed
        for (int i = 0; i < Entries.size(); i++)
        {
            Entry entry = (Entry) Entries.get(i);
            if (entry.AddToFARC)
            {
                File[] selectedFARCs = Window.getSelectedFARCs();
                if (selectedFARCs == null) { System.out.println("File access cancelled by user."); return; }
                FarUtils.addFile(entry.FileToAdd, selectedFARCs);   
                
            }
            MiscUtils.addEntry(entry.Path, entry.SHA1, entry.Size, entry.GUID, Window);
        }
        Window.showUserDialog("Success!", "Entries successfully added!");
        Entries.clear();
        TableEntries.clear();
    }//GEN-LAST:event_AddEntriesActionPerformed

    private void ManualAddEntryToTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ManualAddEntryToTableActionPerformed
        Entry newEntry = new Entry(ManualFilePath.getText(), ManualSHA1.getText(), ManualSize.getText(), ManualGUID.getText());
        Entries.add(newEntry);
        TableEntries.addElement(newEntry.Path);
    }//GEN-LAST:event_ManualAddEntryToTableActionPerformed

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
            java.util.logging.Logger.getLogger(EntryAdditionWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EntryAdditionWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EntryAdditionWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EntryAdditionWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EntryAdditionWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddEntries;
    private javax.swing.JList<String> EntryList;
    private javax.swing.JButton FFAddEntry;
    private javax.swing.JCheckBox FFFARC;
    private javax.swing.JTextField FFFilePath;
    private javax.swing.JButton FFFileSelect;
    private javax.swing.JLabel FFFileSelected;
    private javax.swing.JTextField FFGUID;
    private javax.swing.JButton ManualAddEntryToTable;
    private javax.swing.JTextField ManualFilePath;
    private javax.swing.JTextField ManualGUID;
    private javax.swing.JTextField ManualSHA1;
    private javax.swing.JTextField ManualSize;
    private javax.swing.JButton RemoveSelectedEntry;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}