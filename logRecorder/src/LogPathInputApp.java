import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LogPathInputApp extends Application {
    private ArrayList<LogRecord> logRecords;
    private ComboBox<String> sortOptions;
    private ListView<String> listView;
    private ArrayList<LogRecord> originalRecords;
    private Stack<ArrayList<LogRecord>> historyStack;


    @Override
    public void start(Stage primaryStage) {
        originalRecords = new ArrayList<>();
        historyStack = new Stack<>();


        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Log File Path Input");
        dialog.setHeaderText("Log File Path Input");
        dialog.setContentText("Please enter the log file path:");

        listView = new ListView<>();


        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            processPath(result.get());
        }


        TextField filterField = new TextField();
        filterField.setPromptText("Enter filter command");

        Button filterButton = new Button("Filter");
        Button saveButton = new Button("Save");
        Button loadButton = new Button("Load");
        Button resetButton = new Button("Reset");
        Button backButton = new Button("Back");
        Button sortButton = new Button("Sort");






        sortOptions = new ComboBox<>();
        sortOptions.getItems().addAll("timestamp", "username", "ip", "role", "url", "description"); // Replace with actual field names as appropriate
        sortOptions.setValue("timestamp"); // Set a default value

        filterButton.setOnAction(e -> applyFilter(filterField.getText()));
        saveButton.setOnAction(e -> saveRecords());
        loadButton.setOnAction(e -> loadRecords());
        resetButton.setOnAction(e -> resetRecords());
        backButton.setOnAction(e -> rollbackAction());
        sortButton.setOnAction(e -> sortRecords(sortOptions.getValue()));



        VBox layout = new VBox(10);
        layout.getChildren().addAll(filterField, filterButton, saveButton, loadButton, resetButton, backButton, sortOptions, sortButton);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(listView);



        primaryStage.setTitle("Log File Analyzer");
        primaryStage.setScene(new Scene(layout, 500, 500));
        primaryStage.show();
    }
    private void updateDisplay() {

        listView.getItems().clear();


        for (LogRecord record : logRecords) {
            listView.getItems().add(record.toString()); // Convert LogRecord objects to strings for display
        }
    }

    private void applyFilter(String command) {
        historyStack.push(new ArrayList<>(logRecords));

        if (command.contains("Date")){
            String parts[]=command.split(" ");
            String field = parts[0];
            String operation = parts[1];
            String value = parts[2];
            String temp=value.substring(1,value.length()-1);
            Predicate<LogRecord> predicate = createPredicate(field, operation, temp);
            logRecords = logRecords.stream().filter(predicate).collect(Collectors.toCollection(ArrayList::new));
            updateDisplay(); // Refresh your display method to show filtered records
            
        } else if (command.contains("Timestamp")) {
            String parts[]=command.split(" ");
            Predicate<LogRecord> predicate = createPredicate(parts[0], parts[1], parts[2]);
            logRecords = logRecords.stream().filter(predicate).collect(Collectors.toCollection(ArrayList::new));
            updateDisplay();
        }
        else{


        String[] parts = command.split("\\.",2);
        String field = parts[0];
        String operation = parts[1].substring(0, parts[1].indexOf('('));
        String value = parts[1].substring(parts[1].indexOf('(') + 1, parts[1].indexOf(')'));
        String temp=value.substring(1,value.length()-1);

        Predicate<LogRecord> predicate = createPredicate(field, operation, temp);
        logRecords = logRecords.stream().filter(predicate).collect(Collectors.toCollection(ArrayList::new));
        updateDisplay();}
    }
    private Predicate<LogRecord> createPredicate(String field, String operation, String value) {
        switch (field.toLowerCase()) {
            case "description":
                if (operation.equalsIgnoreCase("contains")) {
                    return record -> record.getDescription() != null && record.getDescription().contains(value);
                }
                break;
            case "url":
                if (operation.equalsIgnoreCase("contains")) {
                    return record -> record.getUrl() != null && record.getUrl().contains(value);
                }
                break;
            case "ipaddress":
                if (operation.equalsIgnoreCase("contains")) {
                    return record -> record.getIp() != null && record.getIp().contains(value);
                }
                break;
            case "role":
                if (operation.equalsIgnoreCase("contains")) {
                    return record -> record.getRole() != null && record.getRole().contains(value);
                }
                break;
            case "username":
                if (operation.equalsIgnoreCase("contains")) {
                    return record -> record.getUsername() != null && record.getUsername().contains(value);
                }
                break;
            case "date":
                return record -> record.getDate().equals(value);
            case "timestamp":
                try {
                    long timestampValue = Long.parseLong(value);
                    switch (operation) {
                        case "=":
                            return record -> record.getTimestamp().getTime() == timestampValue;
                        case "<":
                            return record -> record.getTimestamp().getTime() < timestampValue;
                        case ">":
                            return record -> record.getTimestamp().getTime() > timestampValue;
                        default:

                            return record -> false;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Timestamp value is not a valid long: " + value);
                    return record -> false;
                }


        }
        return record -> false;
    }

    private void saveRecords() {
        TextInputDialog dialog = new TextInputDialog("C:/defaultPath/defaultFileName.ser");
        dialog.setTitle("Save Log Records");
        dialog.setHeaderText("Enter the full path and filename to save:");
        dialog.setContentText("Path:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(path -> {
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs(); // Ensure directory exists
            }
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(logRecords);
                out.writeObject(originalRecords);
                out.writeObject(historyStack);
                System.out.println("Records saved successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }




    private void loadRecords() {
        TextInputDialog dialog = new TextInputDialog("C:/defaultPath/defaultFileName.ser");
        dialog.setTitle("Load Log Records");
        dialog.setHeaderText("Enter the full path and filename to load from:");
        dialog.setContentText("Path:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(path -> {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("File not found!");
                return;
            }
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                logRecords = (ArrayList<LogRecord>) in.readObject();
                originalRecords = (ArrayList<LogRecord>) in.readObject();
                historyStack = (Stack<ArrayList<LogRecord>>) in.readObject();
                updateDisplay();
                System.out.println("Records loaded successfully from " + file.getAbsolutePath());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
    



    private void resetRecords() {
        logRecords = new ArrayList<>(originalRecords);
        historyStack.clear();
        updateDisplay();
    }

    private void rollbackAction() {
        if (!historyStack.isEmpty()) {
            logRecords = historyStack.pop();
        } else {
            System.out.println("No previous state to rollback to.");
        }
        updateDisplay();
    }

    private void sortRecords(String sortField) {
        Comparator<LogRecord> comparator;
        switch (sortField.toLowerCase()) {
            case "timestamp":
                comparator = Comparator.comparing(LogRecord::getTimestamp);
                break;
            case "username":
                comparator = Comparator.comparing(LogRecord::getUsername);
                break;
            case "ip":
                comparator = Comparator.comparing(LogRecord::getIp);
                break;
            case "role":
                comparator = Comparator.comparing(LogRecord::getRole);
                break;
            case "url":
                comparator = Comparator.comparing(LogRecord::getUrl);
                break;
            case "description":
                comparator = Comparator.comparing(LogRecord::getDescription);
                break;

            default:
                comparator = Comparator.comparing(LogRecord::getTimestamp);
                break;
        }

        Collections.sort(logRecords, comparator);
        updateDisplay();
    }


    private void processPath(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found!");
            return;
        }

        logRecords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String logFilePath;
            while ((logFilePath = reader.readLine()) != null) {
                ArrayList<LogRecord> records = LogRecord.readLogRecords(logFilePath.trim());
                logRecords.addAll(records);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(logRecords);

        for (LogRecord record : logRecords) {
            System.out.println(record);
        }
        originalRecords = new ArrayList<>(logRecords);
        updateDisplay();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
