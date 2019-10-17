package com.philosophofee.farctool2.windows;

//import com.bulenkov.darcula.DarculaLaf;
import com.philosophofee.farctool2.streams.CustomPrintStream;
import com.philosophofee.farctool2.algorithms.KMPMatch;
import com.philosophofee.farctool2.utilities.MapUtils;
import com.philosophofee.farctool2.streams.TextAreaOutputStream;
import com.philosophofee.farctool2.utilities.MiscUtils;
import com.philosophofee.farctool2.utilities.ZlibUtils;
import com.philosophofee.farctool2.utilities.FarUtils;
import com.philosophofee.farctool2.utilities.FileChooser;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import tv.porst.jhexview.*;
import tv.porst.jhexview.JHexView.DefinitionStatus;

public class MainWindow extends javax.swing.JFrame {

 public File MAP = null;
 public File FARC[] = null;
 public File FAR4 = null;
 
 public String currSHA1[] = null;
 public String currGUID[] = null;
 public String currFileName[] = null;
 public String currSize[] = null;

 public static String FAR4SHA1[] = null;
 public static String FAR4Size[] = null;

 public String PRFbSHA1;
 public String BPRbSHA1;
 public String IPReSHA1;
 
 public TreePath[] selectedPaths = null;
 
 public FileChooser fileDialogue = new FileChooser(this);

 public MainWindow() {     
  initComponents();
  PreviewLabel.setVisible(false);
  TextPrevScroll.setVisible(false);
  TextPreview.setVisible(false);
  setIconImage(new ImageIcon(getClass().getResource("resources/farctool2_icon.png")).getImage());
  aboutWindow.setIconImage(new ImageIcon(getClass().getResource("resources/farctool2_icon.png")).getImage());

  mapTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
  mapTree.addTreeSelectionListener((TreeSelectionEvent e) -> {
   DefaultMutableTreeNode node = (DefaultMutableTreeNode) mapTree.getLastSelectedPathComponent();
   selectedPaths = mapTree.getSelectionPaths();
   if (selectedPaths != null) {
    currSHA1 = new String[selectedPaths.length];
    currGUID = new String[selectedPaths.length];
    currFileName = new String[selectedPaths.length];
    currSize = new String[selectedPaths.length];
   } else {
    currSHA1 = null;
    currGUID = null;
    currFileName = null;
    currSize = null;
    selectedPaths = new TreePath[0];
   }

   for (int currentTreeNode = 0; currentTreeNode < selectedPaths.length; currentTreeNode++) {
    if (node == null) return;
    node = (DefaultMutableTreeNode) selectedPaths[currentTreeNode].getLastPathComponent();
    if (node.getChildCount() > 0) {
     currSHA1[currentTreeNode] = null;
     currGUID[currentTreeNode] = null;
     currFileName[currentTreeNode] = null;
     currSize[currentTreeNode] = null;
     continue;
    }
    if (mapTree.getSelectionPath().getPathCount() == 1) return;
    
    String[] test = new String[selectedPaths[currentTreeNode].getPathCount()];
    for (int i = 1; i < selectedPaths[currentTreeNode].getPathCount(); i++) test[i] = selectedPaths[currentTreeNode].getPathComponent(i).toString();
    String finalString = new String();
    for (int i = 1; i < selectedPaths[currentTreeNode].getPathCount(); i++) {
     finalString += test[i];
     if (i != selectedPaths[currentTreeNode].getPathCount() - 1) finalString += "/";
    }

    if (finalString.contains(".") && FAR4 == null) {
     currFileName[currentTreeNode] = finalString;
     EditorPanel.setValueAt(finalString, 0, 1);
     KMPMatch matcher = new KMPMatch();

     try {
      long offset = 0;
      offset = matcher.indexOf(Files.readAllBytes(MAP.toPath()), finalString.getBytes());
      try (RandomAccessFile mapAccess = new RandomAccessFile(MAP, "rw")) {
       mapAccess.seek(0);
       boolean lbp3map = MapUtils.isLBP3Map(mapAccess.readInt(), false);

       offset += finalString.length();

       if (lbp3map == false) offset += 4;
       
       mapAccess.seek(offset);

       // Get that timestamp, baby! //

       String fileTimeStamp = MiscUtils.leftPad(Integer.toHexString(mapAccess.readInt()), 8);
       EditorPanel.setValueAt(fileTimeStamp, 1, 2); //set hex timestamp
       Date readableDate = new Date();
       readableDate.setTime((long) Integer.parseInt(fileTimeStamp, 16) * 1000);
       EditorPanel.setValueAt(readableDate.toString(), 1, 1); //set readable timestamp

       // Get the size. //
       String fileSize = MiscUtils.leftPad(Integer.toHexString(mapAccess.readInt()), 8);
       currSize[currentTreeNode] = fileSize;
       EditorPanel.setValueAt(fileSize, 2, 2); //set hex filesize
       EditorPanel.setValueAt(Integer.parseInt(fileSize, 16), 2, 1); //set readable filesize

       // Get the SHA1. //
       byte[] rawHash = new byte[20];
       mapAccess.read(rawHash);
       String fileHash = MiscUtils.byteArrayToHexString(rawHash);
       EditorPanel.setValueAt(fileHash, 3, 2); //set hex hash
       currSHA1[currentTreeNode] = fileHash;
       EditorPanel.setValueAt(fileHash, 3, 1); //set readable hash (redundant)

       // Get the wildly exquisite GUID. //
       
       String fileGUID = MiscUtils.leftPad(Integer.toHexString(mapAccess.readInt()), 8);
       currGUID[currentTreeNode] = fileGUID;
       EditorPanel.setValueAt(fileGUID, 4, 2); //set hex guid
       EditorPanel.setValueAt("g" + Integer.parseInt(fileGUID, 16), 4, 1); //set readable guid
       
       // Get the esoteric GUID. // // The method for calculating this isn't finished, so it's just going to be left commented. //
       //String eGUID = MiscUtils.getEGUID(fileGUID);
       //EditorPanel.setValueAt(eGUID.toUpperCase(), 5, 1);
       //EditorPanel.setValueAt(eGUID.toUpperCase(), 5, 2);
       
      } catch (IOException ex) {
       Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
      }
     } catch (IOException ex) {
      Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
     }
    } else if (FAR4 != null) {
     DefaultMutableTreeNode myNode = (DefaultMutableTreeNode) selectedPaths[currentTreeNode].getLastPathComponent();
     int NodeIndex = myNode.getParent().getIndex(myNode);

     currFileName[currentTreeNode] = finalString;
     EditorPanel.setValueAt(finalString, 0, 1);

     EditorPanel.setValueAt(FAR4SHA1[NodeIndex], 3, 2); //set hex hash
     currSHA1[currentTreeNode] = FAR4SHA1[NodeIndex];
     EditorPanel.setValueAt(FAR4SHA1[NodeIndex], 3, 1); //set readable hash (redundant)

     currSize[currentTreeNode] = FAR4Size[NodeIndex];
     EditorPanel.setValueAt(FAR4Size[NodeIndex], 2, 2); //set hex filesize
     EditorPanel.setValueAt(Integer.parseInt(FAR4Size[NodeIndex], 16), 2, 1); //set readable filesize

     EditorPanel.setValueAt("N/A", 4, 2);
     EditorPanel.setValueAt("N/A", 4, 1);

     EditorPanel.setValueAt("N/A", 1, 2);
     EditorPanel.setValueAt("N/A", 1, 1);
    }
   }

   if ((FARC != null || FAR4 != null) && selectedPaths.length != 0 && currFileName[0] != null) {
    FileOutputStream fos = null;
    try {
     PreviewLabel.setVisible(true);
     PreviewLabel.setIcon(null);
     PreviewLabel.setText("No preview available :(");
     TextPrevScroll.setVisible(false);
     TextPreview.setVisible(false);
     byte[] workWithData = null;
     if (FAR4 != null)
      workWithData = FarUtils.pullFromFAR4(currFileName[currFileName.length - 1].split("[.]")[0], currSize[currFileName.length - 1], FAR4);
     else
      workWithData = FarUtils.pullFromFarc(currSHA1[currFileName.length - 1], FARC, true);
     if (workWithData == null) {
      System.out.println("As a result, I wasn't able to preview anything...");
      hexViewer.setData(null);
      hexViewer.setDefinitionStatus(DefinitionStatus.UNDEFINED);
      hexViewer.setEnabled(false);
      return;
     } else {
      try {
       fos = new FileOutputStream(new File("temp"));
      } catch (FileNotFoundException ex) {
       Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
      }
      fos.write(workWithData);
      fos.close();
     }
     if (
      workWithData[3] == 0x74 ||
      currFileName[currFileName.length - 1].contains(".nws") ||
      currFileName[currFileName.length - 1].contains(".txt") ||
      currFileName[currFileName.length - 1].contains(".rlst") ||
      currFileName[currFileName.length - 1].contains(".xml") ||
      currFileName[currFileName.length - 1].contains(".cha") ||
      currFileName[currFileName.length - 1].contains(".edset") ||
      currFileName[currFileName.length - 1].contains(".nws") ||
      currFileName[currFileName.length - 1].contains(".rlist") ||
      currFileName[currFileName.length - 1].contains(".sph")
     ) {
      //Text file we can read with the text preview pane
      PreviewLabel.setVisible(false);
      TextPrevScroll.setVisible(true);
      TextPreview.setVisible(true);
      TextPreview.setText(new String(workWithData));
      TextPreview.setCaretPosition(0);
     }
     if (currFileName[currFileName.length - 1].contains(".tex")) {
      try {
       ZlibUtils.decompressThis(workWithData, true);
       PreviewLabel.setVisible(true);
       TextPrevScroll.setVisible(false);
       TextPreview.setVisible(false);
       PreviewLabel.setText(null);
       PreviewLabel.setIcon(MiscUtils.createDDSIcon("temp_prev_tex"));
      } catch (DataFormatException | IOException ex) {
       Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
      }
     } else if (currFileName[currFileName.length - 1].contains(".png") || currFileName[currFileName.length - 1].contains(".jpg")) {
       try (InputStream in = new ByteArrayInputStream(workWithData)) {
        BufferedImage image = ImageIO.read( in );
        PreviewLabel.setVisible(true);
        TextPrevScroll.setVisible(false);
        TextPreview.setVisible(false);
        PreviewLabel.setText(null);
        PreviewLabel.setIcon(MiscUtils.getScaledImage(image));
       } catch (IOException ex) { Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex); }
     } else if (currFileName[currFileName.length - 1].contains(".dds")) {
      PreviewLabel.setVisible(true);
      TextPrevScroll.setVisible(false);
      TextPreview.setVisible(false);
      PreviewLabel.setText(null);
      PreviewLabel.setIcon(MiscUtils.createImageIconFromDDS(workWithData));
     }
     hexViewer.setData(new SimpleDataProvider(workWithData));
     hexViewer.setDefinitionStatus(DefinitionStatus.DEFINED);
     hexViewer.setEnabled(true);
    } catch (IOException ex) { Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex); }
   }
  });
  PrintStream out = new CustomPrintStream(new TextAreaOutputStream(OutputTextArea));
  System.setOut(out);
  System.setErr(out);
 }

 @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        PopUpMessage = new javax.swing.JOptionPane();
        aboutWindow = new javax.swing.JFrame();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        ConsolePopup = new javax.swing.JPopupMenu();
        Clear = new javax.swing.JMenuItem();
        jSplitPane1 = new javax.swing.JSplitPane();
        MapPanel = new javax.swing.JScrollPane();
        mapTree = new javax.swing.JTree();
        RightHandStuff = new javax.swing.JSplitPane();
        pnlOutput = new javax.swing.JPanel();
        mapLoadingBar = new javax.swing.JProgressBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        OutputTextArea = new javax.swing.JTextArea();
        jSplitPane2 = new javax.swing.JSplitPane();
        ToolsPanel = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
        PreviewPanel = new javax.swing.JPanel();
        hexViewer = new tv.porst.jhexview.JHexView();
        ToolsPanel2 = new javax.swing.JPanel();
        PreviewLabel = new javax.swing.JLabel();
        TextPrevScroll = new javax.swing.JScrollPane();
        TextPreview = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        EditorPanel = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        jMenu4 = new javax.swing.JMenu();
        openMAP = new javax.swing.JMenuItem();
        openFARC = new javax.swing.JMenuItem();
        openFAR4 = new javax.swing.JMenuItem();
        FileExportMenu = new javax.swing.JMenu();
        ExportTEXOptions = new javax.swing.JMenu();
        exportAsPNG = new javax.swing.JMenuItem();
        exportAsJPG = new javax.swing.JMenuItem();
        exportAsDDS = new javax.swing.JMenuItem();
        ExportMAPOptions = new javax.swing.JMenu();
        exportAsRLST = new javax.swing.JMenuItem();
        PLANExportOptions = new javax.swing.JMenu();
        exportAsMOD = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        closeApplication = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        addFileToFARC = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        ExtractionOptions = new javax.swing.JMenu();
        extractRaw = new javax.swing.JMenuItem();
        extractDecompressed = new javax.swing.JMenuItem();
        ReplaceSelectedOptions = new javax.swing.JMenu();
        replaceRaw = new javax.swing.JMenuItem();
        replaceDecompressed = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        addEntry = new javax.swing.JMenuItem();
        removeEntry = new javax.swing.JMenuItem();
        zeroEntry = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        reverseBytes = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        printDependencies = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        installMod = new javax.swing.JMenuItem();
        HelpMenu = new javax.swing.JMenu();
        openAboutFrame = new javax.swing.JMenuItem();
        toggleDarcula = new javax.swing.JCheckBoxMenuItem();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        aboutWindow.setTitle("farctool2 :)");
        aboutWindow.setAlwaysOnTop(true);
        aboutWindow.setMinimumSize(new java.awt.Dimension(450, 225));
        aboutWindow.setResizable(false);

        jTextArea2.setEditable(false);
        jTextArea2.setColumns(20);
        jTextArea2.setFont(new java.awt.Font("Courier New", 0, 10)); // NOI18N
        jTextArea2.setRows(5);
        jTextArea2.setText("Special thanks to:\n\n \"The man in the shadows\"\n                          Aluigi (http://zenhax.com)\n  http://xentax.com\n                              Jon, TBA, and friends\n     npedotnet (NPESDK_GWT) licensed under MIT license\n                 riversun (bigdoc) licensed under MIT license\n  Sporst (JHexView, splib) licensed under GPL 2.0");
        jTextArea2.setFocusable(false);
        jScrollPane4.setViewportView(jTextArea2);

        jLabel1.setFont(new java.awt.Font("Courier New", 0, 18)); // NOI18N
        jLabel1.setText("farctool2!");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/philosophofee/farctool2/windows/resources/1002.gif"))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        jLabel3.setText("written by Philosophofee");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout aboutWindowLayout = new javax.swing.GroupLayout(aboutWindow.getContentPane());
        aboutWindow.getContentPane().setLayout(aboutWindowLayout);
        aboutWindowLayout.setHorizontalGroup(
            aboutWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
            .addGroup(aboutWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutWindowLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        aboutWindowLayout.setVerticalGroup(
            aboutWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 225, Short.MAX_VALUE)
            .addGroup(aboutWindowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutWindowLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        Clear.setText("Clear");
        Clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearActionPerformed(evt);
            }
        });
        ConsolePopup.add(Clear);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("farctool2");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jSplitPane1.setDividerLocation(150);

        mapTree.setModel(null);
        mapTree.setMaximumSize(new java.awt.Dimension(72, 60));
        MapPanel.setViewportView(mapTree);

        jSplitPane1.setLeftComponent(MapPanel);

        RightHandStuff.setDividerLocation(400);
        RightHandStuff.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        OutputTextArea.setEditable(false);
        OutputTextArea.setColumns(20);
        OutputTextArea.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        OutputTextArea.setLineWrap(true);
        OutputTextArea.setRows(5);
        OutputTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                OutputTextAreaMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(OutputTextArea);

        javax.swing.GroupLayout pnlOutputLayout = new javax.swing.GroupLayout(pnlOutput);
        pnlOutput.setLayout(pnlOutputLayout);
        pnlOutputLayout.setHorizontalGroup(
            pnlOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mapLoadingBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 747, Short.MAX_VALUE)
        );
        pnlOutputLayout.setVerticalGroup(
            pnlOutputLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOutputLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mapLoadingBar, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        RightHandStuff.setRightComponent(pnlOutput);

        jSplitPane2.setDividerLocation(256);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jSplitPane3.setDividerLocation(256);

        javax.swing.GroupLayout PreviewPanelLayout = new javax.swing.GroupLayout(PreviewPanel);
        PreviewPanel.setLayout(PreviewPanelLayout);
        PreviewPanelLayout.setHorizontalGroup(
            PreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(hexViewer, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
        );
        PreviewPanelLayout.setVerticalGroup(
            PreviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(hexViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jSplitPane3.setRightComponent(PreviewPanel);

        PreviewLabel.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        PreviewLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        PreviewLabel.setAlignmentX(0.5F);

        TextPreview.setColumns(20);
        TextPreview.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        TextPreview.setRows(5);
        TextPrevScroll.setViewportView(TextPreview);

        javax.swing.GroupLayout ToolsPanel2Layout = new javax.swing.GroupLayout(ToolsPanel2);
        ToolsPanel2.setLayout(ToolsPanel2Layout);
        ToolsPanel2Layout.setHorizontalGroup(
            ToolsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 255, Short.MAX_VALUE)
            .addGroup(ToolsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(PreviewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE))
            .addGroup(ToolsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(TextPrevScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE))
        );
        ToolsPanel2Layout.setVerticalGroup(
            ToolsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 253, Short.MAX_VALUE)
            .addGroup(ToolsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(PreviewLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
            .addGroup(ToolsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(TextPrevScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
        );

        jSplitPane3.setLeftComponent(ToolsPanel2);

        javax.swing.GroupLayout ToolsPanelLayout = new javax.swing.GroupLayout(ToolsPanel);
        ToolsPanel.setLayout(ToolsPanelLayout);
        ToolsPanelLayout.setHorizontalGroup(
            ToolsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane3)
        );
        ToolsPanelLayout.setVerticalGroup(
            ToolsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane3)
        );

        jSplitPane2.setTopComponent(ToolsPanel);

        EditorPanel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Filename", null, null},
                {"Timestamp", null, null},
                {"Size", null, null},
                {"SHA1", null, null},
                {"GUID", null, null}
            },
            new String [] {
                "Variable", "Value", "Value (HEX)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(EditorPanel);

        jSplitPane2.setRightComponent(jScrollPane1);

        RightHandStuff.setLeftComponent(jSplitPane2);

        jSplitPane1.setRightComponent(RightHandStuff);

        FileMenu.setText("File");

        jMenu4.setText("Load");

        openMAP.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMAP.setText(".MAP");
        openMAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMAPActionPerformed(evt);
            }
        });
        jMenu4.add(openMAP);

        openFARC.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK));
        openFARC.setText(".FARC");
        openFARC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFARCActionPerformed(evt);
            }
        });
        jMenu4.add(openFARC);

        openFAR4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        openFAR4.setText(".FAR4");
        openFAR4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFAR4ActionPerformed(evt);
            }
        });
        jMenu4.add(openFAR4);

        FileMenu.add(jMenu4);

        FileExportMenu.setText("Export");
        FileExportMenu.setEnabled(false);

        ExportTEXOptions.setText(".TEX");
        ExportTEXOptions.setEnabled(false);

        exportAsPNG.setText(".PNG");
        exportAsPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsPNGActionPerformed(evt);
            }
        });
        ExportTEXOptions.add(exportAsPNG);

        exportAsJPG.setText(".JPG");
        exportAsJPG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsJPGActionPerformed(evt);
            }
        });
        ExportTEXOptions.add(exportAsJPG);

        exportAsDDS.setText(".DDS");
        exportAsDDS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsDDSActionPerformed(evt);
            }
        });
        ExportTEXOptions.add(exportAsDDS);

        FileExportMenu.add(ExportTEXOptions);

        ExportMAPOptions.setText(".MAP");
        ExportMAPOptions.setEnabled(false);

        exportAsRLST.setText(".RLST");
        exportAsRLST.setEnabled(false);
        exportAsRLST.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsRLSTActionPerformed(evt);
            }
        });
        ExportMAPOptions.add(exportAsRLST);

        FileExportMenu.add(ExportMAPOptions);

        PLANExportOptions.setText(".PLAN");
        PLANExportOptions.setEnabled(false);

        exportAsMOD.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        exportAsMOD.setText("Mod Package");
        exportAsMOD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAsMODActionPerformed(evt);
            }
        });
        PLANExportOptions.add(exportAsMOD);

        FileExportMenu.add(PLANExportOptions);

        FileMenu.add(FileExportMenu);
        FileMenu.add(jSeparator6);

        closeApplication.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        closeApplication.setText("Exit");
        closeApplication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeApplicationActionPerformed(evt);
            }
        });
        FileMenu.add(closeApplication);

        jMenuBar1.add(FileMenu);

        jMenu5.setText("FAR");

        addFileToFARC.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        addFileToFARC.setText("Add Files...");
        addFileToFARC.setEnabled(false);
        addFileToFARC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileToFARCActionPerformed(evt);
            }
        });
        jMenu5.add(addFileToFARC);
        jMenu5.add(jSeparator5);

        ExtractionOptions.setText("Extract Selected...");
        ExtractionOptions.setEnabled(false);

        extractRaw.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        extractRaw.setText("As-is");
        extractRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractRawActionPerformed(evt);
            }
        });
        ExtractionOptions.add(extractRaw);

        extractDecompressed.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        extractDecompressed.setText("Decompressed");
        extractDecompressed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractDecompressedActionPerformed(evt);
            }
        });
        ExtractionOptions.add(extractDecompressed);

        jMenu5.add(ExtractionOptions);

        ReplaceSelectedOptions.setText("Replace Selected...");
        ReplaceSelectedOptions.setEnabled(false);

        replaceRaw.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        replaceRaw.setText("As-is");
        replaceRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceRawActionPerformed(evt);
            }
        });
        ReplaceSelectedOptions.add(replaceRaw);

        replaceDecompressed.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        replaceDecompressed.setText("Decompressed");
        replaceDecompressed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceDecompressedActionPerformed(evt);
            }
        });
        ReplaceSelectedOptions.add(replaceDecompressed);

        jMenu5.add(ReplaceSelectedOptions);

        jMenuBar1.add(jMenu5);

        jMenu6.setText("MAP");

        addEntry.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        addEntry.setText("Add Entry...");
        addEntry.setEnabled(false);
        addEntry.setFocusCycleRoot(true);
        addEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEntryActionPerformed(evt);
            }
        });
        jMenu6.add(addEntry);

        removeEntry.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        removeEntry.setText("Remove Entry");
        removeEntry.setEnabled(false);
        removeEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeEntryActionPerformed(evt);
            }
        });
        jMenu6.add(removeEntry);

        zeroEntry.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        zeroEntry.setText("Zero Entry");
        zeroEntry.setEnabled(false);
        zeroEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroEntryActionPerformed(evt);
            }
        });
        jMenu6.add(zeroEntry);

        jMenuBar1.add(jMenu6);

        jMenu7.setText("Tools");

        reverseBytes.setText("Reverse Bytes...");
        reverseBytes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reverseBytesActionPerformed(evt);
            }
        });
        jMenu7.add(reverseBytes);
        jMenu7.add(jSeparator2);

        printDependencies.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        printDependencies.setText("Print Dependencies");
        printDependencies.setEnabled(false);
        printDependencies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printDependenciesActionPerformed(evt);
            }
        });
        jMenu7.add(printDependencies);

        jMenuBar1.add(jMenu7);

        jMenu3.setText("Mods");

        installMod.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        installMod.setText("Install Mod...");
        installMod.setEnabled(false);
        installMod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installModActionPerformed(evt);
            }
        });
        jMenu3.add(installMod);

        jMenuBar1.add(jMenu3);

        HelpMenu.setText("Help");

        openAboutFrame.setText("About");
        openAboutFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openAboutFrameActionPerformed(evt);
            }
        });
        HelpMenu.add(openAboutFrame);

        toggleDarcula.setSelected(true);
        toggleDarcula.setText("Darcula");
        toggleDarcula.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleDarculaActionPerformed(evt);
            }
        });
        HelpMenu.add(toggleDarcula);

        jMenuBar1.add(HelpMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


 private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
     // I don't even know what this is for, and I'm too lazy to look into it, so here it stays! //
 }//GEN-LAST:event_formWindowClosed

 private void printDependenciesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printDependenciesActionPerformed
  if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
  if (FARC == null) { showUserDialog("A bit of advice", "You kind of need a .FARC file opened to do anything with this."); return; }
  try {
   for (int pathCount = 0; pathCount < currSHA1.length; pathCount++) {
    if (currFileName[pathCount] == null) continue;
    byte[] bytesToRead = FarUtils.pullFromFarc(currSHA1[pathCount], FARC, false);
    ByteArrayInputStream fileAccess = new ByteArrayInputStream(bytesToRead);
    fileAccess.skip(8);
    //Get dependencies offset
    byte[] offsetDependenciesByte = new byte[4];
    fileAccess.read(offsetDependenciesByte);
    int offsetDependencies = Integer.parseInt(MiscUtils.byteArrayToHexString(offsetDependenciesByte), 16);
    System.out.println("Dependencies offset: " + offsetDependencies);

    fileAccess.skip(offsetDependencies - 12);

    byte[] dependenciesCountByte = new byte[4];
    fileAccess.read(dependenciesCountByte);
    int dependenciesCount = Integer.parseInt(MiscUtils.byteArrayToHexString(dependenciesCountByte), 16);

    System.out.println("Dependencies count: " + dependenciesCount);

    byte[] dependencyKindByte = new byte[1];
    byte[] dependencyGUIDByte = new byte[4];
    int dependencyGUID = 0;
    byte[] dependencyTypeByte = new byte[4];
    int dependencyType = 0;
    boolean levelFail = false;

    for (int i = 0; i < dependenciesCount; i++) {
     fileAccess.read(dependencyKindByte);
     if (dependencyKindByte[0] == 0x01) fileAccess.skip(24); 
     if (dependencyKindByte[0] == 0x02) {
      fileAccess.read(dependencyGUIDByte);
      dependencyGUID = Integer.parseInt(MiscUtils.byteArrayToHexString(dependencyGUIDByte), 16);
      fileAccess.read(dependencyTypeByte);
      dependencyType = Integer.parseInt(MiscUtils.byteArrayToHexString(dependencyTypeByte), 16);
      String fileNameNew = MiscUtils.getFileNameFromGUID(MiscUtils.byteArrayToHexString(dependencyGUIDByte), MAP);
      if (fileNameNew.contains("Error")) levelFail = true;
      System.out.println((i + 1) + ": " + fileNameNew + " | " + "g" + dependencyGUID + " | " + dependencyType);
     }
    }
    if (levelFail) System.out.println("ERROR! The level contains at least one dependency that does not exist. You will have problems.");
   }
  } catch (IOException ex) { Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex); }
 }//GEN-LAST:event_printDependenciesActionPerformed

 private void openAboutFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openAboutFrameActionPerformed
  aboutWindow.setVisible(true);
 }//GEN-LAST:event_openAboutFrameActionPerformed

 private void extractRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractRawActionPerformed
   extract(false);
 }//GEN-LAST:event_extractRawActionPerformed

 private void exportTEX(String type) {
  if (FAR4 == null) {
   if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
   if (FARC == null) { showUserDialog("A bit of advice", "You kind of need a .FARC file opened to do anything with this."); return; }
  }
  
  File file = null;
  String outputFileName;
  String selectedDirectory = "";

  if (currSHA1.length == 1) {
   outputFileName = currFileName[0].substring(currFileName[0].lastIndexOf("/") + 1) + "." + type;
   file = fileDialogue.openFile(outputFileName, "", "", true);
   if (file == null) { System.out.println("File access cancelled by user."); return; }
   } else {
      selectedDirectory = fileDialogue.openDirectory();
      if (selectedDirectory == null || selectedDirectory.isEmpty()) { System.out.println("File access cancelled by user."); return; }
   }

  for (int pathCount = 0; pathCount < currSHA1.length; pathCount++) {
   if (currFileName[pathCount] == null) continue;
   if (!currFileName[pathCount].contains(".tex")) continue;
   
   try {
    byte[] bytesToSave;
    if (FAR4 == null) bytesToSave = FarUtils.pullFromFarc(currSHA1[pathCount], FARC, false);
    else bytesToSave = FarUtils.pullFromFAR4(currFileName[pathCount].split("[.]")[0], currSize[pathCount], FAR4);
    if (currSHA1.length != 1)
    {
     outputFileName = selectedDirectory;
     outputFileName = outputFileName + "\\" + currFileName[pathCount].substring(currFileName[pathCount].lastIndexOf("/") + 1);
     outputFileName = outputFileName.substring(0, outputFileName.length() - 4) + "." + type;
     file = new File(outputFileName);
    }

    byte[] buffer = ZlibUtils.decompressThis(bytesToSave, false);
    if (type == "png" || type == "jpg") MiscUtils.DDStoStandard(buffer, type, file);
    else { FileOutputStream fos = new FileOutputStream(file); fos.write(bytesToSave); fos.close(); }
   } catch (DataFormatException | IOException | NullPointerException ex) {
    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
   }
  }
  System.out.println("Textures have succesfully been exported.");  
 }
 
 private void exportAsPNGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsPNGActionPerformed
   exportTEX("png");
 }//GEN-LAST:event_exportAsPNGActionPerformed

 private void exportAsDDSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsDDSActionPerformed
  exportTEX("dds");
 }//GEN-LAST:event_exportAsDDSActionPerformed

 private void exportAsJPGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsJPGActionPerformed
  exportTEX("jpg");
 }//GEN-LAST:event_exportAsJPGActionPerformed

 private void closeApplicationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeApplicationActionPerformed
  System.out.println("Well, I guess it's time for a nap.");
  System.exit(0);
 }//GEN-LAST:event_closeApplicationActionPerformed

 private void openFARCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFARCActionPerformed
  if (MAP == null) showUserDialog("Warning", "Please keep in mind, opening a .FARC file alone will not display anything within farctool2. A .MAP file is required for most functions.");
  File[] newFARCs = fileDialogue.openFiles(".FARC", "FARC Files");
  if (newFARCs == null) { System.out.println("File access cancelled by user."); return; }
  else FARC = newFARCs;
  String printStatement = "Successfully opened FARCs: ";
  for (int i = 0; i < FARC.length; i++) printStatement += FARC[i].getName() + " ";
  System.out.println(printStatement);
  enableFARCMenus();
  if (FAR4 != null) {
    FAR4 = null;
    this.mapTree.setModel(null);
    this.mapTree.updateUI();
  }
 }//GEN-LAST:event_openFARCActionPerformed

 private void openMAPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMAPActionPerformed
   File newMAP = fileDialogue.openFile("", ".MAP", "MAP Files", false);
   if (newMAP == null) { System.out.println("File access cancelled by user."); return; }
   else MAP = newMAP;
   System.out.println("Sucessfully opened " + MAP.getName());
   System.out.println("A haiku for the impatient:\n" +
   "Map parsing takes time.\n" +
   "I might freeze, I have not crashed.\n" +
   "Wait, please bear with me!");

   MapUtils self = new MapUtils();
   DefaultMutableTreeNode root = new DefaultMutableTreeNode(MAP.getName());
   
   mapTree.setModel(null);
   DefaultTreeModel model = self.parseMapIntoMemory(root, MAP);
   mapTree.setModel(model);
   
   enableMAPMenus();
   FAR4 = null;
 }//GEN-LAST:event_openMAPActionPerformed

 private void addEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEntryActionPerformed
  if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
  EntryAdditionWindow EntryWindow = new EntryAdditionWindow(this);
  EntryWindow.setVisible(true);
 }//GEN-LAST:event_addEntryActionPerformed

 private void zeroEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroEntryActionPerformed
  if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
  for (int pathCount = 0; pathCount < currFileName.length; pathCount++) {
   if (currFileName[pathCount] == null) continue;
   KMPMatch matcher = new KMPMatch();
   long offset = 0;
   boolean lbp3map = false;
   try {
    offset = matcher.indexOf(Files.readAllBytes(MAP.toPath()), currFileName[pathCount].getBytes());
   } catch (IOException ex) {
    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
   }
   try (RandomAccessFile mapAccess = new RandomAccessFile(MAP, "rw")) {
    mapAccess.seek(0);
    if (mapAccess.readInt() == 21496064) lbp3map = true;
    mapAccess.seek(offset);
    offset += currFileName[pathCount].length();
    mapAccess.seek(offset);
    if (lbp3map == false) {
     offset += 4;
     mapAccess.seek(offset);
    }
    byte buffer[] = new byte[28];
    mapAccess.write(buffer, 0, 28);
    mapAccess.close();
   } catch (Exception e) {}
  }
  System.out.println("Successfully zeroed entries.");
 }//GEN-LAST:event_zeroEntryActionPerformed

 private void removeEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEntryActionPerformed
  if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
  for (int pathCount = 0; pathCount < currFileName.length; pathCount++) {
   if (currFileName[pathCount] == null) continue;
   DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPaths[pathCount].getLastPathComponent();
   if (node.getChildCount() > 0) {
    System.out.println("You can't delete a folder that has contents in it!");
    return;
   }
   KMPMatch matcher = new KMPMatch();
   long offset = 0;
   boolean lbp3map = false;
   try {
    offset = matcher.indexOf(Files.readAllBytes(MAP.toPath()), currFileName[pathCount].getBytes());
   } catch (IOException ex) {
    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
   }
   try (RandomAccessFile mapAccess = new RandomAccessFile(MAP, "rw")) {
    mapAccess.seek(0);
    if (mapAccess.readInt() == 21496064) lbp3map = true;
    mapAccess.seek(offset);

    mapAccess.seek(0);
    byte prev[] = new byte[(int) offset - 1];
    mapAccess.read(prev);

    Boolean lastEntry = mapAccess.length() - (offset + currFileName[pathCount].length()) == 36;

    byte next[] = null;
    if (!lastEntry) {
     offset += currFileName[pathCount].length() + (lbp3map ? 33 : 39);
     mapAccess.seek(offset);

     next = new byte[(int) mapAccess.length() - (int) offset];
     mapAccess.read(next);
    }


    mapAccess.setLength(0);
    if (!lastEntry)
     mapAccess.setLength(prev.length + next.length);
    else mapAccess.setLength(prev.length);

    mapAccess.write(prev);
    if (!lastEntry)
     mapAccess.write(next);

    mapAccess.seek(4);
    int entries = mapAccess.readInt();
    entries -= 1;

    mapAccess.seek(4);
    mapAccess.writeInt(entries);

    if (lastEntry)
     mapAccess.setLength(mapAccess.length() - 3);

    mapAccess.close();

    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
    node.removeFromParent();

    Boolean parentNodes = true;
    while (parentNodes) {
     if (parent.isLeaf()) {
      node = (DefaultMutableTreeNode) parent.getParent();
      parent.removeFromParent();
      parent = node;
     } else parentNodes = false;
    }
    mapTree.updateUI();


   } catch (Exception e) {}
  }
  System.out.println("Successfully removed entries.");
 }//GEN-LAST:event_removeEntryActionPerformed

 private void extract(boolean decompress) {
  if (FAR4 == null) {
   if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
   if (FARC == null) { showUserDialog("A bit of advice", "You kind of need a .FARC file opened to do anything with this."); return; }
  }
  
  File file = null;
  String outputFileName;
  String selectedDirectory = "";
  
  if (currSHA1.length == 1) {
   outputFileName = currFileName[0].substring(currFileName[0].lastIndexOf("/") + 1);
   file = fileDialogue.openFile(outputFileName, "", "", true);
   if (file == null) { System.out.println("File access cancelled by user."); return; }
  } else {
      selectedDirectory = fileDialogue.openDirectory();
      if (selectedDirectory == null || selectedDirectory.isEmpty()) { System.out.println("File access cancelled by user."); return; }
  }

  for (int pathCount = 0; pathCount < currSHA1.length; pathCount++) {
   if (currFileName[pathCount] == null) continue;
   byte[] bytesToSave = null;
   try {
    if (FAR4 == null) 
    {
        if (decompress) bytesToSave = ZlibUtils.decompressThis(FarUtils.pullFromFarc(currSHA1[pathCount], FARC, false), false);
        else bytesToSave = FarUtils.pullFromFarc(currSHA1[pathCount], FARC, false);
    }
    else
    {
        if (decompress) bytesToSave = ZlibUtils.decompressThis(FarUtils.pullFromFAR4(currFileName[pathCount].split("[.]")[0], currSize[pathCount], FAR4), false);
        else bytesToSave = FarUtils.pullFromFAR4(currFileName[pathCount].split("[.]")[0], currSize[pathCount], FAR4);
    }
   } catch (DataFormatException ex) { Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex); }

   if (currSHA1.length != 1) {
    outputFileName = selectedDirectory;
    outputFileName = outputFileName + "\\" + currFileName[pathCount].substring(currFileName[pathCount].lastIndexOf("/") + 1);
    if (decompress)
    {
       if (outputFileName.contains(".tex")) outputFileName = outputFileName.substring(0, outputFileName.length() - 4) + ".dds";   
    }
    file = new File(outputFileName);
   }

   try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(bytesToSave); } 
   catch (IOException ex) {}
  }
  System.out.println("Files have succesfully been extracted.");
 }
 private void extractDecompressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractDecompressedActionPerformed
   extract(true);
 }//GEN-LAST:event_extractDecompressedActionPerformed

 private void replaceRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceRawActionPerformed
  if (FARC == null && FAR4 == null) { showUserDialog("A bit of advice", "You kind of need a .FARC file opened to do anything with this."); return; }
  if (IPReSHA1 != null) { System.out.println("Replacement of files in littlefart files is currently not functional due to the encryption of the IPRe."); return; }
  File file = fileDialogue.openFile("", "", "", false);
  if (file == null) { System.out.println("File access cancelled by user."); return; }
  try {
    if (FAR4 == null) {
     File[] selectedFARCs = getSelectedFARCs();
     if (selectedFARCs == null) { System.out.println("File access cancelled by user."); return; }
     FarUtils.addFile(file, selectedFARCs);
     byte[] SHA1 = MiscUtils.getSHA1(file);
     String fileName = "";
     for (int pathCount = 0; pathCount < currSHA1.length; pathCount++) {
      if (currFileName[pathCount] == null) continue;
      fileName = currFileName[pathCount];
      if (currFileName[pathCount].contains(".tex") && !file.getName().contains(".tex")) {
       if (fileName != null) {
        fileName = fileName.substring(0, fileName.length() - 3);
       }
       if (file.getName() != null) {
        String path = file.getName();
        String[] paths = path.split("[.]");
        fileName += paths[paths.length - 1];
       }
      }
      MiscUtils.replaceEntryByGUID(currGUID[pathCount], fileName, Integer.toHexString((int) file.length()), MiscUtils.byteArrayToHexString(SHA1), this);
     }
    }
    else
    {
        byte[] data = Files.readAllBytes(file.toPath());
        for (int pathCount = 0; pathCount < currSHA1.length; pathCount++) FarUtils.rebuildFAR4(this, currSHA1[pathCount], data);
    }
   } catch (Exception ex) {
    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
   }
   if (FAR4 == null)
   {
      ((DefaultTreeModel) mapTree.getModel()).reload((DefaultMutableTreeNode) mapTree.getModel().getRoot());
      mapTree.updateUI();   
   }
   else FarUtils.openFAR4(this);
   System.out.println("Files have succesfully been replaced.");
 }//GEN-LAST:event_replaceRawActionPerformed

 private void addFileToFARCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileToFARCActionPerformed
  if (FARC == null) { showUserDialog("A bit of advice", "You kind of need a .FARC file opened to do anything with this."); return; }
  File[] files = fileDialogue.openFiles("", "");
  if (files == null || files.length == 0) { System.out.println("File access cancelled by user."); return; }
  File[] SelectedFARCs = getSelectedFARCs();
  if (SelectedFARCs == null) { System.out.println("File access cancelled by user."); return; }
  try { for (int i = 0; i < files.length; i++) FarUtils.addFile(files[i], SelectedFARCs); } 
  catch (Exception ex) { Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex); }
  System.out.println("Files have successfully been added to the FARC.");
 }//GEN-LAST:event_addFileToFARCActionPerformed

 public File[] getSelectedFARCs()
 {
     if (FARC.length > 1)
     {
        FARChooser farChooser = new FARChooser(this, true);
        farChooser.setTitle("FAR Chooser");
        return farChooser.getSelected();   
     }
     return FARC;
 }
 
 private void installModActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installModActionPerformed
  if (FARC == null) { showUserDialog("A bit of advice", "You kind of need a .FARC file opened to do anything with this."); return; }
  if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
  try {
    File modInfo = fileDialogue.openFile("", ".XML", "XML Files", false);
    if (modInfo == null) { System.out.println("File access cancelled by user."); return; } 
    File[] selectedFARCs = getSelectedFARCs();
    if (selectedFARCs == null) { System.out.println("File access cancelled by user."); return; }
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder;
    dBuilder = dbFactory.newDocumentBuilder();
    Document mod = dBuilder.parse(modInfo);
    mod.getDocumentElement().normalize();
    String directory = modInfo.getParent() + "/";
    ModInstaller installer = new ModInstaller(mod, directory, selectedFARCs, this);
  } catch (ParserConfigurationException | SAXException | IOException ex) {
    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
  }
 }//GEN-LAST:event_installModActionPerformed

 private void reverseBytesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reverseBytesActionPerformed
     try {
         File file = fileDialogue.openFile("", "", "", false);
         if (file == null) { System.out.println("File access cancelled by user."); return; }
         MiscUtils.reverseBytes(file);
     } catch (IOException ex) {
         Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
     }
 }//GEN-LAST:event_reverseBytesActionPerformed

 // I really need to clean up this mess of a function at some point. Why did I just copy the entire function twice for sub-levels? I should add a seperate function for that. Later though, I need a nap. //
 private void exportAsMODActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsMODActionPerformed
  if (FARC == null) { showUserDialog("A bit of advice", "You kind of need a .FARC file opened to do anything with this."); return; }
  if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
  
  
  String selectedDirectory = fileDialogue.openDirectory();
  if (selectedDirectory == null || selectedDirectory.isEmpty()) { System.out.println("File access cancelled by user."); return; }
  
  if (!currFileName[0].contains(".plan")) return;
  try {
   byte[] bytesToRead = FarUtils.pullFromFarc(currSHA1[0], FARC, false);
   ByteArrayInputStream fileAccess = new ByteArrayInputStream(bytesToRead);
   fileAccess.skip(8);

   byte[] offsetDependenciesByte = new byte[4];
   fileAccess.read(offsetDependenciesByte);
   int offsetDependencies = Integer.parseInt(MiscUtils.byteArrayToHexString(offsetDependenciesByte), 16);

   fileAccess.skip(offsetDependencies - 12);

   byte[] dependenciesCountByte = new byte[4];
   fileAccess.read(dependenciesCountByte);
   int dependenciesCount = Integer.parseInt(MiscUtils.byteArrayToHexString(dependenciesCountByte), 16);

   byte[] dependencyKindByte = new byte[1];
   byte[] dependencyGUIDByte = new byte[4];
   int dependencyGUID = 0;
   byte[] dependencyTypeByte = new byte[4];
   int dependencyType = 0;
   boolean levelFail = false;

   DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
   DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

   Document doc = docBuilder.newDocument();

   Element rootElement = doc.createElement("mod");

   doc.appendChild(rootElement);

   Attr description = doc.createAttribute("description");
   description.setValue("Automatically packed mod : " + currFileName[0].substring(currFileName[0].lastIndexOf("/") + 1));

   Attr game = doc.createAttribute("game");
   game.setValue("any");

   Attr icon = doc.createAttribute("icon");
   icon.setValue("icon.png");

   Attr name = doc.createAttribute("name");

   String nameOfNode = currFileName[0].substring(currFileName[0].lastIndexOf("/") + 1);
   name.setValue(nameOfNode.substring(0, nameOfNode.length() - 5));

   rootElement.setAttributeNode(name);
   rootElement.setAttributeNode(description);
   rootElement.setAttributeNode(icon);
   rootElement.setAttributeNode(game);

   byte[] buffer = FarUtils.pullFromFarc(currSHA1[0], FARC, false);
   File fileToPack = new File(selectedDirectory + nameOfNode.substring(0, nameOfNode.length() - 5) + "/files/" + nameOfNode);
   fileToPack.getParentFile().mkdirs();
   fileToPack.createNewFile();
   try (FileOutputStream fileToPackStream = new FileOutputStream(fileToPack)) {
    fileToPackStream.write(buffer);
   }

   Element fileNode = doc.createElement("file");
   rootElement.appendChild(fileNode);

   Attr guid = doc.createAttribute("guid");
   guid.setValue(currGUID[0]);
   fileNode.setAttributeNode(guid);

   Element path = doc.createElement("path");
   path.appendChild(doc.createTextNode("files/" + nameOfNode));

   fileNode.appendChild(path);

   Element mapNode = doc.createElement("map");
   mapNode.appendChild(doc.createTextNode(currFileName[0]));

   fileNode.appendChild(mapNode);

   for (int i = 0; i < dependenciesCount; i++) {
    fileAccess.read(dependencyKindByte);
    if (dependencyKindByte[0] == 0x01) {
     fileAccess.skip(20); //sha1
     fileAccess.skip(4);
    }
    if (dependencyKindByte[0] == 0x02) {
     fileAccess.read(dependencyGUIDByte);
     dependencyGUID = Integer.parseInt(MiscUtils.byteArrayToHexString(dependencyGUIDByte), 16);

     fileAccess.read(dependencyTypeByte);
     dependencyType = Integer.parseInt(MiscUtils.byteArrayToHexString(dependencyTypeByte), 16);
     String fileNameNew = MiscUtils.getFileNameFromGUID(MiscUtils.byteArrayToHexString(dependencyGUIDByte), MAP);
     String Hash = MiscUtils.getHashFromGUID(MiscUtils.byteArrayToHexString(dependencyGUIDByte), MAP);
     if ("NULL".equals(Hash))
      continue;
     byte[] file = FarUtils.pullFromFarc(Hash, FARC, false);
     if (file == null)
      continue;
     if (fileNameNew.contains(".gmat") || fileNameNew.contains(".mol")) {
      byte[] innerBytesToRead = FarUtils.pullFromFarc(Hash, FARC, false);
      ByteArrayInputStream innerFileAccess = new ByteArrayInputStream(innerBytesToRead);
      innerFileAccess.skip(8);

      byte[] innerOffsetDependenciesByte = new byte[4];
      innerFileAccess.read(innerOffsetDependenciesByte);
      int innerOffsetDependencies = Integer.parseInt(MiscUtils.byteArrayToHexString(innerOffsetDependenciesByte), 16);

      innerFileAccess.skip(innerOffsetDependencies - 12);

      byte[] innerDependenciesCountByte = new byte[4];
      innerFileAccess.read(innerDependenciesCountByte);
      int innerDependenciesCount = Integer.parseInt(MiscUtils.byteArrayToHexString(innerDependenciesCountByte), 16);
      byte[] innerDependencyKindByte = new byte[1];
      byte[] innerDependencyGUIDByte = new byte[4];
      int innerDependencyGUID = 0;
      byte[] innerDependencyTypeByte = new byte[4];
      int innerDependencyType = 0;
      for (int j = 0; j < innerDependenciesCount; j++) {
       innerFileAccess.read(innerDependencyKindByte);
       if (dependencyKindByte[0] == 0x01) {
        innerFileAccess.skip(20); //sha1
        innerFileAccess.skip(4);
       }
       if (innerDependencyKindByte[0] == 0x02) {
        innerFileAccess.read(innerDependencyGUIDByte);
        innerDependencyGUID = Integer.parseInt(MiscUtils.byteArrayToHexString(innerDependencyGUIDByte), 16);

        innerFileAccess.read(innerDependencyTypeByte);
        innerDependencyType = Integer.parseInt(MiscUtils.byteArrayToHexString(innerDependencyTypeByte), 16);
        String innerFileNameNew = MiscUtils.getFileNameFromGUID(MiscUtils.byteArrayToHexString(innerDependencyGUIDByte), MAP);
        String innerHash = MiscUtils.getHashFromGUID(MiscUtils.byteArrayToHexString(innerDependencyGUIDByte), MAP);
        if ("NULL".equals(innerHash))
         continue;
        byte[] innerFile = FarUtils.pullFromFarc(innerHash, FARC, false);
        if (innerFile == null)
         continue;
        String innerOutputFileName = selectedDirectory + currFileName[0].substring(currFileName[0].lastIndexOf("/") + 1);
        innerOutputFileName = innerOutputFileName.substring(0, innerOutputFileName.length() - 5) + "/files/" + innerFileNameNew.substring(innerFileNameNew.lastIndexOf("/") + 1);

        if (!innerFileNameNew.contains(".tex")) {
         File innerMyFile = new File(innerOutputFileName);
         innerMyFile.getParentFile().mkdirs();
         innerMyFile.createNewFile();
         try (FileOutputStream innerOutputFile = new FileOutputStream(innerMyFile)) {
          innerOutputFile.write(innerFile);
         }
        } else {
         innerOutputFileName = innerOutputFileName.substring(0, innerOutputFileName.length() - 3) + "png";
         ImageIO.write(MiscUtils.DDStoPNG(ZlibUtils.decompressThis(innerFile, false)), "png", new File(innerOutputFileName));
        }

        fileNode = doc.createElement("file");
        rootElement.appendChild(fileNode);

        guid = doc.createAttribute("guid");
        guid.setValue(MiscUtils.byteArrayToHexString(innerDependencyGUIDByte));
        fileNode.setAttributeNode(guid);

        path = doc.createElement("path");
        String innerPathName = "files/" + innerFileNameNew.substring(innerFileNameNew.lastIndexOf("/") + 1);
        if (innerPathName.contains(".tex"))
         innerPathName = innerPathName.substring(0, innerPathName.length() - 3) + "png";
        path.appendChild(doc.createTextNode(innerPathName));

        fileNode.appendChild(path);

        mapNode = doc.createElement("map");
        if (innerFileNameNew.contains(".tex"))
         innerFileNameNew = innerFileNameNew.substring(0, innerFileNameNew.length() - 3) + "png";
        mapNode.appendChild(doc.createTextNode(innerFileNameNew));

        fileNode.appendChild(mapNode);
       }
      }

     }
     String outputFileName = selectedDirectory + currFileName[0].substring(currFileName[0].lastIndexOf("/") + 1);
     outputFileName = outputFileName.substring(0, outputFileName.length() - 5) + "/files/" + fileNameNew.substring(fileNameNew.lastIndexOf("/") + 1);

     if (!fileNameNew.contains(".tex")) {
      File myFile = new File(outputFileName);
      myFile.getParentFile().mkdirs();
      myFile.createNewFile();
      try (FileOutputStream outputFile = new FileOutputStream(myFile)) {
       outputFile.write(file);
      }
     } else {
      outputFileName = outputFileName.substring(0, outputFileName.length() - 3) + "png";
      ImageIO.write(MiscUtils.DDStoPNG(ZlibUtils.decompressThis(file, false)), "png", new File(outputFileName));
     }

     fileNode = doc.createElement("file");
     rootElement.appendChild(fileNode);

     guid = doc.createAttribute("guid");
     guid.setValue(MiscUtils.byteArrayToHexString(dependencyGUIDByte));
     fileNode.setAttributeNode(guid);

     path = doc.createElement("path");
     String pathName = "files/" + fileNameNew.substring(fileNameNew.lastIndexOf("/") + 1);
     if (pathName.contains(".tex"))
      pathName = pathName.substring(0, pathName.length() - 3) + "png";
     path.appendChild(doc.createTextNode(pathName));

     fileNode.appendChild(path);

     mapNode = doc.createElement("map");
     if (fileNameNew.contains(".tex"))
      fileNameNew = fileNameNew.substring(0, fileNameNew.length() - 3) + "png";
     mapNode.appendChild(doc.createTextNode(fileNameNew));

     fileNode.appendChild(mapNode);

    }

   }
   TransformerFactory transformerFactory = TransformerFactory.newInstance();
   Transformer transformer = transformerFactory.newTransformer();
   DOMSource source = new DOMSource(doc);
   String outputFileName = selectedDirectory + currFileName[0].substring(currFileName[0].lastIndexOf("/") + 1);
   outputFileName = outputFileName.substring(0, outputFileName.length() - 5);
   StreamResult result = new StreamResult(new File(outputFileName + "/mod.xml"));
   transformer.transform(source, result);

   InputStream fis = getClass().getResourceAsStream("resources/icon.png");
   FileOutputStream fos = new FileOutputStream(outputFileName + "/icon.png");
   byte[] imageBuffer = fis.readAllBytes();
   fos.write(imageBuffer);
   fos.close();
   fis.close();
   System.out.println("Successfully packed .PLAN into a moddable format.");
  } catch (IOException | NullPointerException | ParserConfigurationException | TransformerException | DataFormatException ex) {
   Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
  }

 }//GEN-LAST:event_exportAsMODActionPerformed

 private void exportAsRLSTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAsRLSTActionPerformed
  if (MAP == null) { showUserDialog("A bit of advice", "You kind of need a .MAP file opened to do anything with this."); return; }
  String output = "";
  try (DataInputStream mapAccess = new DataInputStream(new FileInputStream(MAP))) { 
    boolean lbp3map = MapUtils.isLBP3Map(mapAccess.readInt(), false);
    int mapEntries = mapAccess.readInt();
    
    int fileNameLength = 0;
    String fileName = "";
    for (int i = 0; i < mapEntries; i++) {
     if (!lbp3map)
      mapAccess.skip(2);

     fileNameLength = mapAccess.readShort();
     byte[] fileNameBytes = new byte[fileNameLength];
     mapAccess.read(fileNameBytes);
     fileName = new String(fileNameBytes);

     if (fileName.contains(".plan") || fileName.contains(".pal"))
            output += fileName + "\n";   

     if (!lbp3map)
      mapAccess.skip(4);
     
     mapAccess.skip(32);
    }
    PrintWriter out = new PrintWriter("inventory.rlst"); out.println(output); out.close();
    System.out.println("Successfully created RLST from MAP.");
  } catch (FileNotFoundException ex) {} catch (IOException ex) {}
 }//GEN-LAST:event_exportAsRLSTActionPerformed

 private void OutputTextAreaMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OutputTextAreaMouseReleased
  if (evt.isPopupTrigger()) ConsolePopup.show(OutputTextArea, evt.getX(), evt.getY());
 }//GEN-LAST:event_OutputTextAreaMouseReleased

 private void ClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearActionPerformed
  OutputTextArea.selectAll();
  OutputTextArea.replaceSelection("");
 }//GEN-LAST:event_ClearActionPerformed

 private void openFAR4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFAR4ActionPerformed
  showUserDialog("Warning", "Editing your save file may be dangerous, be sure to keep a backup.");
  if (IPReSHA1 != null) { System.out.println("Replacement of files in littlefart files is currently not functional due to the encryption of the IPRe."); return; }
  File newFAR4 = fileDialogue.openFile("", "", "", false);
  if (newFAR4 == null) { System.out.println("File access cancelled by user."); return; }
  else FAR4 = newFAR4;
  FarUtils.openFAR4(this);
 }//GEN-LAST:event_openFAR4ActionPerformed

 private void replaceDecompressedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceDecompressedActionPerformed
  if (FARC == null && FAR4 == null) { showUserDialog("A bit of advice", "You kind of need a FAR file opened to do anything with this."); return; }
  if (IPReSHA1 != null) { System.out.println("Replacement of files in littlefart files is currently not functional due to the encryption of the IPRe."); return; }
  if (currSHA1.length != 1) { showUserDialog("Warning", "This function can only be used with one file at a time to prevent errors."); return; }
  File file = fileDialogue.openFile("", "", "", false);
  if (file == null) { System.out.println("File access cancelled by user."); return; }
  try {
    byte[] data = ZlibUtils.compressFile(new File("temp"), file, false);
    if (FARC != null)
    {
        File[] selectedFARCs = getSelectedFARCs();
        if (selectedFARCs == null) { System.out.println("File access cancelled by user."); return; }
        FarUtils.addFile(data, selectedFARCs);
        MiscUtils.replaceEntryByGUID(currGUID[0], currFileName[0], Integer.toHexString((int) data.length), MiscUtils.byteArrayToHexString(MiscUtils.getSHA1(data)), this);   
    }
    else 
    {
        FarUtils.rebuildFAR4(this, currSHA1[0], data);
    }

   } catch (Exception ex) {
    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
   }
   if (FAR4 == null)
   {
      ((DefaultTreeModel) mapTree.getModel()).reload((DefaultMutableTreeNode) mapTree.getModel().getRoot());
      mapTree.updateUI();   
   }
   else FarUtils.openFAR4(this);
   System.out.println("Files have succesfully been replaced.");
 }//GEN-LAST:event_replaceDecompressedActionPerformed

    private void toggleDarculaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleDarculaActionPerformed
       // TODO: fix
       /*
       if (toggleDarcula.isSelected()) 
       {
            BasicLookAndFeel darcula = new DarculaLaf();
            try { UIManager.setLookAndFeel(darcula); } 
            catch (UnsupportedLookAndFeelException ex) { Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex); }
       }
       else 
       {
           try {
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
           } catch (ClassNotFoundException ex) {
               Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
           } catch (InstantiationException ex) {
               Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
           } catch (IllegalAccessException ex) {
               Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
           } catch (UnsupportedLookAndFeelException ex) {
               Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
       SwingUtilities.updateComponentTreeUI(this);
       SwingUtilities.updateComponentTreeUI(fileDialogue.fileDialogue);
       */
    }//GEN-LAST:event_toggleDarculaActionPerformed

 public void disableFARCMenus() {
  ReplaceSelectedOptions.setEnabled(true);
  addFileToFARC.setEnabled(false);
  ReplaceSelectedOptions.setEnabled(true);
  addEntry.setEnabled(false);
  removeEntry.setEnabled(false);
  zeroEntry.setEnabled(false);
  exportAsRLST.setEnabled(false);
  ExportMAPOptions.setEnabled(false);
  installMod.setEnabled(false);
  PLANExportOptions.setEnabled(false);
  printDependencies.setEnabled(false);
  ExportTEXOptions.setEnabled(true);

  FileExportMenu.setEnabled(true);
  ExtractionOptions.setEnabled(true);
 }

 public void enableFARCMenus() {
  addFileToFARC.setEnabled(true);
  if (MAP != null)
   enableSharedMenus();
  else {
   ExportTEXOptions.setEnabled(false);
   FileExportMenu.setEnabled(false);
   ExtractionOptions.setEnabled(false);
   ReplaceSelectedOptions.setEnabled(false);
  }
 }

 public void enableMAPMenus() {
  FileExportMenu.setEnabled(true);
  addEntry.setEnabled(true);
  removeEntry.setEnabled(true);
  zeroEntry.setEnabled(true);
  exportAsRLST.setEnabled(true);
  ExportMAPOptions.setEnabled(true);
  if (FARC != null)
   enableSharedMenus();
  else {
   ExportTEXOptions.setEnabled(false);
   ExtractionOptions.setEnabled(false);
   ReplaceSelectedOptions.setEnabled(false);
  }
 }

 public void enableSharedMenus() {
  ExportTEXOptions.setEnabled(true);
  ReplaceSelectedOptions.setEnabled(true);
  printDependencies.setEnabled(true);
  installMod.setEnabled(true);
  PLANExportOptions.setEnabled(true);
  FileExportMenu.setEnabled(true);
  ExtractionOptions.setEnabled(true);
 }

 public void showUserDialog(String title, String message) {
  if ("Warning".equals(title)) {
   JOptionPane.showMessageDialog(PopUpMessage, message, title, JOptionPane.WARNING_MESSAGE);
  } else {
   JOptionPane.showMessageDialog(PopUpMessage, message, title, JOptionPane.PLAIN_MESSAGE);
  }
 }

 public static void main(String args[]) {
  java.awt.EventQueue.invokeLater(() -> {
   /* TODO: fix
   BasicLookAndFeel darcula = new DarculaLaf();
   try {
    UIManager.setLookAndFeel(darcula);
   } catch (UnsupportedLookAndFeelException ex) {
    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
   }
   */
   MainWindow myWindow = new MainWindow();
   myWindow.setVisible(true);
  });
 }
 
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Clear;
    private javax.swing.JPopupMenu ConsolePopup;
    private javax.swing.JTable EditorPanel;
    private javax.swing.JMenu ExportMAPOptions;
    private javax.swing.JMenu ExportTEXOptions;
    private javax.swing.JMenu ExtractionOptions;
    private javax.swing.JMenu FileExportMenu;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenu HelpMenu;
    private javax.swing.JScrollPane MapPanel;
    private javax.swing.JTextArea OutputTextArea;
    private javax.swing.JMenu PLANExportOptions;
    private javax.swing.JOptionPane PopUpMessage;
    private javax.swing.JLabel PreviewLabel;
    private javax.swing.JPanel PreviewPanel;
    private javax.swing.JMenu ReplaceSelectedOptions;
    private javax.swing.JSplitPane RightHandStuff;
    private javax.swing.JScrollPane TextPrevScroll;
    private javax.swing.JTextArea TextPreview;
    private javax.swing.JPanel ToolsPanel;
    private javax.swing.JPanel ToolsPanel2;
    private javax.swing.JFrame aboutWindow;
    private javax.swing.JMenuItem addEntry;
    private javax.swing.JMenuItem addFileToFARC;
    private javax.swing.JMenuItem closeApplication;
    private javax.swing.JMenuItem exportAsDDS;
    private javax.swing.JMenuItem exportAsJPG;
    private javax.swing.JMenuItem exportAsMOD;
    private javax.swing.JMenuItem exportAsPNG;
    private javax.swing.JMenuItem exportAsRLST;
    private javax.swing.JMenuItem extractDecompressed;
    private javax.swing.JMenuItem extractRaw;
    private tv.porst.jhexview.JHexView hexViewer;
    private javax.swing.JMenuItem installMod;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JProgressBar mapLoadingBar;
    public javax.swing.JTree mapTree;
    private javax.swing.JMenuItem openAboutFrame;
    private javax.swing.JMenuItem openFAR4;
    private javax.swing.JMenuItem openFARC;
    private javax.swing.JMenuItem openMAP;
    private javax.swing.JPanel pnlOutput;
    private javax.swing.JMenuItem printDependencies;
    private javax.swing.JMenuItem removeEntry;
    private javax.swing.JMenuItem replaceDecompressed;
    private javax.swing.JMenuItem replaceRaw;
    private javax.swing.JMenuItem reverseBytes;
    private javax.swing.JCheckBoxMenuItem toggleDarcula;
    private javax.swing.JMenuItem zeroEntry;
    // End of variables declaration//GEN-END:variables
}