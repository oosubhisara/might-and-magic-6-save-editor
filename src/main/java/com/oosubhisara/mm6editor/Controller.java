package com.oosubhisara.mm6editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oosubhisara.mm6lod.MM6LodData;
import com.oosubhisara.mm6lod.MM6LodFile;
import com.oosubhisara.util.FileUtil;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Controller implements Initializable { 
    @FXML private TableView<MM6LodData> tvSaveFiles;
    @FXML private TableView<DetailedItem> tvDetails;
    
    private App app;
    private Properties config;
    private Properties locations;
    private DirectoryChooser saveDirectoryChooser;
    private DirectoryChooser outputDirectoryChooser;
    private String saveDirectory;
    private String outputDirectory;

    public Controller() {
        app = App.getInstance();
        app.setController(this);
        config = app.getConfig();
        locations = app.getLocations();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String homeDirectory = System.getProperty("user.home");
        saveDirectoryChooser = new DirectoryChooser();
        outputDirectoryChooser = new DirectoryChooser();

        loadConfigFile();
        loadLocationFile();
        MM6LodData.setLocationNames(locations);

        saveDirectory = config.getProperty("save-directory");
        if (saveDirectory == null) {
            saveDirectory = homeDirectory;
        }
        
        outputDirectory = config.getProperty("output-directory");
        if (outputDirectory == null) {
            outputDirectory = homeDirectory;
        }

        // Initialize UI
        initTvSaveFiles();
        initTvDetails();
    }

    //----------------- Event Handlers ----------------------------------------
    @FXML private void onSelectSaveDirectory(ActionEvent e) {
        saveDirectoryChooser.setInitialDirectory(new File(saveDirectory));
        File directory = saveDirectoryChooser.showDialog( app.getStage());
        if (directory != null) {
            saveDirectory = directory.toString();
            updateView();
            config.setProperty("save-directory", saveDirectory);
            saveConfigFile();
        }
    }
    
    @FXML private void onQuit(ActionEvent e) {
        Platform.exit();
    }

    @FXML private void onRefresh(ActionEvent e) { updateView(); }
    @FXML private void onEditTitle(ActionEvent e) { editTitle(); }
    @FXML private void onEditGold(ActionEvent e) { editGold(); }

    @FXML private void onEditDepositedGold(ActionEvent e) { 
        editDepositedGold(); 
    }

    @FXML private void onEditFood(ActionEvent e) { editFood(); }

    @FXML private void onShowFileOffsets(ActionEvent e) {
        MM6LodData data = tvSaveFiles.getSelectionModel().getSelectedItem();
        showTextDialog("Data Headers", data.getDataHeadersString(), 650, 600);
    }

    @FXML private void onExtractDataFiles(ActionEvent e) {
        outputDirectoryChooser.setInitialDirectory(new File(outputDirectory));
        File directory = outputDirectoryChooser.showDialog(app.getStage());
        if (directory == null) return;
        
        outputDirectory = directory.toString();
        config.setProperty("output-directory", outputDirectory);
        MM6LodData data = tvSaveFiles.getSelectionModel().getSelectedItem();
        MM6LodFile lodFile = data.getLodFile();
        try {
            lodFile.extractDataFiles(outputDirectory);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    //----------------- End of Event Handlers ---------------------------------
    
    
    private void loadConfigFile() {
        System.out.println("Loading config file");
        String configFilePath = app.getConfigFileName();
        try {
            config.load(new FileInputStream(configFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLocationFile() {
        String locationFilePath = app.getLocationFileName();

        // if location file does not exists in configuration directory
        // then create a new copy from resources
        if (!FileUtil.fileExists(locationFilePath)) {
            if (!FileUtil.copyFile(
                    MM6LodData.class.getResourceAsStream("locations.txt"),
                    locationFilePath, false)) {
                // Failed to copy locations file, skip loading
                System.err.println("Failed to create " + locationFilePath);
                return;
            }
        }

        // Load location file from configuration directory
        System.out.println("Loading location file");
        try {
            locations.load(new FileInputStream(locationFilePath));
        } catch (FileNotFoundException e) {
            System.err.println(locationFilePath + " not found!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveConfigFile() {
        System.out.println("Saving config file");
        String configFilePath = app.getConfigFileName();

        try (FileWriter writer = new FileWriter(configFilePath)) {
            writer.write("# " + App.getName() + " configuration file\n");
            
            config.forEach( (k, v) -> {
                try {
                    writer.write(String.format("%s=%s\n", k, v));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    private void initTvSaveFiles() {
        // Create columns
        var slot = new TableColumn<MM6LodData, String>("#");
        slot.setCellValueFactory(new PropertyValueFactory<>("saveSlot"));
        slot.setPrefWidth(30);
        var image = new TableColumn<MM6LodData, ImageView>("Thumbnail");
        image.setCellValueFactory(new PropertyValueFactory<>("thumbnail"));
        image.setPrefWidth(120);
        var title = new TableColumn<MM6LodData, String>("Title");
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        title.setPrefWidth(170);
        var modifiedTime = new TableColumn<MM6LodData, String>("Modified Time");
        modifiedTime.setCellValueFactory(new PropertyValueFactory<>(
                                         "modifiedTime"));
        modifiedTime.setPrefWidth(170);
        var mapTitle = new TableColumn<MM6LodData, String>("Location");
        mapTitle.setCellValueFactory(new PropertyValueFactory<>("location"));
        mapTitle.setPrefWidth(160);

        // Add columns
        var columns = tvSaveFiles.getColumns();
        columns.add(slot);
        columns.add(image);
        columns.add(title);
        columns.add(mapTitle);
        columns.add(modifiedTime);

        tvSaveFiles.setOnMouseClicked(e -> updateDetailedView());
        modifiedTime.setSortType(TableColumn.SortType.DESCENDING);
        tvSaveFiles.getSortOrder().add(modifiedTime);
    }
    
    private void initTvDetails() {
        // Create details columns
        var name = new TableColumn<DetailedItem, String>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        name.setPrefWidth(200);
        var value = new TableColumn<DetailedItem, String>("Value");
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        value.setPrefWidth(400);

        var columns = tvDetails.getColumns();
        columns.add(name);
        columns.add(value);
        
        tvDetails.setOnMouseClicked(e -> {
            DetailedItem item = tvDetailsLMBTest(e, 2);
            if (item != null) {
                switch (item.name) {
                    case "Title": editTitle(); break;
                    case "Gold": editGold(); break;
                    case "Deposited Gold": editDepositedGold(); break;
                    case "Food": editFood(); break;
                }
            }
        });
    }
    
    public void updateView() {
        System.out.println("Updating view");
        Runnable task = () -> {
            List<String> fileList = getFileList();
            Platform.runLater(() -> {
                taskUpdateView(fileList);
            });
        };
        new Thread(task).start();
    }

    private void taskUpdateView(List<String> fileNames) {
        tvSaveFiles.getItems().clear();
        tvSaveFiles.requestFocus();

        // Read all the save files
        for (String fileName : fileNames) {
            MM6LodFile lodFile = null;
            try {
                lodFile = new MM6LodFile(fileName);
                if (!lodFile.checkFileHeader()) {  // Skip invalid save
                    continue;
                }
                lodFile.load();
            } catch (IOException e) {
                System.err.println(e.toString());
                continue;
            }

            // Pull data from LOD file and add to Save-file table
            MM6LodData data = new MM6LodData(lodFile);
            tvSaveFiles.getItems().add(data);
        }

        // Finished loading 
        tvSaveFiles.sort();
        tvSaveFiles.getSelectionModel().select(0);
        updateDetailedView();
    }
    
    private List<String> getFileList() {
        MM6LodFile.setDirectory(saveDirectory);
        
        if (saveDirectory.isEmpty()) return new ArrayList<>();
            
        
        List<String> fileNames = null;
        try (Stream<Path> stream = Files.list(Path.of(saveDirectory))) {
            Set<String> fileNameSet = stream
                    .filter(file -> Files.isRegularFile(file))
                    .filter(file -> file.toString().endsWith(".mm6"))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
            fileNames = new ArrayList<>(fileNameSet);
            Collections.sort(fileNames);
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        
        return fileNames;
    }
    
    private void updateDetailedView() {
        MM6LodData data = tvSaveFiles.getSelectionModel().getSelectedItem();
        if (data != null) {
            ObservableList<DetailedItem> items = tvDetails.getItems();
            items.clear();
            items.add(new DetailedItem("Title", data.getTitle())); 
            items.add(new DetailedItem("Location", data.getLocation()));
            items.add(new DetailedItem("Gold", data.getGold()));
            items.add(new DetailedItem("Deposited Gold", 
                    data.getDepositedGold()));
            items.add(new DetailedItem("Food", data.getFood()));
            items.add(new DetailedItem("Map", data.getMapName()));
            items.add(new DetailedItem("Filename", data.getLodFileName()));
        }
    }

    private void showTextDialog(String title, String text, 
                                int width, int height) {
        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(0, 0, 10, 0));
        vBox.setAlignment(Pos.TOP_CENTER);

        TextArea ta = new TextArea();
        ta.setText(text);
        ta.setFont(Font.font("MonoSpace"));
        
        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(64);
        btnOk.setOnAction(e -> dialogStage.close());

        var children = vBox.getChildren();
        children.add(ta);
        children.add(btnOk);
        VBox.setVgrow(ta, Priority.ALWAYS);

        dialogStage.setScene(new Scene(vBox, width, height));
        dialogStage.setTitle(title);
        dialogStage.show();
    }

    private void editTitle() {
        MM6LodData data = tvSaveFiles.getSelectionModel().getSelectedItem();
        String response = AskForString(
                "Edit Title", "Enter new title", data.getTitle(), 19);
        if (response != null) {
            if (data.setTitle(response)) {
                tvSaveFiles.refresh();
                updateDetailedView();
            }
        }
    }
    
    private void editGold() {
        MM6LodData data = tvSaveFiles.getSelectionModel().getSelectedItem();
        int value = data.getGold();
        int newValue = AskForInt(
                "Edit Gold", "Enter new amount of gold", 
                value, 0, 999999);
        if (newValue != value) {
            if (data.setGold(newValue)) {
                updateDetailedView();
            }
        }
    }

    private void editDepositedGold() {
        MM6LodData data = tvSaveFiles.getSelectionModel().getSelectedItem();
        int value = data.getDepositedGold();
        int newValue = AskForInt(
                "Edit Deposited Gold", "Enter new amount of deposited gold", 
                value, 0, 999999);
        if (newValue != value) {
            if (data.setDepositedGold(newValue)) {
                updateDetailedView();
            }
        }
    }

    private void editFood() {
        MM6LodData data = tvSaveFiles.getSelectionModel().getSelectedItem();
        int value = data.getFood();
        int newValue = AskForInt(
                "Edit Food", "Enter new amount of food", value, 0, 10000);
        if (newValue != value) {
            if (data.setFood(newValue)) {
                updateDetailedView();
            }
        }
    }

    private String AskForString(String title, String header, String value,
                                int maxLength) {
        var dialog = new TextInputDialog(value);
        dialog.setTitle(title);
        dialog.setHeaderText(
                header + String.format("  (Max: %d charecters)", maxLength));
        
        String result = "";
        
        while (result != null && 
                (result.isEmpty() || result.length() > maxLength)) { 
            result = dialog.showAndWait().orElse(null);
        }
        
        return result;
    }
    
    private int AskForInt(String title, String header, int value,
                             int min, int max) {
        var dialog = new TextInputDialog(String.valueOf(value));
        dialog.setTitle(title);
        dialog.setHeaderText(header + String.format("  (%d - %d)", min, max));
        
        int newValue = value;
        
        while (newValue == value) { 
            String response = dialog.showAndWait().orElse(null);
            if (response == null) break;

            newValue = Integer.parseInt(response);
            if (newValue < min || newValue > max) {
                newValue = value;
            }
        }
        
        return newValue;
    }
    
    private DetailedItem tvDetailsLMBTest( MouseEvent e, int clickCount) {
        if (e.getClickCount() == clickCount) {
            return tvDetails.getSelectionModel().getSelectedItem();
        }
        
        return null;
    }
}
