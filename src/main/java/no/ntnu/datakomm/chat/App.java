package no.ntnu.datakomm.chat;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Class representing the main Graphical User Interface (GUI). JavaFX interface.
 */
public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method is called automatically by JavaFX when the application is
     * launched
     *
     * @param primaryStage The main "stage" where the GUI will be rendered
     */
    @Override
    public void start(Stage primaryStage) {
        URL r = getClass().getClassLoader().getResource("layout.fxml");
        Parent root = null;
        try {
            root = FXMLLoader.load(r);
        } catch (IOException e) {
            System.out.println("Error while loading FXML");
            return;
        }
        Scene scene = new Scene(root, 600, 400);
        scene.getStylesheets().add("styles/style.css");
        primaryStage.setTitle("NTNU Ålesund - ID203012 - ChatClient");
        primaryStage.setScene(scene);
        Image anotherIcon = new Image("styles/ntnu.png");
        primaryStage.getIcons().add(anotherIcon);
        primaryStage.show();
    }
}