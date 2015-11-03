package cz.vithabada.nmr_gui;

import cz.vithabada.nmr_gui.pulse.HahnEcho;
import cz.vithabada.nmr_gui.pulse.Pulse;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import libs.Complex;
import spinapi.SpinAPI;

public class MainController implements Initializable {

    @FXML
    LineChart<Number, Number> lineChart;

    @FXML
    Button startButton;

    @FXML
    MenuBar menuBar;

    boolean started = false;
    Pulse<Complex[]> source;
    Thread worker;
    Timeline timeline;

    @FXML
    void handleStart() {
        if (started) {
            startButton.setText("Start");
            source.stop();
            timeline.stop();

            started = false;

            return;
        }

        //source = new RandomDataSource(16);
        source = new HahnEcho();
        worker = createWorker();

        started = true;
        startButton.setText("Stop");

        worker.start();
        timeline.play();
    }

    @FXML
    void handleOpenAttenuatorWindow(ActionEvent event) throws IOException {
        if (started) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Data retrieval is currently running. Stop the pulse execution first to configure the USB Attenuator.");

            alert.showAndWait();

            return;
        }

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Attenuator.fxml"));

        Stage stage = new Stage();
        stage.setTitle("USB Attenuator");
        stage.setScene(new Scene(root, 300, 100));
        stage.setResizable(false);

        stage.show();
    }

    @FXML
    void handleQuit() {
        Stage stage = (Stage) menuBar.getScene().getWindow();

        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), (ActionEvent event) -> {
            Complex[] data = source.getData();
            if (data != null) {
                updateChart(data);
            }
        }), new KeyFrame(Duration.seconds(1)));

        timeline.setCycleCount(Animation.INDEFINITE);

        SpinAPI api = SpinAPI.INSTANCE;
        System.out.println("SpinAPI Version: " + api.pb_get_version());
        System.out.println("Connected boards: " + api.pb_count_boards());
    }

    void updateChart(Complex[] value) {
        lineChart.getData().clear();

        final XYChart.Series<Number, Number> real = new XYChart.Series();
        final XYChart.Series<Number, Number> imag = new XYChart.Series();

        real.setName("Real");
        imag.setName("Imaginary");

        imag.getData().clear();

        int count = 0;
        for (Complex c : value) {
            real.getData().add(new XYChart.Data(count, c.getReal()));
            imag.getData().add(new XYChart.Data(count, c.getImag()));

            count++;
        }

        lineChart.getData().add(real);
        lineChart.getData().add(imag);
    }

    Thread createWorker() {
        worker = new Thread(() -> {
            source.start();
        });

        worker.setDaemon(true);

        return worker;
    }
}
