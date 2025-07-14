package com.oosubhisara.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtil {
    private static final int OS_UNKNOWN = -1;
    private static final int OS_LINUX = 0;
    private static final int OS_WINDOWS = 1;
    private static final int OS_MAC = 2;

    /**
     * Return common configuration file location according to OS 
     */
    public static String getConfigDirectory() {
        String directory = "";
        int os = detectOS();

        switch (os) {
            case OS_LINUX: 
                directory = Path.of(
                        System.getProperty("user.home"), ".config")
                              .toString();
                break;
            case OS_WINDOWS:
                break;
            case OS_MAC: 
                break;
        }
        
        return directory;
    }
    
    /**
     * Detect the operating system the application is running on
     * 
     * @return integer constant represent the operating system
     */
    private static int detectOS() {
        int os = OS_UNKNOWN;
        String name = System.getProperty("os.name").toLowerCase();
        if (name.contains("linux")) os = OS_LINUX;
        else if (name.contains("win")) os = OS_WINDOWS;
        else if (name.contains("mac")) os = OS_MAC;
        return os;
    }

    public static boolean fileExists(String fileName) {
        File f = new File(fileName);
        return f.exists() && !f.isDirectory(); 
    }
    
    public static boolean copyFile(InputStream is,
                                             String targetFileName,
                                             boolean replaceExisting) {
        try {
            if (replaceExisting) {
                Files.copy(is, Path.of(targetFileName), 
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(is, Path.of(targetFileName));
            }
            return true;
        } catch (IOException e) {
            return false; 
        }
    }
}
