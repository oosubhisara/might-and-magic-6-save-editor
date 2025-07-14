package com.oosubhisara.mm6lod;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;

public class MM6LodData {
    static private Properties locations;

    private MM6LodFile lodFile;
    private String title;
    private String map;
    private int[] gold;
    private int food;
    private ImageView thumbnail;
    
    public static void setLocationNames(Properties locations) {
        MM6LodData.locations = locations;
    }
    
    public MM6LodData(MM6LodFile lodFile) {
        this.lodFile = lodFile;
        title = lodFile.getTitle();
        map = lodFile.getMapFileName();
        gold = lodFile.getGold();
        food = lodFile.getFood();
        thumbnail = new ImageView();
        
        // Read image data to BufferedImage
        ByteArrayInputStream bis = new ByteArrayInputStream(
                lodFile.getThumbnailData());
        try {
            BufferedImage img = ImageIO.read(bis);
            thumbnail.setImage(SwingFXUtils.toFXImage(img, null));
        } catch (IOException e) {
            System.out.println(getFileName() + ": " + e.toString());
        }
    }
    
    public MM6LodFile getLodFile() {
        return lodFile;
    }
    
    public void printDataHeaders() {
        lodFile.printDataHeaders();
    }

    public String getDataHeadersString() {
        return lodFile.getDataHeadersString();
    }
    
    public String getLodFileName() {
        return lodFile.getFileName();
    }
    
    public String getFileName() {
        return lodFile.getFileName();
    }

    public String getTitle() {
        return title;
    }

    public int getGold() {
        return gold[0];
    }

    public int getDepositedGold() {
        return gold[1];
    }
    
    public int getFood() {
        return food;
    }
    
    public boolean setTitle(String title) {
        if (lodFile.writeTitle(title)) {
            this.title = title;
            return true;
        }
        return false;
    }

    public boolean setGold(int amount) {
        if (lodFile.writeGold(amount)) {
            gold[0] = amount;
            return true;
        }
        return false;
    }

    public boolean setDepositedGold(int amount) {
        if (lodFile.writeDepositedGold(amount)) {
            gold[1] = amount;
            return true;
        }
        return false;
    }
    
    public boolean setFood(int amount) {
        if (lodFile.writeFood(amount)) {
            food = amount;
            return true;
        }
        return false;
    }
    
    public String getLocation() {
        String location = locations.getProperty(map.toLowerCase());
        return (location != null) ? location : "Unknown";
    }
    
    public int getSaveSlot() {
        return lodFile.getSaveSlot();
    }
    
    public String getModifiedTime() {
        return lodFile.getModifiedTime();
    }
    
    public String getMapName() {
        return map;
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }
}
