package cz.vithabada.nmr_gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import libs.AlertHelper;
import spinapi.SpinAPI;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMinWidth(900);
        stage.setMinHeight(500);

        // check if SpinAPI is installed
        try {
            System.loadLibrary((System.getProperty("sun.arch.data.model").contains("64")) ? "spinapi64" : "spinapi");
        } catch (Error error) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Missing SpinAPI", "It seems that SpinAPI is not installed on this computer. Please install SpinAPI first to use this software.");

            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("NMR GUI");
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:icon.png"));

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
