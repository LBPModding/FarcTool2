/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.philosophofee.farctool2.utilities;

import com.philosophofee.farctool2.algorithms.KMPMatch;
import com.philosophofee.farctool2.windows.MainWindow;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import net.npe.dds.DDSReader;

/**
 *
 * @author hasben
 */
public class MiscUtils {

 public static void deleteFile(String file) throws IOException {
  Files.deleteIfExists(Paths.get(file));
 }

 public static File returnFile(String file) {
  File to = new File(file);
  return to;
 }

 public static String getFileNameFromGUID(String myGUID, File inputMap) {
  long offset = findGUIDOffset(myGUID, inputMap);
  try {
   //Create data input stream
   RandomAccessFile mapAccess = new RandomAccessFile(inputMap, "rw");
   mapAccess.seek(offset);
   
   Boolean lbp3map = false;
   Boolean beginning = false;
   offset -= 33;
   while(!beginning)
   {
        offset -= 1;
        mapAccess.seek(offset);
                
        byte[] buffer = new byte[1];
        mapAccess.read(buffer);
                
        if (MiscUtils.byteArrayToHexString(buffer).equals("00"))
            beginning = true;
    }
    offset++;  
       
        int length = (int) mapAccess.readByte();
        byte[] fileName = new byte[length];
        mapAccess.read(fileName);
        return new String(fileName, "UTF-8");
        
   
   
  } catch (FileNotFoundException ex) {} catch (IOException ex) {}
  return "Error finding GUID filename";
 }

 public static byte[] hexStringToByteArray(String s) {
  int len = s.length();
  byte[] data = new byte[len / 2];
  for (int i = 0; i < len; i += 2) {
   data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) +
    Character.digit(s.charAt(i + 1), 16));
  }
  return data;
 }

 public static String byteArrayToHexString(byte[] bytes) {
  Formatter formatter = new Formatter();
  for (byte b: bytes) {
   formatter.format("%02x", b);
  }
  return formatter.toString();
 }

 public static String getHeader(File input) {
  String header = "";
  try {
   RandomAccessFile fileAccess = new RandomAccessFile(input, "rw");
   for (int i2 = 0; i2 < 4; i2++) {
    header += (char) fileAccess.readByte();
   }
   return header;
  } catch (IOException e) {}
  return header;
 }

 public static String getHeaderHexString(File input) {
  String header = "";
  try {
   RandomAccessFile fileAccess = new RandomAccessFile(input, "rw");
   for (int i2 = 0; i2 < 4; i2++) {
    header += String.format("%02X", fileAccess.readByte());
   }
   return header;
  } catch (IOException e) {}
  return header;
 }

 public static String convertShortHexStringToLittleEndian(String input) {
  //what a mouthful
  char[] ch = input.toCharArray();
  String output = new String();
  output += ch[2];
  output += ch[3];
  output += ch[0];
  output += ch[1];
  return output;
 }

 public static ImageIcon createDDSIcon(String path) throws IOException {

  FileInputStream fis = new FileInputStream(path);
  byte[] buffer = new byte[fis.available()];
  fis.read(buffer);
  fis.close();

  int[] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
  int width = DDSReader.getWidth(buffer);
  int height = DDSReader.getHeight(buffer);
  BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  image.setRGB(0, 0, width, height, pixels, 0, width);
  return getScaledImage(image);
 }

 public static ImageIcon createImageIconFromDDS(byte[] buffer) {
  int[] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
  int width = DDSReader.getWidth(buffer);
  int height = DDSReader.getHeight(buffer);
  BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  image.setRGB(0, 0, width, height, pixels, 0, width);
  return getScaledImage(image);
 }
 
  public static BufferedImage DDStoPNG(byte[] buffer) {
  int[] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
  int width = DDSReader.getWidth(buffer);
  int height = DDSReader.getHeight(buffer);
  BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  image.setRGB(0, 0, width, height, pixels, 0, width);
  return image;
 }

 public static void DDStoStandard(byte[] buffer, String extension, File output) throws IOException, NullPointerException {

  int[] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
  int width = DDSReader.getWidth(buffer);
  int height = DDSReader.getHeight(buffer);

  BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  image.setRGB(0, 0, width, height, pixels, 0, width);

  if (extension.equals("jpg")) {
   BufferedImage JPG = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
   JPG.createGraphics().drawImage(image, 0, 0, Color.BLACK, null);
   image = JPG;
  }

  ImageIO.write(image, extension, output);

 }

 public static String reverseHex(String originalHex) {
  // TODO: Validation that the length is even
  int lengthInBytes = originalHex.length() / 2;
  char[] chars = new char[lengthInBytes * 2];
  for (int index = 0; index < lengthInBytes; index++) {
   int reversedIndex = lengthInBytes - 1 - index;
   chars[reversedIndex * 2] = originalHex.charAt(index * 2);
   chars[reversedIndex * 2 + 1] = originalHex.charAt(index * 2 + 1);
  }
  return new String(chars);
 }

 public static ImageIcon getScaledImage(BufferedImage source) {
  int width = source.getWidth();
  int height = source.getHeight();

  if (width > 256 || height > 256) {
   if (width > height) {
    return new ImageIcon(source.getScaledInstance(256, 128, BufferedImage.SCALE_SMOOTH));
   }
   if (width < height) {
    return new ImageIcon(source.getScaledInstance(128, 256, BufferedImage.SCALE_SMOOTH));
   }
   return new ImageIcon(source.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH));
  }
  return new ImageIcon(source.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
 }
 
  public static ImageIcon getScaledIcon(BufferedImage source) {
  int width = source.getWidth();
  int height = source.getHeight();

  if (width > 116 || height > 116) {
   if (width > height) {
    return new ImageIcon(source.getScaledInstance(116, 116, BufferedImage.SCALE_SMOOTH));
   }
   if (width < height) {
    return new ImageIcon(source.getScaledInstance(58, 116, BufferedImage.SCALE_SMOOTH));
   }
   return new ImageIcon(source.getScaledInstance(116, 116, BufferedImage.SCALE_SMOOTH));
  }
  return new ImageIcon(source.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH));
 }

public static long getLong(byte[] bytes) {
    long value = ((bytes[0] & 0xFFL) << 56) |
         ((bytes[1] & 0xFFL) << 48) |
         ((bytes[2] & 0xFFL) << 40) |
         ((bytes[3] & 0xFFL) << 32) |
         ((bytes[4] & 0xFFL) << 24) |
         ((bytes[5] & 0xFFL) << 16) |
         ((bytes[6] & 0xFFL) <<  8) |
         ((bytes[7] & 0xFFL) <<  0) ;
    return value;
}

 public static String leftPad(String text, int size) {
  StringBuilder builder = new StringBuilder(text);
  while (builder.length() < size) {
   builder.insert(0, '0');
  }
  return builder.toString();
 }

 public static byte[] getSHA1(File file) throws Exception {
  MessageDigest digest = MessageDigest.getInstance("SHA-1");
  InputStream fis = new FileInputStream(file);
  int read = 0;
  byte[] buffer = new byte[8192];
  while (read != -1) {
   read = fis.read(buffer);
   if (read > 0) {
    digest.update(buffer, 0, read);
   }
  }
  fis.close();
  return digest.digest();
 }
 
  public static byte[] getSHA1(byte[] file) throws Exception {
  MessageDigest digest = MessageDigest.getInstance("SHA-1");
  return digest.digest(file);
 }
 
 public static void addEntry(String Path, String Hash, String Size, String GUID, MainWindow Window)
 {
     Boolean lbp3map = false;   
     try (RandomAccessFile mapAccess = new RandomAccessFile(Window.MAP, "rw"))
        {
            mapAccess.seek(0);
            if (mapAccess.readInt() == 21496064) lbp3map = true;
            mapAccess.seek(mapAccess.length());
            
            if (lbp3map) mapAccess.write(new byte[1]);
            else mapAccess.write(new byte[3]);
            
            mapAccess.write((byte)(Path.length()));
            mapAccess.write(Path.getBytes());
            if (!lbp3map)
                mapAccess.write(new byte[4]);
            mapAccess.write(ByteBuffer.allocate(4).putInt((int) (new Date().getTime() / 1000)).array());
            mapAccess.write(MiscUtils.hexStringToByteArray(MiscUtils.leftPad(Size, 8)));
            mapAccess.write(MiscUtils.hexStringToByteArray(MiscUtils.leftPad(Hash, 40)));
            mapAccess.write(MiscUtils.hexStringToByteArray(MiscUtils.leftPad(GUID, 8)));
            
            mapAccess.seek(4);
            int entries = mapAccess.readInt();
            entries += 1;
            
            mapAccess.seek(4);
            mapAccess.writeInt(entries);
            
            mapAccess.close();
            
            MapUtils parser = new MapUtils();
            buildTreeFromString((DefaultTreeModel) Window.mapTree.getModel(), Path);
            ((DefaultTreeModel) Window.mapTree.getModel()).reload((DefaultMutableTreeNode)Window.mapTree.getModel().getRoot());
            Window.mapTree.updateUI();
            
            System.out.println("Successfully added entry to .MAP!");
 }   catch (IOException ex) {
         Logger.getLogger(MiscUtils.class.getName()).log(Level.SEVERE, null, ex);
     }
 }
 
 public static long findGUIDOffset(String GUID, File map)
 {
   try {
        RandomAccessFile myMap = new RandomAccessFile(map, "rw");
        myMap.seek(myMap.length() - 4);
        int GUIDQuestion = myMap.readInt();
        myMap.close();
        if (MiscUtils.leftPad(Integer.toHexString(GUIDQuestion), 8).equals(GUID))
            return (long) (map.length() - 4);
        KMPMatch matcher = new KMPMatch();
        long offset = matcher.indexOf(Files.readAllBytes(map.toPath()), MiscUtils.hexStringToByteArray(MiscUtils.leftPad(GUID + "00", 10)));
        return offset;
    } catch (IOException ex) {
        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
    }
   return 0;
 }
 
 public static String getHashFromGUID(String GUID, File map)
 {
     try {
         long offset = findGUIDOffset(GUID, map);
         if (offset == -1) return "NULL";
         RandomAccessFile mapAccess = new RandomAccessFile(map, "rw");
         offset -= 20;
         mapAccess.seek(offset);
         byte[] buffer = new byte[20];
         mapAccess.read(buffer);
         mapAccess.close();
         return byteArrayToHexString(buffer);
     } catch (FileNotFoundException ex) {
         Logger.getLogger(MiscUtils.class.getName()).log(Level.SEVERE, null, ex);
     } catch (IOException ex) {
         Logger.getLogger(MiscUtils.class.getName()).log(Level.SEVERE, null, ex);
     }
     return null;
 }
 
 public static void replaceEntryByGUID(String GUID, String Filename, String Size, String Hash, MainWindow Window)
 {
        Boolean lbp3map = false;
        long offset = MiscUtils.findGUIDOffset(GUID, Window.MAP);
        try {
        if (offset == -1) return;
            
        
            RandomAccessFile map = new RandomAccessFile(Window.MAP, "rw");
            Boolean beginning = false;
            
            map.seek(0);
            if (map.readInt() == 21496064) lbp3map = true;
        
            if (lbp3map) offset -= 28;
            else offset -= 32;
            
            while(!beginning)
            {
                offset -= 1;
                map.seek(offset);
                
                byte[] buffer = new byte[1];
                map.read(buffer);
                
                if (MiscUtils.byteArrayToHexString(buffer).equals("00"))
                    beginning = true;
            }
            offset++;  
           
            byte[] prev = new byte[(int) offset];
            map.seek(0);
            map.read(prev);
            
            map.seek(offset);

            int length = map.readByte();
            offset++;
            
            byte[] fileName = new byte[length];
            map.read(fileName);
           
            
            String oldName = new String(fileName, "UTF-8");
            
            offset += length;
            
            if (lbp3map) offset += 32;
            else offset += 36;
            
            map.seek(offset);
            
            byte[] buffer = null;
            Boolean lastFile = false;
            if (map.length() != (int) (offset))
            {
                buffer = new byte[(int)(map.length() - offset)];
                map.read(buffer);
            } else 
            {
                buffer = new byte[0];
                lastFile = true;
            }
            
            map.setLength(0);
            map.write(prev);
            
            map.write((byte)Filename.length());
            map.write(Filename.getBytes());
            if (!lbp3map) map.write(new byte[4]);
            map.write(ByteBuffer.allocate(4).putInt((int) (new Date().getTime() / 1000)).array());
            map.write(MiscUtils.hexStringToByteArray(MiscUtils.leftPad(Size, 8)));
            map.write(MiscUtils.hexStringToByteArray(MiscUtils.leftPad(Hash, 40)));
            map.write(MiscUtils.hexStringToByteArray(MiscUtils.leftPad(GUID, 8)));
            map.write(buffer);
            
            String[] paths = oldName.split("/");
            deleteNodeFromPath((DefaultMutableTreeNode) (Window.mapTree.getModel().getRoot()), paths, 0);
            
            MapUtils parser = new MapUtils();
            buildTreeFromString((DefaultTreeModel) Window.mapTree.getModel(), Filename);
            map.close();
 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
 }
 
  public static void deleteNodeFromPath(DefaultMutableTreeNode node, String[] path, int index)
 {
     for (int i = 0; i < node.getChildCount(); i++)
     {
          if (((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject().toString().equals(path[index]))
          {
             if (index == (path.length - 1))
             {
                 ((DefaultMutableTreeNode) node.getChildAt(i)).removeFromParent();
                 return;
             }
             deleteNodeFromPath((DefaultMutableTreeNode) node.getChildAt(i), path, (index + 1));
             return;
         }
     }
 }
  
 public static void reverseBytes(File file) throws IOException
    {
        int seek = 0;
        byte[] buffer = null;
        try (RandomAccessFile fileAccess = new RandomAccessFile(file, "rw")) {
            for (int i = 0; fileAccess.length() > (i * 4); i++)
            {
                buffer = new byte[4];
                fileAccess.read(buffer);

                seek += 4;
                
                fileAccess.seek(seek - 4);
                fileAccess.write(reverseArray(buffer, 0, 4));
            }
            fileAccess.close();
        } catch (IOException e) {}
        System.out.println("Finished!");
    }
 
 public static byte[] reverseArray(byte arr[], int start, int end)
{
    int len = end - start;
    if(len <= 0) return null;

    int len2 = len >> 1;
    int temp;

    for (int i = 0; i < len2; ++i)
    {
        temp = arr[start + i];
        arr[start + i] = arr[end - i - 1];
        arr[end - i - 1] = (byte) temp;
    }
    return arr;
}
 
 public static void buildTreeFromString(final DefaultTreeModel model, final String str) {
  DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

  String[] strings = str.split("/");

  DefaultMutableTreeNode node = root;

  for (String s: strings) {
   int index = childIndex(node, s);

   if (index < 0) {
    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(s);
    node.insert(newChild, node.getChildCount());
    node = newChild;
   } else {
    node = (DefaultMutableTreeNode) node.getChildAt(index);
   }
  }
 }

 public static int childIndex(final DefaultMutableTreeNode node, final String childValue) {
  Enumeration < TreeNode > children = node.children();
  DefaultMutableTreeNode child = null;
  int index = -1;

  while (children.hasMoreElements() && index < 0) {
   child = (DefaultMutableTreeNode) children.nextElement();

   if (child.getUserObject() != null && childValue.equals(child.getUserObject())) {
    index = node.getIndex(child);
   }
  }

  return index;
 }
 
 public static String getEGUID(String fileGUID)
 {
      int secondByte = Integer.parseInt(fileGUID.substring(2, 4), 16);
       int thirdByte = Integer.parseInt(fileGUID.substring(4, 6), 16);
       int fourthByte = Integer.parseInt(fileGUID.substring(6, 8), 16);
       String obfGUID = "";
       
       obfGUID += MiscUtils.leftPad(Integer.toHexString(((fourthByte > 127) ? fourthByte : (fourthByte + 128))), 2);
       
       if (thirdByte == 0) obfGUID += "80";
       else if (thirdByte < 64) obfGUID += MiscUtils.leftPad(Integer.toHexString(((thirdByte * 2) + 128)), 2);
       else if (thirdByte > 63 && thirdByte < 127) obfGUID += MiscUtils.leftPad(Integer.toHexString(thirdByte * 2), 2);
       else if (thirdByte > 127 && thirdByte < 192) obfGUID += MiscUtils.leftPad(Integer.toHexString(((thirdByte * 2) - 128)), 2);
       else if (thirdByte > 191 && thirdByte < 256) obfGUID += MiscUtils.leftPad(Integer.toHexString(((thirdByte * 2) - 255)), 2);
       
       int nextBit = Integer.parseInt(Character.toString(fileGUID.charAt(6)), 16);
       int fourthBit = Integer.parseInt(Character.toString(fileGUID.charAt(4)), 16);
       
       StringBuilder builder = new StringBuilder(obfGUID);
       if (fourthBit == 15) builder.setCharAt(3, (char) (builder.charAt(3) - 1));
       else if (nextBit == 15) builder.setCharAt(3, (char) (builder.charAt(3) - 1));
       if (fourthBit > 7 && fourthBit < 15) builder.setCharAt(3, (char) (builder.charAt(3) + 1));
       else if (nextBit > 7 && nextBit < 15) builder.setCharAt(3, (char) (builder.charAt(3) + 1));   
       obfGUID = builder.toString();
       
       StringBuilder thirdByteBuilder = new StringBuilder(obfGUID.toUpperCase());
       obfGUID = thirdByteBuilder.toString();
       switch(secondByte) {
           case 0: obfGUID += "01"; break;
           case 1: obfGUID += "07"; break;
           case 2: obfGUID += "08"; break;
           case 3: obfGUID += "0C"; break;
           case 4: obfGUID += "13"; break;
           case 5: obfGUID += "14"; break;
           case 6: obfGUID += "19"; break;
           case 7: obfGUID += "1E"; break;
           case 8: obfGUID += "23"; break;
           case 9: obfGUID += "24"; break;
           case 10: obfGUID += "28"; break;
           case 11: obfGUID += "2E"; break;
           case 12: obfGUID += "30"; break;
           case 13: obfGUID += "37"; break;
           case 14: obfGUID += "3A"; break;
           case 15: obfGUID += "3E"; break;
           case 16: obfGUID += "43"; break;
           case 17: obfGUID += "44"; break;
       }
       obfGUID += "00";
       return obfGUID;
 }
 
 public static List<byte[]> divideArray(byte[] source, int chunkSize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunkSize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunkSize;
        }
        return result;
}
}