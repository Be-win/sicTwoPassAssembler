import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

public class TwoPassAssemblerGUI extends Application {
    private TextField inputFileField;
    private TextArea intermediateFileOutput, symbolTableOutput, objectCodeOutput;
    private VBox mainPanel2;
    private BorderPane mainPane;
    VBox mainPanel;

    //Load Fonts
    Font font = Font.font("Arial", FontWeight.BOLD, 16);
    Font font2 = Font.font("Monospaced", FontWeight.LIGHT, 14);


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Two Pass Assembler");
        mainPane = new BorderPane();
        Scene scene = new Scene(mainPane, 800, 600);

        // Load the external stylesheet
        String cssPath = "/styles.css"; // Adjust the path if needed
        loadStylesheet(scene, cssPath);

        // Load image
        ImageView imageView = new ImageView(getImage("java.png"));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        // Heading
        Label headingLabel = new Label("Two Pass Assembler");
        headingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        headingLabel.getStyleClass().add("label"); // Add CSS class

        // Input field and button
        Label inputFileLabel = new Label("Input File:");
        inputFileField = new TextField();
        inputFileField.setPrefWidth(250);
        inputFileField.getStyleClass().add("text-field"); // Add CSS class

        Button browseBtn = new Button("Browse", new ImageView(getImage("browse-icon.png")));
        browseBtn.getStyleClass().add("button"); // Add CSS class
        browseBtn.setOnAction(e -> browseFile(primaryStage));

        HBox inputFields = new HBox(10, inputFileLabel, inputFileField, browseBtn);
        inputFields.setAlignment(Pos.CENTER);

        // Assemble button
        Button assembleBtn = new Button("Assemble");
        assembleBtn.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        assembleBtn.getStyleClass().add("button"); // Add CSS class
        assembleBtn.setOnAction(e -> assemble());

        VBox buttonBox = new VBox(10, assembleBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // Main panel setup
        mainPanel = new VBox(20, imageView, headingLabel, inputFields, buttonBox);
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: #ffffff;"); // White background for main panel

        // Initialize the secondary panel
        createSecondPanel();

        // Set the main panel in the center
        mainPane.setCenter(mainPanel);

        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void createSecondPanel() {
        // Intermediate File Output
        intermediateFileOutput = new TextArea();
        intermediateFileOutput.setWrapText(true);
        intermediateFileOutput.setFont(font2);
        intermediateFileOutput.setPromptText("Intermediate File Output");
        intermediateFileOutput.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #c0c0c0; -fx-border-radius: 5; -fx-border-width: 1;");

        Label intermediateFileLabel = new Label("Intermediate File Output");
        intermediateFileLabel.setFont(font);
        intermediateFileLabel.setStyle("-fx-text-fill: #333333;");

        VBox intermediateFileBox = new VBox(5, intermediateFileLabel, intermediateFileOutput);
        intermediateFileBox.setAlignment(Pos.CENTER);
        intermediateFileBox.setPadding(new Insets(10));
        intermediateFileBox.setStyle("-fx-border-color: #007BFF; -fx-border-radius: 10; -fx-padding: 10;");

        // Symbol Table Output
        symbolTableOutput = new TextArea();
        symbolTableOutput.setWrapText(true);
        symbolTableOutput.setFont(font2);
        symbolTableOutput.setPromptText("Symbol Table Output");
        symbolTableOutput.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #c0c0c0; -fx-border-radius: 5; -fx-border-width: 1;");

        Label symbolTableLabel = new Label("Symbol Table Output");
        symbolTableLabel.setFont(font);
        symbolTableLabel.setStyle("-fx-text-fill: #333333;");

        VBox symbolTableBox = new VBox(5, symbolTableLabel, symbolTableOutput);
        symbolTableBox.setAlignment(Pos.CENTER);
        symbolTableBox.setPadding(new Insets(10));
        symbolTableBox.setStyle("-fx-border-color: #007BFF; -fx-border-radius: 10; -fx-padding: 10;");

        // Combine the two output boxes
        HBox outputPanel = new HBox(10, intermediateFileBox, symbolTableBox);
        outputPanel.setAlignment(Pos.CENTER);
        outputPanel.setPadding(new Insets(10));

        // Object Code Output
        objectCodeOutput = new TextArea();
        objectCodeOutput.setWrapText(true);
        objectCodeOutput.setFont(font2);
        objectCodeOutput.setPromptText("Object Code Output");
        objectCodeOutput.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #c0c0c0; -fx-border-radius: 5; -fx-border-width: 1;");

        Label objectCodeLabel = new Label("Object Code Output");
        objectCodeLabel.setFont(font);
        objectCodeLabel.setStyle("-fx-text-fill: #333333;");

        VBox objectCodeBox = new VBox(5, objectCodeLabel, objectCodeOutput);
        objectCodeBox.setAlignment(Pos.CENTER);
        objectCodeBox.setPadding(new Insets(10));
        objectCodeBox.setStyle("-fx-border-color: #007BFF; -fx-border-radius: 10; -fx-padding: 10;");

        // Main VBox for all components
        VBox textAreaPanel = new VBox(10, outputPanel, objectCodeBox);
        textAreaPanel.setAlignment(Pos.CENTER);
        textAreaPanel.setPadding(new Insets(10));

        // Return button
        Button returnBtn = new Button("Return");
        returnBtn.setFont(Font.font("Arial", 16));
        returnBtn.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 5;");
        returnBtn.setOnAction(e -> mainPane.setCenter(mainPanel));

        // Main panel setup
        mainPanel2 = new VBox(10, textAreaPanel, returnBtn);
        mainPanel2.setAlignment(Pos.CENTER);
        mainPanel2.setPadding(new Insets(20));
        mainPanel2.setStyle("-fx-background-color: #ffffff;"); // Set a light background color for the main panel
    }



    private void assemble() {
        String inputFilePath = inputFileField.getText().trim();
        File inputFile = new File(inputFilePath);

        if (!inputFile.exists() || !inputFile.isFile()) {
            showAlert("Invalid input file path.");
            return;
        }

        try {
            TwoPassAssembler assembler = new TwoPassAssembler(inputFilePath);
            assembler.loadOptab();
            assembler.passOne();
            assembler.passTwo();

            displayResults(assembler);

            mainPane.setCenter(mainPanel2);
        } catch (Exception ex) {
            showAlert("Error during assembly: " + ex.getMessage());
        }
    }

    private void browseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            inputFileField.setText(file.getPath());
        }
    }

    private void displayResults(TwoPassAssembler assembler) {
        StringBuilder intermediateOutput = new StringBuilder();
        for (Map.Entry<Integer, String> entry : assembler.getIntermediateStart().entrySet()) {
            intermediateOutput.append(String.format("%04X  %s\n", entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<Integer, String> entry : assembler.getIntermediate().entrySet()) {
            intermediateOutput.append(String.format("%04X  %s\n", entry.getKey(), entry.getValue()));
        }
        intermediateFileOutput.setText(intermediateOutput.toString());

        StringBuilder symbolTableOutputStr = new StringBuilder();
        for (Map.Entry<String, Integer> entry : assembler.getSymtab().entrySet()) {
            symbolTableOutputStr.append(String.format("%s\t%04X\n", entry.getKey(), entry.getValue()));
        }
        symbolTableOutput.setText(symbolTableOutputStr.toString());

        StringBuilder objectCodeOutputStr = new StringBuilder();
        for (Map.Entry<Integer, String> entry : assembler.getObjectCode().entrySet()) {
            objectCodeOutputStr.append(entry.getValue()).append("\n");
        }
        objectCodeOutput.setText(objectCodeOutputStr.toString());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Image getImage(String path) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            System.out.println("Resource not found: " + path);
            return null;
        }
        return new Image(inputStream);
    }

    private void loadStylesheet(Scene scene, String cssPath) {
        URL cssResource = getClass().getResource(cssPath);
        if (cssResource != null) {
            // Add the stylesheet to the scene using URL
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            // Handle the case where the resource is not found
            System.err.println("CSS file not found: " + cssPath);
        }
    }
}
