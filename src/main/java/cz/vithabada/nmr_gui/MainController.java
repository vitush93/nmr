package cz.vithabada.nmr_gui;

import cz.vithabada.nmr_gui.pulse.HahnEcho;
import cz.vithabada.nmr_gui.pulse.Pulse;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import libs.AlertHelper;
import libs.Complex;
import libs.Invokable;
import spinapi.SpinAPI;

public class MainController implements Initializable {

    @FXML
    LineChart<Number, Number> lineChart;

    @FXML
    Button startButton;

    @FXML
    Button stopButton;

    @FXML
    MenuBar menuBar;

    @FXML
    Label leftStatus;

    @FXML
    Label rightStatus;

    boolean running = false;

    boolean boardConnected = false;

    Pulse<Complex[]> pulse;

    @FXML
    void handleStart() {
        if (!boardConnected) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "No boards detected", "RadioProcessor is not connected.");

            return;
        }

        pulse = new HahnEcho(); // TODO pulse based on selected tab
        initPulse();

        Task pulseTask = createPulseTask();

        Thread t = new Thread(pulseTask);
        t.setDaemon(true);
        t.start();

        setRunningState();
    }

    @FXML
    void handleStop() {
        pulse.stop();
        setReadyState();
    }

    @FXML
    void handleOpenAttenuatorWindow(ActionEvent event) throws IOException {
        if (running) {
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Warning", "Data retrieval is currently running. Stop the pulse execution first to configure the USB Attenuator.");

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
        SpinAPI api = SpinAPI.INSTANCE;

        String boardStatus = String.format("SpinAPI Version: %s, Connected boards: %d", api.pb_get_version(), api.pb_count_boards());
        rightStatus.setText(boardStatus);

        setReadyState();
        startCheckForBoardBackgroundTask();
    }

    void initPulse() {
        Invokable<Complex[]> updateChartOnFetch = createChartUpdateEvent();

        pulse.onFetch = updateChartOnFetch;
        pulse.onComplete = updateChartOnFetch;
        pulse.onRefresh = (sender, value) -> Platform.runLater(() -> leftStatus.setText("Current scan: " + value));
    }

    Task createPulseTask() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                pulse.start();

                return null;
            }
        };

        task.setOnSucceeded(event -> setReadyState());

        task.setOnFailed(event -> leftStatus.setText("Error has occured during program execution"));

        return task;
    }

    Invokable<Complex[]> createChartUpdateEvent() {
        return (sender, value) -> Platform.runLater(() -> {
            lineChart.getData().clear();

            final XYChart.Series<Number, Number> real = new XYChart.Series<>();
            final XYChart.Series<Number, Number> imag = new XYChart.Series<>();

            real.setName("Real");
            imag.setName("Imaginary");

            for (int i = 0, valueLength = value.length; i < valueLength; i++) { // TODO compute actual X axis
                Complex c = value[i];

                real.getData().add(new XYChart.Data<>(i, c.getReal()));
                imag.getData().add(new XYChart.Data<>(i, c.getImag()));
            }

            lineChart.getData().add(real);
            lineChart.getData().add(imag);
        });
    }

    void setReadyState() {
        running = false;

        leftStatus.setText("Ready");

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    void setRunningState() {
        running = true;

        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    void startCheckForBoardBackgroundTask() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), event -> boardConnected = (SpinAPI.INSTANCE.pb_count_boards() > 0)), new KeyFrame(Duration.millis(500)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}
