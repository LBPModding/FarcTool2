package com.philosophofee.farctool2.utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
/*

Custom Compression Full Format // Compresses files in 32 KiB slices, includes dependencies
	4 byte magic_number // Magic Number to identify file type
	4 byte game_revision // Revision of the game, when this file was added/last modified
	4 byte offset_dependency_list // Offset from start of the file to dependency list
	2 byte load_dynamic_flag // Indicates dynamic data that changes at runtime? 0x4C44 "LD" = true, 0x0000 = false (game_revision == 626)
	2 byte unknown_1 // Unknown, known values: 0x0004 (4) - 0x0017 (23) (game_revision == 626)
	1 byte unknown_2 // Unknown, known value: 0x07 (game_revision == 626 && load_dynamic_flag == "LD")
	1 byte unknown_3 // Unknown, known value: 0x01 (0x00000190 (394) <= game_revision <= 0x0000026E (622) or game_revision == 626)
	2 byte encrypted_body // 0x01 = false, 0x02 = true, encryption covers everything from here up to footer (dependency_entry_count)
	2 byte stream_entry_count // Number of zlib streams (= ceil(file_size_uncompressed / 2^15))
	stream_entry_count times: // Per stream a custom 4 byte header with size infos
		2 byte size_compressed // Size of the zlib stream (including zlib header, and checksum)
		2 byte size_uncompressed // Size of file slice (for non final stream: 2^15)
	stream_entry_count times: // Actual zlib streams containing the file slices (concatenate uncompressed for real file)
		size_compressed byte zlib_stream // Single complete zlib stream
	4 byte dependency_entry_count // Number of File Depenencies
	dependency_entry_count times: // List of dependencies
		1 byte dependency_type // The type of this dependency
		dependency_type 0x01: // A dependency defined by SHA1 Hash of file
			20 byte hash // SHA1 Hash of this dependency
		dependency_type 0x02: // A dependency defined by GUID of file
			4 byte guid // GUID of this dependency
		4 byte spu_affinity // affinity of asset towards specific SPUs (0b00000000 = PPU)? file type?

Custom Compression Texture Format // Compresses files in 32 KiB slices, .tex Magic Number
	4 byte magic_number // Magic Number to identify file type
	2 byte encrypted_body // 0x01 = false, 0x02 = true, encryption covers everything from here onwards
	2 byte stream_entry_count // Number of zlib streams (= ceil(file_size_uncompressed / 2^15))
	stream_entry_count times: // Per stream a custom 4 byte header with size infos
		2 byte size_compressed // Size of the zlib stream (including zlib header, and checksum)
		2 byte size_uncompressed // Size of file slice (for non final stream: 2^15)
	stream_entry_count times: // Actual zlib streams containing the file slices (concatenate uncompressed for real file)
		size_compressed byte zlib_stream // Single complete zlib stream


*/
public class ZlibUtils {

 public static byte[] decompressFull(File input, boolean log) {
  int seek = 0;
  int streamCount = 0;
  try {
   byte[] finale;
   try (RandomAccessFile fileAccess = new RandomAccessFile(input, "rw")) {

    seek += 12;
    fileAccess.seek(seek);

    short type = fileAccess.readShort();
    seek += 2;

    if (type == 256) seek += 1;
    else seek += 6;

    fileAccess.seek(seek);

    streamCount = fileAccess.readShort();
    if (log) System.out.println("Processing " + streamCount + " Zlib streams...");
    int[] unc = new int[streamCount];
    int[] com = new int[streamCount];
    int fullSize = 0;
    for (int i = 0; i < streamCount; i++) {
     seek += 2;
     fileAccess.seek(seek);
     com[i] = Math.abs(fileAccess.readShort());
     seek += 2;
     fileAccess.seek(seek);
     unc[i] = Math.abs(fileAccess.readShort());
     fullSize += unc[i];
    }
    seek += 2;
    fileAccess.seek(seek);
    finale = new byte[fullSize];
    try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
    {
       for (int i = 0; i < streamCount; i++) {
       byte[] temp = new byte[com[i]];
       fileAccess.readFully(temp);
       Inflater decompresser = new Inflater();
       decompresser.setInput(temp);
       byte[] result = new byte[unc[i]];
       decompresser.inflate(result);
       decompresser.end();

       outputStream.write(result);
      }
      finale = outputStream.toByteArray();   
      fileAccess.close();
    }
   }
   if (log) System.out.println("Operation complete!");
   return finale;

  } catch (IOException | DataFormatException e) {}
  return null;

 }
 /**
  * Returns an array of bytes that have been decompressed from an LBP 32-KiB 
  * zlib file.
  * 
  * @param input
  * @return
  * @throws java.util.zip.DataFormatException
  */
 public static byte[] decompressThis(byte[] input, boolean log) throws DataFormatException {

  //create temp file from input
  try (FileOutputStream fos = new FileOutputStream("temp")) { fos.write(input); fos.close(); } catch (IOException ex) {}

  File workingFile = new File("temp");
  switch (MiscUtils.getHeaderHexString(workingFile)) {
   case "54455820":
    if (log) System.out.println("Format: TEX File");
    return decompressTex(workingFile, log);
   case "47544620":
    if (log) System.out.println("Format: GTF File");
    return decompressGTF(workingFile, log);
   case "47544673":
    if (log) System.out.println("Format: PS Vita GTF Swizzled (GXT) (Simple Header) - Not yet implemented");
    break;
   case "47544653":
    if (log) System.out.println("Format: PS Vita GTF Swizzled (GXT) (Extended Header) - Not yet implemented");
    break;
   case "504C4E62":
   case "414E4D62":
   case "42455662":
   case "4C564C62":
   case "434C4462":
   case "46534862":
   case "474D5462":
   case "4F415462":
   case "4D534862":
   case "534C5462":
   case "50414C62":
   case "50434B62":
   case "42505262":
    if (log) System.out.println("Format: Custom Compression Full");
    return decompressFull(workingFile, log);
   default:
    if (log) System.out.println("Not implemented");
    return input;
  }
  return null;
 }

 public static byte[] decompressTex(File input, boolean log) {
  int seek = 0;
  int streamCnt = 0;
  try {
   byte[] finale;
   //Skip header
   try ( //Set up access file
    RandomAccessFile fileAccess = new RandomAccessFile(input, "rw")) {
    //Skip header
    seek += 4;
    fileAccess.seek(seek);
    //Skip encryption (maybe will add later but undocumented)
    seek += 2;
    fileAccess.seek(seek);
    //Read stream count
    streamCnt = fileAccess.readShort();
    if (log) System.out.println("Processing " + streamCnt + " Zlib streams...");
    //Read streams
    int[] unc = new int[streamCnt];
    int[] com = new int[streamCnt];
    int fullSize = 0;
    for (int i = 0; i < streamCnt; i++) {
     seek += 2;
     fileAccess.seek(seek);
     com[i] = Math.abs(fileAccess.readShort());
     seek += 2;
     fileAccess.seek(seek);
     unc[i] = Math.abs(fileAccess.readShort());
     fullSize += unc[i];
    } //Now we start piecing together the decompressed file
    seek += 2;
    fileAccess.seek(seek);
    finale = new byte[fullSize];
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //Decompress every stream and add it to the byte array
    for (int i = 0; i < streamCnt; i++) {
     byte[] temp = new byte[com[i]];
     fileAccess.readFully(temp);
     Inflater decompresser = new Inflater();
     decompresser.setInput(temp);
     byte[] result = new byte[unc[i]];
     decompresser.inflate(result);
     decompresser.end();

     outputStream.write(result);
    }
    finale = outputStream.toByteArray();
    //Done!
   }
   try (FileOutputStream fos = new FileOutputStream("temp_prev_tex")) {
    fos.write(finale);
   }
   if (log) System.out.println("Operation complete!");
   return finale;

  } catch (IOException | DataFormatException e) {}
  return null;
 }
 public static byte[] decompressGTF(File input, boolean log) throws DataFormatException {
  int seek = 0;
  int streamCnt = 0;
  String DXTMode = "";
  short width = 0;
  short height = 0;
  String widthHex = "";
  String heightHex = "";
  try {
   //Set up access file
   RandomAccessFile fileAccess = new RandomAccessFile(input, "rw");
   seek += 4;
   fileAccess.seek(seek);
   //Get dxt mode
   switch (fileAccess.readByte()) {
    case -122:
     if (log) System.out.println("DXT Mode: DXT1");
     DXTMode = "DXT1";
     break;
    case -120:
     if (log) System.out.println("DXT Mode: DXT5");
     DXTMode = "DXT5";
     break;
    case -123:
     if (log) System.out.println("DXT Mode: 8.8.8.8 RGBA 32 Bit");
     DXTMode = "8.8.8.8";
     break;
    default:
     if (log) System.out.println("Unknown DXT Mode!");
     break;
   }
   seek += 3;
   fileAccess.seek(seek);
   
   seek += 4;
   fileAccess.seek(seek);

   //2 bytes width
   fileAccess.seek(12);
   width = fileAccess.readShort();
   if (log) System.out.println("Width: " + width + "px");
   fileAccess.seek(12); //do it again
   for (int i2 = 0; i2 < 2; i2++) widthHex += String.format("%02X", fileAccess.readByte());

   //2 bytes height
   fileAccess.seek(14);
   height = fileAccess.readShort();
   if (log) System.out.println("Height: " + height + "px");
   fileAccess.seek(14); //do it again
   for (int i2 = 0; i2 < 2; i2++) heightHex += String.format("%02X", fileAccess.readByte());

   try (FileOutputStream fos = new FileOutputStream("temp_prev_tex")) {
    fos.write(MiscUtils.hexStringToByteArray("444453207C00000007100A00"));
    fos.write(MiscUtils.hexStringToByteArray(MiscUtils.convertShortHexStringToLittleEndian(heightHex)));
    fos.write(MiscUtils.hexStringToByteArray("0000"));
    fos.write(MiscUtils.hexStringToByteArray(MiscUtils.convertShortHexStringToLittleEndian(widthHex)));
    fos.write(MiscUtils.hexStringToByteArray("0000"));
    if (!"8.8.8.8".equals(DXTMode)) {
     fos.write(MiscUtils.hexStringToByteArray("00400000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002000000004000000"));
     if ("DXT1".equals(DXTMode)) fos.write(MiscUtils.hexStringToByteArray("44585431"));
     else if ("DXT5".equals(DXTMode))
      fos.write(MiscUtils.hexStringToByteArray("44585435"));
    fos.write(MiscUtils.hexStringToByteArray("00000000000000000000000000000000000000000810400000000000000000000000000000000000"));
    } else fos.write(MiscUtils.hexStringToByteArray("0040000000000000070000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000200000004100000000000000200000000000FF0000FF0000FF000000000000FF0810400000000000000000000000000000000000"));
    

    //now actually get the texture data...
    //Read stream count
    seek = 30;
    fileAccess.seek(seek);
    streamCnt = fileAccess.readShort();
    if (log) System.out.println("Processing " + streamCnt + " Zlib streams...");
    //Read streams
    int[] unc = new int[streamCnt];
    int[] com = new int[streamCnt];
    int fullSize = 0;
    for (int i = 0; i < streamCnt; i++) {
     seek += 2;
     fileAccess.seek(seek);
     com[i] = Math.abs(fileAccess.readShort());
     seek += 2;
     fileAccess.seek(seek);
     unc[i] = Math.abs(fileAccess.readShort());
     fullSize += unc[i];
    } //Now we start piecing together the decompressed file
    seek += 2;
    fileAccess.seek(seek);
    byte[] finale = new byte[fullSize];
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //Decompress every stream and add it to the byte array
    for (int i = 0; i < streamCnt; i++) {
     byte[] temp = new byte[com[i]];
     fileAccess.readFully(temp);
     Inflater decompresser = new Inflater();
     decompresser.setInput(temp);
     byte[] result = new byte[unc[i]];
     decompresser.inflate(result);
     decompresser.end();

     outputStream.write(result);
    }
    finale = outputStream.toByteArray();
    fos.write(finale);
    fos.close();
    fileAccess.close();

    FileInputStream fis = new FileInputStream("temp_prev_tex");
    byte[] fullfinale = fis.readAllBytes();
    fis.close();
    return fullfinale;
    //ok, we're done
   }

  } catch (IOException e) {}
  return null;

 }

 public static byte[] compressFile(File workingFile, File decompressFile, boolean log) throws FileNotFoundException, IOException {
  File tempc = File.createTempFile("tempc", ".tmp");
  FileInputStream Stream = new FileInputStream(decompressFile); byte[] fileToCompress = Stream.readAllBytes(); Stream.close();
  switch (MiscUtils.getHeaderHexString(workingFile)) {
   case "504C4E62":
   case "414E4D62":
   case "42455662":
   case "4C564C62":
   case "434C4462":
   case "46534862":
   case "474D5462":
   case "4F415462":
   case "4D534862":
   case "534C5462":
   case "50414C62":
   case "50434B62":
   case "42505262":
    if (log) System.out.println("Format: Custom Compression Full");
    break;
   default:
    if (log) System.out.println("Not implemented | Not a valid file type");
    return null;
  }

  int CHUNK_SIZE = 32768;

  int numberOfChunks = ((int) Math.ceil((fileToCompress.length / CHUNK_SIZE))) + 1;

  int[] compressedSize = new int[numberOfChunks];
  int[] uncompressedSize = new int[numberOfChunks];
  ArrayList compressedData = new ArrayList < byte[] > ();
  List < byte[] > uncompressedData = MiscUtils.divideArray(fileToCompress, CHUNK_SIZE);
  for (int i = 0; i < numberOfChunks; i++) {
   Deflater deflater = new Deflater();
   uncompressedSize[i] = uncompressedData.get(i).length;
   ByteArrayOutputStream outputStream = new ByteArrayOutputStream(uncompressedData.get(i).length);
   deflater.setInput(uncompressedData.get(i));
   deflater.finish();
   byte[] chunk = new byte[1024];
   while (!deflater.finished()) {
    int count = deflater.deflate(chunk);
    outputStream.write(chunk, 0, count);
   }
   outputStream.close();
   compressedData.add(outputStream.toByteArray());
   compressedSize[i] = outputStream.size();
  }

  int seek = 8;
  try(FileInputStream fis = new FileInputStream("temp")) { try(FileOutputStream fos = new FileOutputStream(tempc)) { fos.write(fis.readAllBytes()); fis.close(); fos.close(); } }

  try(RandomAccessFile fileAccess = new RandomAccessFile(tempc, "rw")) {
    seek = 8;
    fileAccess.seek(seek);
    int dependencyOffset = fileAccess.readInt();

    fileAccess.seek(dependencyOffset);
    byte[] dependencies = new byte[(int)(fileAccess.length() - dependencyOffset)];
    fileAccess.read(dependencies);

    seek = 12;
    fileAccess.seek(seek);

    short type = fileAccess.readShort();

    if (type == 256) seek += 3;
    else seek += 8;

    fileAccess.seek(seek);
    fileAccess.writeShort(numberOfChunks);
    for (int i = 0; i < uncompressedSize.length; i++) {
     fileAccess.writeShort(compressedSize[i]);
     fileAccess.writeShort(uncompressedSize[i]);
    }
    for (int i = 0; i < numberOfChunks; i++) fileAccess.write((byte[]) compressedData.get(i));
    dependencyOffset = (int) fileAccess.getFilePointer();
    fileAccess.write(dependencies);

    fileAccess.seek(8);
    fileAccess.writeInt(dependencyOffset);
    
    fileAccess.close();
    
    FileInputStream fis = new FileInputStream(tempc);
    byte[] data = fis.readAllBytes();
    fis.close();
    tempc.delete();
    return data;
  }
 }
}