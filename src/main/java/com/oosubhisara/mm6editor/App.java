package com.oosubhisara.mm6editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.oosubhisara.util.FileUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    private static App instance;
    private Stage stage;
    private Controller controller;
    private String configDirectory;
    private Properties config;
    private Properties locations;
    
    public static App getInstance() {
        return instance;
    }
    
    public static String getName() {
        return "MM6SaveEditor";
    }
    
    public App() {
        super();
        App.instance = this;
        
        configDirectory = Path.of(
                FileUtil.getConfigDirectory(), getName()).toString();

        // Create the directory for configuration file
        try {
            Files.createDirectories(Path.of(configDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }

        config = new Properties();
        locations = new Properties();
    }

    public String getConfigFileName() {
        return Path.of(configDirectory, "settings.cfg").toString();
    }
    
    public String getLocationFileName() {
        return Path.of(configDirectory, "locations.txt").toString();
    }
    
    public Stage getStage() {
        return stage;
    }
    
    public Properties getConfig() {
        return config;
    }

    public Properties getLocations() {
        return locations;
    }
    
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ImageIO.scanForPlugins();

        stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(root, 800, 750));
        stage.setResizable(false);
        stage.getIcons().add(new Image(
                getClass().getResource("icon.png").toExternalForm()));
        stage.setTitle("Might and Magic 6 Save Editor");
        stage.setOnShown(e -> this.controller.updateView());
        stage.show();
    }
}
