package com.oosubhisara.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    public static String formatFileTime(FileTime fileTime) {
        LocalDateTime localDateTime = fileTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return localDateTime.format(DateTimeFormatter.ofPattern(
                "yyyy-MM-dd  HH:mm:ss"));
    }
    
    public static FileTime getModifiedTime(String fileName) {
        FileTime fileTime = null;
        try {
            Path path = Path.of(fileName);
            BasicFileAttributes attr = Files.readAttributes(
                    path, BasicFileAttributes.class);
            fileTime = attr.lastModifiedTime();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return fileTime;
    }
    
}
