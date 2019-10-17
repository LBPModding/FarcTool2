package com.philosophofee.farctool2.utilities;

import com.philosophofee.farctool2.utilities.MiscUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class MapUtils {

 public DefaultTreeModel parseMapIntoMemory(TreeNode root, File file) {
  DefaultTreeModel model = new DefaultTreeModel(root);
  try {
   try ( 
    // Open the selected file. // 
    DataInputStream mapAccess = new DataInputStream(new FileInputStream(file))) {
    
    // Initialize Variables // 
    long begin = System.currentTimeMillis();
    boolean lbp3map = isLBP3Map(mapAccess.readInt(), true);
    
    // Get the amount of entries present in the .MAP File //
    int mapEntries = mapAccess.readInt();
    System.out.println("There are " + mapEntries + " entries present in the .MAP file");
    
    // Enumerate each entry detected. //
    int fileNameLength = 0;
    String fileName = "";
    for (int i = 0; i < mapEntries; i++) 
    {

        // Seek 2 bytes if the .MAP originates from LBP1/2. //
        if (!lbp3map) mapAccess.skip(2);
        
        // Get path of entry //
        byte[] fileNameBytes = new byte[mapAccess.readShort()];
        mapAccess.read(fileNameBytes);
        fileName = new String(fileNameBytes);

        // Seek 4 bytes if the .MAP originates from LBP1/2. (Padding) //
        if (!lbp3map) mapAccess.skip(4);
        
        // Skip the rest of the data as it's obtained at a future point in time. //
        mapAccess.skip(32);
        
        if (isHidden(fileName)) continue;

        // Build the node for the JTree. //
        MiscUtils.buildTreeFromString(model, fileName);

    }
    
    // Print the amount of time it took to process. //
    long end = System.currentTimeMillis();
    long timeTook = end - begin;
    System.out.println(".MAP parsed in " + (timeTook / 1000) + " seconds (" + timeTook + "ms)");
   }

  } catch (FileNotFoundException ex) {} catch (IOException ex) {}
  
  return model;
 }
 
 public static boolean isLBP3Map(int header, boolean log) throws IOException {
    switch (header)
    {
        case 256:
            if (log) System.out.println("Detected: LBP 1/2 .MAP File");
            return false;
        case 21496064:
               if (log) System.out.println("Detected: LBP3 .MAP File");
               return true;
        case 936:
            if (log) System.out.println("Detected: LBP Vita .MAP File");
            return false;
        default:
            throw new IOException("Error reading 4 bytes - not a valid .map file");
    }
 }
 
 public static boolean isHidden(String entry) {
     return (entry.contains(".fsb") || entry.contains(".farc") || entry.contains(".sdat") || 
             entry.contains(".edat") || entry.contains(".bik") || 
             entry.contains(".fnt") || entry.contains(".fev") || 
             entry.equals(""));
 }
 
}