import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }

    @Override
    public void start(Stage primaryStage) {
        TwoPassAssemblerGUI gui = new TwoPassAssemblerGUI();
        gui.start(primaryStage); // Start the JavaFX GUI
    }
}
