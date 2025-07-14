package com.oosubhisara.util;

import java.nio.charset.StandardCharsets;

public class ByteArrayUtil {
    public static void printByteArray32(byte[] bytes) {
        System.out.println(String.format(
                "Bytes: %02X %02X %02X %02X" ,
                bytes[0], bytes[1], bytes[2], bytes[3]));
    }

    public static String formatByteArray(byte[] bytes, int length) {
        String result = "";
        int i = 0;
        while (i < length) {
            result += String.format("%02X ", bytes[i]);
            i++;
        }
        return result;
    }
    
    public static void printByteArrayString(byte[] bytes, int length) {
        int i = 0;
        while (i < length && bytes[i] != 0) {
            System.out.print(String.format("%02X ", bytes[i]));
            i++;
        }
        System.out.println();
    }

    public static String byteArrayToString(byte[] bytes) {
        // Remove trailing null characters
        int i;
        for (i = 0; i < bytes.length && bytes[i] != 0; i++) {}
        
        // Convert byte array to string
        return new String(bytes, 0, i, StandardCharsets.UTF_8);
    }

    public static int byteArrayToInt(byte[] bytes) {
        int result = 0;
        result = (bytes[0] & 0xFF) << 0 | 
                 (bytes[1] & 0xFF) << 8 | 
                 (bytes[2] & 0xFF) << 16 |
                 (bytes[3] & 0xFF) << 24; 
        
        return result;
    }
    
    public static byte[] intToByteArray(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xFF);
        b[1] = (byte) ((n >> 8) & 0xFF);
        b[2] = (byte) ((n >> 16) & 0xFF);
        b[3] = (byte) ((n >> 24) & 0xFF);
        return b;
    }
    
    public static byte[] stringToByteArray(String s) {
        byte[] b = new byte[s.length() + 1];
        for (int i = 0; i < s.length(); i++) {
            b[i] = (byte) s.charAt(i);
        }
        return b;
    }

}
