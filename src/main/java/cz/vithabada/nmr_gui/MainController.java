package cz.vithabada.nmr_gui;

import cz.vithabada.nmr_gui.pulse.HahnEcho;
import cz.vithabada.nmr_gui.pulse.Pulse;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

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
    MenuBar menuBar;

    @FXML
    Label leftStatus;

    @FXML
    Label rightStatus;

    boolean started = false;

    boolean boardConnected = true; // TODO periodically check for board

    Pulse<Complex[]> pulse;

    @FXML
    void handleStart() {
        if (started) {
            pulse.stop();
            setReadyState();

            return;
        }

        if (!boardConnected) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "No boards detected", "RadioProcessor is not connected.");
            return;
        }

        pulse = new HahnEcho();
        initPulse();

        Task pulseTask = createPulseTask();

        Thread t = new Thread(pulseTask);
        t.setDaemon(true);
        t.start();

        setRunningState();
    }

    @FXML
    void handleOpenAttenuatorWindow(ActionEvent event) throws IOException {
        if (started) {
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

        task.setOnSucceeded(event -> {
            setReadyState();
        });

        task.setOnFailed(event -> {
            leftStatus.setText("Error has occured during program execution");
        });

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
        started = false;

        leftStatus.setText("Ready");
        startButton.setText("Start");
    }

    void setRunningState() {
        started = true;

        startButton.setText("Stop");
    }

    void startCheckForBoardBackgroundTask() {
        // TODO update boardConnected
    }
}
