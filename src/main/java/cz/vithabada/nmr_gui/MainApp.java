package cz.vithabada.nmr_gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setMinWidth(900);
        stage.setMinHeight(500);

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
