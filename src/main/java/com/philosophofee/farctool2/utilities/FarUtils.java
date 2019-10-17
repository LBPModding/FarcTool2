package com.philosophofee.farctool2.utilities;

import com.philosophofee.farctool2.windows.MainWindow;
import org.riversun.bigdoc.bin.BigFileSearcher;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FarUtils {

    public static byte[] pullFromFarc(String currSHA1, File[] bigBoyFarc, boolean log) {
        if (bigBoyFarc == null) return null;
        if (log) System.out.println("Converting " + currSHA1 + " to byte array...");

        BigFileSearcher searcher = new BigFileSearcher();
        int fileCount = 0;
        int selectedFARC = 0;
        long tableOffset = 0;
        long fileTableOffset = 0;
        RandomAccessFile farcAccess = null;
        try {
            for (int i = 0; i < bigBoyFarc.length; i++) {
                farcAccess = new RandomAccessFile(bigBoyFarc[i], "rw");
                farcAccess.seek(bigBoyFarc[i].length() - 8);
                fileCount = farcAccess.readInt();
                tableOffset = (bigBoyFarc[i].length() - 8 - (fileCount * 28));
                fileTableOffset = searcher.indexOf(bigBoyFarc[i], MiscUtils.hexStringToByteArray(currSHA1), tableOffset);
                if (fileTableOffset == -1) farcAccess.close();
                else {
                    selectedFARC = i;
                    break;
                }
            }

        } catch (IOException ex) {
        }

        //Let's do some extraction
        long newFileSize = 0;
        long newFileOffset = 0;
        byte[] newSHA1 = new byte[20];
        try {

            //go to the file table, and grab the hash for verification later
            farcAccess.seek(fileTableOffset);
            farcAccess.readFully(newSHA1);
            if (log)
                System.out.println("Entry SHA1 (" + bigBoyFarc[selectedFARC].getName() + "): " + MiscUtils.byteArrayToHexString(newSHA1).toUpperCase());

            //seek past the sha1 and grab the offset to know where to extract the file
            farcAccess.seek(fileTableOffset + 20);
            byte bytes[] = new byte[8];
            farcAccess.read(bytes, 4, 4);
            newFileOffset = MiscUtils.getLong(bytes);
            if (log) System.out.println("Entry offset: " + newFileOffset);

            //get file size so we can know how much data to pull later
            farcAccess.seek(fileTableOffset + 24);
            newFileSize = farcAccess.readInt();
            if (log) System.out.println("Entry size: " + newFileSize);


            if (log) System.out.println("Attempting to extract now!");
            FileInputStream fin = new FileInputStream(bigBoyFarc[selectedFARC]);
            fin.skip(newFileOffset);
            byte[] outputbytes = new byte[(int) newFileSize];
            int output = 0;
            output = fin.read(outputbytes);

            fin.close();

            farcAccess.close();

            //finally, return our bytes!
            return outputbytes;

        } catch (IOException ex) {
        }
        return null; //something messed up
    }

    public static void addFile(File newFile, File[] bigBoyFarc) {
        try {
            byte[] SHA1 = MiscUtils.getSHA1(newFile);
            byte[] file = Files.readAllBytes(newFile.toPath());

            for (int i = 0; i < bigBoyFarc.length; i++) {
                RandomAccessFile farc = new RandomAccessFile(bigBoyFarc[i], "rw");

                farc.seek(farc.length() - 8);
                long fileCount = farc.readInt();
                long tableOffset = (farc.length() - 8 - (fileCount * 28));

                farc.seek(tableOffset);
                byte[] table = new byte[(int) (farc.length() - tableOffset)];
                farc.read(table);


                farc.seek(tableOffset);
                farc.write(file);

                farc.write(table);
                farc.seek(farc.length() - 8);
                farc.write(SHA1);
                farc.write(ByteBuffer.allocate(4).putInt((int) tableOffset).array());
                farc.write(ByteBuffer.allocate(4).putInt(file.length).array());
                farc.write(ByteBuffer.allocate(4).putInt((int) (fileCount + 1)).array());
                farc.write("FARC".getBytes());

                farc.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Successfully injected file into .FARC!");
    }

    public static void addFile(byte[] newFile, File[] bigBoyFarc) {
        try {
            byte[] SHA1 = MiscUtils.getSHA1(newFile);

            for (int i = 0; i < bigBoyFarc.length; i++) {
                RandomAccessFile farc = new RandomAccessFile(bigBoyFarc[i], "rw");

                farc.seek(farc.length() - 8);
                long fileCount = farc.readInt();
                long tableOffset = (farc.length() - 8 - (fileCount * 28));

                farc.seek(tableOffset);
                byte[] table = new byte[(int) (farc.length() - tableOffset)];
                farc.read(table);


                farc.seek(tableOffset);
                farc.write(newFile);

                farc.write(table);
                farc.seek(farc.length() - 8);
                farc.write(SHA1);
                farc.write(ByteBuffer.allocate(4).putInt((int) tableOffset).array());
                farc.write(ByteBuffer.allocate(4).putInt(newFile.length).array());
                farc.write(ByteBuffer.allocate(4).putInt((int) (fileCount + 1)).array());
                farc.write("FARC".getBytes());

                farc.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Successfully injected file into .FARC!");
    }

    public static byte[] pullFromFAR4(String Offset, String Size, File FAR4) {
        try {
            byte[] file;
            try (RandomAccessFile FAR4Access = new RandomAccessFile(FAR4, "rw")) {
                FAR4Access.seek(Integer.parseInt(Offset, 16));
                file = new byte[Integer.parseInt(Size, 16)];
                FAR4Access.read(file);
            }
            return file;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FarUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FarUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void rebuildFAR4(MainWindow window, String oHash, byte[] nFile) throws IOException {
        File decompressedBPRb = File.createTempFile("BPRbDecompressed", ".tmp");
        File tempBPRb = File.createTempFile("BPRb", ".tmp");
        File tempc = File.createTempFile("compressedFile", ".tmp");
        try {
            byte[] nFileHash = MiscUtils.getSHA1(nFile);
            if (MiscUtils.byteArrayToHexString(nFileHash).toUpperCase().equals(oHash)) {
                System.out.println("This file is the same, cancelling operation.");
                return;
            }

            RandomAccessFile FAR4Access = new RandomAccessFile(window.FAR4, "rw");
            try (RandomAccessFile RebuiltFAR4 = new RandomAccessFile("temp_FAR4", "rw")) {
                File tempTableFile = new File("tempTable");
                RandomAccessFile tempTable = new RandomAccessFile(tempTableFile, "rw");
                tempTable.setLength(0);

                BigFileSearcher searcher = new BigFileSearcher();

                FAR4Access.seek(FAR4Access.length() - 8);
                int Entries = FAR4Access.readInt();

                long Offset = (FAR4Access.length() - 8) - 24;
                FAR4Access.seek(Offset);
                byte[] newBPRbHash = null;
                for (int i = 0; i < Entries; i++) {
                    int tableFileSize = FAR4Access.readInt();
                    Offset -= 4;
                    FAR4Access.seek(Offset);
                    int tableFileOffset = FAR4Access.readInt();
                    Offset -= 20;
                    FAR4Access.seek(Offset);
                    String tableFileHash = "";
                    for (int j = 0; j < 20; j++) {
                        tableFileHash += String.format("%02X", FAR4Access.readByte());
                        Offset += 1;
                        FAR4Access.seek(Offset);
                    }
                    Offset -= 24;
                    if (i == 0) {
                        long BPRbHashDataOffset = tableFileOffset + tableFileSize;
                        FAR4Access.seek(BPRbHashDataOffset);
                        byte[] data = new byte[(int) ((FAR4Access.length() - 28) - (Entries * 28) - BPRbHashDataOffset)];
                        FAR4Access.read(data);
                        tempTable.write(data);
                    }
                    byte[] tableFile = new byte[tableFileSize];
                    FAR4Access.seek(tableFileOffset);
                    FAR4Access.read(tableFile);
                    FAR4Access.seek(Offset);
                    if ((tableFileHash.equals(window.BPRbSHA1) && (!oHash.equals(window.BPRbSHA1))) || tableFileHash.equals(window.IPReSHA1)) {
                        try (FileOutputStream tempBPRbOutput = new FileOutputStream(tempBPRb)) {
                            tempBPRbOutput.write(tableFile);
                            tempBPRbOutput.close();
                        }

                        if (window.IPReSHA1 == null) {
                            try (FileOutputStream fos = new FileOutputStream(decompressedBPRb)) {
                                fos.write(ZlibUtils.decompressThis(tableFile, false));
                                fos.close();
                            }
                            try (RandomAccessFile BPRbAccess = new RandomAccessFile(decompressedBPRb, "rw")) {
                                Boolean lastEntry = false;
                                while (!lastEntry) {
                                    long DecOffset = searcher.indexOf(decompressedBPRb, MiscUtils.hexStringToByteArray(oHash));
                                    if (DecOffset == -1) lastEntry = true;
                                    else {
                                        BPRbAccess.seek(DecOffset);
                                        BPRbAccess.write(nFileHash);
                                    }
                                }
                                BPRbAccess.close();
                                try (FileOutputStream fos = new FileOutputStream(tempc)) {
                                    fos.write(ZlibUtils.compressFile(tempBPRb, decompressedBPRb, false));
                                    fos.close();
                                }
                                decompressedBPRb.delete();
                            }
                        }
                        long newOffset = RebuiltFAR4.getFilePointer();
                        File BPRbCompressed;
                        if (window.IPReSHA1 == null) BPRbCompressed = tempc;
                        else BPRbCompressed = tempBPRb;

                        try (RandomAccessFile BPRbCompAccess = new RandomAccessFile(BPRbCompressed, "rw")) {
                            long oldHashOffset = searcher.indexOf(BPRbCompressed, MiscUtils.hexStringToByteArray(oHash));
                            if (oldHashOffset != -1) {
                                BPRbCompAccess.seek(oldHashOffset);
                                BPRbCompAccess.write(nFileHash);
                                BPRbCompAccess.close();
                            }
                        }

                        RebuiltFAR4.write(Files.readAllBytes(BPRbCompressed.toPath()));

                        newBPRbHash = MiscUtils.getSHA1(BPRbCompressed);
                        tempTable.write(newBPRbHash);
                        tempTable.writeInt((int) newOffset);
                        tempTable.writeInt((int) BPRbCompressed.length());
                    } else if (tableFileHash.equals(oHash)) {
                        long newOffset = RebuiltFAR4.getFilePointer();
                        RebuiltFAR4.write(nFile);
                        tempTable.write(nFileHash);
                        tempTable.writeInt((int) newOffset);
                        tempTable.writeInt((int) nFile.length);
                    } else {
                        long newOffset = RebuiltFAR4.getFilePointer();
                        RebuiltFAR4.write(tableFile);
                        tempTable.write(MiscUtils.getSHA1(tableFile));
                        tempTable.writeInt((int) newOffset);
                        tempTable.writeInt((int) tableFile.length);
                    }
                }
                tempTable.write(MiscUtils.hexStringToByteArray("0000000000000000000000000000000000000000"));
                tempTable.close();
                RebuiltFAR4.write(Files.readAllBytes(tempTableFile.toPath()));
                RebuiltFAR4.writeInt(Entries);
                RebuiltFAR4.write(MiscUtils.hexStringToByteArray("46415234"));
                RebuiltFAR4.seek((RebuiltFAR4.length() - 28) - (Entries * 28) - 60);
                if (oHash.equals(window.BPRbSHA1)) RebuiltFAR4.write(nFileHash);
                else if (newBPRbHash != null) RebuiltFAR4.write(newBPRbHash);
                tempTableFile.delete();
            }

            FAR4Access.setLength(0);
            FAR4Access.write(Files.readAllBytes(Paths.get("temp_FAR4")));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FarUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(FarUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        new File("temp_FAR4").delete();
        tempBPRb.delete();
        decompressedBPRb.delete();
        tempc.delete();

    }

    public static void openFAR4(MainWindow window) {
        try (RandomAccessFile FAR4Access = new RandomAccessFile(window.FAR4, "rw")) {
            window.BPRbSHA1 = null;
            window.PRFbSHA1 = null;
            FAR4Access.seek(FAR4Access.length() - 8);
            int Entries = FAR4Access.readInt();
            long TableOffset = (FAR4Access.length() - 28) - (Entries * 28);
            FAR4Access.seek(TableOffset);

            long PreviousOffset = TableOffset;
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(window.FAR4.getName());
            DefaultTreeModel model = new DefaultTreeModel((DefaultMutableTreeNode) root);

            window.FAR4Size = new String[Entries];
            window.FAR4SHA1 = new String[Entries];

            for (int i = 0; i < Entries; i++) {
                String fileHash = "";
                for (int j = 0; j < 20; j++) {
                    fileHash += String.format("%02X", FAR4Access.readByte());
                    PreviousOffset += 1;
                    FAR4Access.seek(PreviousOffset);
                }

                window.FAR4SHA1[i] = fileHash;


                int Offset;

                Offset = FAR4Access.readInt();
                PreviousOffset += 4;
                FAR4Access.seek(Offset);

                byte[] Magic = new byte[3];
                FAR4Access.read(Magic);
                String Extension = new String(Magic, "UTF-8").toLowerCase();
                if (Extension.contains("pln")) Extension = "plan";
                else if (MiscUtils.byteArrayToHexString(Magic).toLowerCase().startsWith("ffd8"))
                    Extension = "jpg";
                else if (MiscUtils.byteArrayToHexString(Magic).toLowerCase().startsWith("89504e"))
                    Extension = "png";

                FAR4Access.seek(PreviousOffset);

                String fileSize = "";
                for (int j = 0; j < 4; j++) {
                    fileSize += String.format("%02X", FAR4Access.readByte());
                    PreviousOffset += 1;
                    FAR4Access.seek(PreviousOffset);
                }

                if (Extension.contains("bpr")) window.BPRbSHA1 = fileHash;
                if (Extension.contains("prf")) window.PRFbSHA1 = fileHash;
                if (Extension.contains("ipr")) window.IPReSHA1 = fileHash;

                window.FAR4Size[i] = fileSize;

                MiscUtils.buildTreeFromString(model, Integer.toHexString(Offset) + "." + Extension);
            }

            window.mapTree.setModel(model);
            window.mapTree.updateUI();

            window.MAP = null;
            window.FARC = null;
            window.disableFARCMenus();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}