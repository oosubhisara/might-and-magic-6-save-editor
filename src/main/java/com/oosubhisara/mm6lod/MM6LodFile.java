package com.oosubhisara.mm6lod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oosubhisara.util.ByteArrayUtil;
import com.oosubhisara.util.TimeUtil;

public class MM6LodFile {
    record DataHeader(int offset, String name, int length) {
        public DataHeader setName(String name) {
            return new DataHeader(offset, name, length);
        }
    }

    private static final String LOD_HEADER_TYPE = "LOD";
    private static final String LOD_HEADER_GAME_ID = "MMVI";
    private static final int LOD_HEADER_TYPE_OFFSET = 0x00;
    private static final int LOD_HEADER_GAME_ID_OFFSET = 0x04;
    private static final int NUM_DATA_HEADERS_OFFSET = 0x11C;
    private static final int DATA_HEADER_SECTION_OFFSET = 0x120;
    private static final int DATA_HEADER_LENGTH = 32;
    private static final int DATA_FILENAME_LENGTH = 16;

    private static final int HEADER_BIN_TITLE_OFFSET = 0;
    private static final int HEADER_BIN_TITLE_LENGTH = 20;
    private static final int HEADER_BIN_MAP_FILENAME_OFFSET = 
                                HEADER_BIN_TITLE_LENGTH;
    private static final int HEADER_BIN_MAP_FILENAME_LENGTH = 20;
    
    private static final int PARTY_BIN_FOOD_OFFSET = 188;
    private static final int PARTY_BIN_FOOD_LENGTH = 4;
    private static final int PARTY_BIN_GOLD_OFFSET = 224;
    private static final int PARTY_BIN_GOLD_LENGTH = 4;
    private static final int PARTY_BIN_DEPOSITED_GOLD_OFFSET = 
            PARTY_BIN_GOLD_OFFSET + PARTY_BIN_GOLD_LENGTH;

    private static String directory;
    
    private int saveSlot;
    private String fileName;
    private String modifiedTime;
    
    private RandomAccessFile fileHandle;
    private int numDataFiles;
    private Map<String, DataHeader> dataHeaders;
    
    public static String getDirectory() {
        return MM6LodFile.directory;
    }

    public static void setDirectory(String directory) {
        MM6LodFile.directory = directory;
    }

    public MM6LodFile(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
        saveSlot = isAutoSave()?  1 : 
            Integer.parseInt( fileName.substring(4, 7)) + 2;
        modifiedTime = TimeUtil.formatFileTime(
                TimeUtil.getModifiedTime(getFullPath()));

        String filePath = Path.of(directory, fileName).toString();
        fileHandle = new RandomAccessFile(filePath, "rw");
        dataHeaders = new HashMap<>();
    }
    
    public void extractDataFiles(String outDirectory) throws IOException {
        for (var entry : dataHeaders.entrySet()) {
            DataHeader header = entry.getValue();

            System.out.println(String.format("Reading %s: 0x%08X - %d bytes",
                    header.name, header.offset, header.length));
            
            // Read data from LOD file
            byte[] buffer = new byte[header.length];
            fileHandle.seek(header.offset);
            fileHandle.readFully(buffer);
            
            // Write data to file
            String fileName = Path.of(outDirectory, header.name).toString();
            File f = new File(fileName);
            FileOutputStream fos = new  FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        }
    }
    
    public boolean isAutoSave() {
        return fileName.equals("autosave.mm6");
    }
    
    public String getFullPath() {
        return Path.of(MM6LodFile.directory, fileName).toString();
    }
    
    public int getSaveSlot() {
        return saveSlot;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public boolean checkFileHeader() {
        try {
            byte[] buffer = new byte[4] ;
            
            fileHandle.seek(LOD_HEADER_TYPE_OFFSET);
            int length = LOD_HEADER_TYPE.length();
            fileHandle.read(buffer, 0, length);
            String strHeaderType = new String(buffer, 0, length);
            if (!strHeaderType.equals(LOD_HEADER_TYPE)) return false;

            fileHandle.seek(LOD_HEADER_GAME_ID_OFFSET);
            length = LOD_HEADER_GAME_ID.length();
            fileHandle.read(buffer, 0, length);
            String strHeaderGame = new String(buffer, 0, length);
            if (!strHeaderGame.equals(LOD_HEADER_GAME_ID)) return false;
            
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void load() {
        readNumDataFiles();
        readDataHeaders();
    }
    
    public String getModifiedTime() {
        return modifiedTime;
    }

    public int getNumDataFiles() {
        return numDataFiles;
    }
    
    public String getTitle() {
        String title = "";
        
        if (isAutoSave()) {
            title = "Autosave";
        } else {
            DataHeader header  = dataHeaders.get("header.bin");
            byte[] buffer = new byte[HEADER_BIN_TITLE_LENGTH];
            try {
                fileHandle.seek(header.offset() + HEADER_BIN_TITLE_OFFSET);
                fileHandle.read(buffer);
                title = ByteArrayUtil.byteArrayToString(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return title;
    }
    
    public String getMapFileName() {
        String mapFileName = "";
        DataHeader header  = dataHeaders.get("header.bin");
        byte[] buffer = new byte[HEADER_BIN_MAP_FILENAME_LENGTH];
        try {
            fileHandle.seek(header.offset() + HEADER_BIN_MAP_FILENAME_OFFSET);
            fileHandle.read(buffer);
            mapFileName = ByteArrayUtil.byteArrayToString(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapFileName;
    }
    
    public int[] getGold() {
        int[] gold = {0, 0};  // [0] = Party Gold , [1] = Deposited Gold
        DataHeader header = dataHeaders.get("party.bin");
        byte[] buffer = new byte[PARTY_BIN_GOLD_LENGTH];

        try {
            fileHandle.seek(header.offset() + PARTY_BIN_GOLD_OFFSET);
            fileHandle.read(buffer);
            System.out.println(ByteArrayUtil.formatByteArray(buffer, 4));
            gold[0] = ByteArrayUtil.byteArrayToInt(buffer);
            fileHandle.read(buffer);
            gold[1] = ByteArrayUtil.byteArrayToInt(buffer);
            System.out.println(String.format( "%s - Gold: %d / %d", 
                        fileName, gold[0], gold[1]));
        } catch(IOException e) {
            System.err.println(e.toString());
        }
        
        return gold;
    }

    public int getFood() {
        int food = 0;
        DataHeader header = dataHeaders.get("party.bin");
        byte[] buffer = new byte[PARTY_BIN_FOOD_LENGTH];

        try {
            fileHandle.seek(header.offset() + PARTY_BIN_FOOD_OFFSET);
            fileHandle.read(buffer);
            food = ByteArrayUtil.byteArrayToInt(buffer);
            System.out.println(String.format( "%s - Food: %s / %d", 
                        fileName,
                        ByteArrayUtil.formatByteArray(buffer, PARTY_BIN_FOOD_LENGTH),
                        food));
        } catch(IOException e) {
            System.err.println(e.toString());
        }
        
        return food;
    }
    
    public byte[] getThumbnailData() {
        DataHeader header  = dataHeaders.get("image.pcx");
        byte[] thumbnailData = new byte[header.length()];

        try {
            fileHandle.seek(header.offset());
            fileHandle.read(thumbnailData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return thumbnailData;
    }

    public boolean writeTitle(String title) {
        System.out.println(String.format("Writing title (%s) to file", title));
        DataHeader header = dataHeaders.get("header.bin");
        return writeString(title, header.offset() + HEADER_BIN_TITLE_OFFSET);
    }

    public boolean writeGold(int amount) {
        System.out.println(String.format("Writing gold (%d) to file", amount));
        DataHeader header = dataHeaders.get("party.bin");
        return writeIntValue(amount, header.offset() + PARTY_BIN_GOLD_OFFSET);
    }

    public boolean writeDepositedGold(int amount) {
        System.out.println(String.format("Writing deposited gold (%d) to file", amount));
        DataHeader header = dataHeaders.get("party.bin");
        return writeIntValue(amount, header.offset() + 
                             PARTY_BIN_DEPOSITED_GOLD_OFFSET);
    }
    
    public boolean writeFood(int amount) {
        System.out.println(String.format("Writing food (%d) to file", amount));
        DataHeader header = dataHeaders.get("party.bin");
        return writeIntValue(amount, header.offset() + PARTY_BIN_FOOD_OFFSET);
    }
    
    public void printDataHeaders() {
        System.out.println("LOD Filename: " + fileName);
        System.out.println("------------------------------------------------");
        dataHeaders.forEach((key, header) -> {
            System.out.println(String.format(
                    "%-16s: Offset = 0x%08X (0x%08X)  Size = %d bytes", 
                    key, header.offset(), 
                    header.offset() - DATA_HEADER_SECTION_OFFSET,
                    header.length()));
        });
        System.out.println("------------------------------------------------");
        System.out.println();
    }

    public String getDataHeadersString() {
        // Create list of data header HashMap values
        List<DataHeader> sortedHeaders = 
                new ArrayList<>(dataHeaders.values());
        // Sort the list by offset
        Collections.sort(sortedHeaders, 
                         Comparator.comparing(DataHeader::offset));

        // Initialize data headers string
        String s = "Filename: " + fileName + "\n" +
                   "Numbers of data files: " + numDataFiles + "\n\n"; 
        int headerNumber = 0;

        // Collect all data header information
        for (var header : sortedHeaders) {
            headerNumber++;
            s += String.format(
                    "%03d. %-16s Offset = 0x%08X (+0x%08X)  Size = %d bytes\n", 
                    headerNumber, "\"" + header.name() + "\"", 
                    header.offset(), 
                    header.offset() - DATA_HEADER_SECTION_OFFSET,
                    header.length());
        }
        
        return s;
    }

    private void readNumDataFiles() {
        byte[] buffer = new byte[4];

        try {
            fileHandle.seek(NUM_DATA_HEADERS_OFFSET);
            fileHandle.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        numDataFiles = ByteArrayUtil.byteArrayToInt(buffer);
    }
    
    private void readDataHeaders() {
        try {
            fileHandle.seek(DATA_HEADER_SECTION_OFFSET);
            byte[] buffer = new byte[256];
            for (int i = 0; i < numDataFiles; i++) {
                int dataHeaderBytesLeft = DATA_HEADER_LENGTH;

                fileHandle.read(buffer, 0, DATA_FILENAME_LENGTH);  // File name
                String fileName = ByteArrayUtil.byteArrayToString(buffer);
                dataHeaderBytesLeft -= DATA_FILENAME_LENGTH;

                fileHandle.read(buffer, 0, 4);  // Offset
                int offset = DATA_HEADER_SECTION_OFFSET + 
                             ByteArrayUtil.byteArrayToInt(buffer);
                dataHeaderBytesLeft -= 4;

                fileHandle.read(buffer, 0, 4);  // Length
                int length = ByteArrayUtil.byteArrayToInt(buffer);
                dataHeaderBytesLeft -= 4;

                fileHandle.skipBytes(dataHeaderBytesLeft);

                DataHeader header = new DataHeader(
                        offset, fileName, length);
                if (!dataHeaders.containsKey(fileName)) {
                    dataHeaders.put(fileName, header); 
                } else {
                    header = header.setName("000-" + fileName);
                    dataHeaders.put("000-" + fileName, header); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeString(String string, long offset) {
        try {
            fileHandle.seek(offset);
            fileHandle.write(ByteArrayUtil.stringToByteArray(string));
        } catch (IOException e) {
            System.err.println(e.toString());
            return false;
        }
        return true;
    }
    
    private boolean writeIntValue(int value, long offset) {
        try {
            fileHandle.seek(offset);
            fileHandle.write(ByteArrayUtil.intToByteArray(value));
        } catch (IOException e) {
            System.err.println(e.toString());
            return false;
        }
        return true;
    }
    
}
