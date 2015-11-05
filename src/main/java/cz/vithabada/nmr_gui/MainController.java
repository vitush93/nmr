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

    /**
     * Indicates whether the data capture is running.
     */
    boolean running = false;

    /**
     * Indicated whether the RadioProcessor is connected.
     * Is continuously updated in background task which asks for board's presence
     * every 500 milisecons.
     */
    boolean boardConnected = false;

    /**
     * Pulse to be executed. Allows running data capture to be stopped from any method in this class.
     */
    Pulse<Complex[]> pulse;

    /**
     * Starts currently selected pulse with given parameters.
     * If no board is connected, displays alert window.
     */
    @FXML
    void handleStart() {
        if (!boardConnected) { // board is not present - display alert
            AlertHelper.showAlert(Alert.AlertType.ERROR, "No boards detected", "RadioProcessor is not connected.");

            return;
        }

        // TODO instantiate pulse wrapper based on currently selected tab and initialize its events
        pulse = new HahnEcho();
        initPulse();

        // TODO verify pulse parameters

        // start data capture task in background thread
        Task pulseTask = createPulseTask();

        Thread t = new Thread(pulseTask);
        t.setDaemon(true);
        t.start();

        // update UI to running state
        setRunningState();
    }

    /**
     * Asks RadioProcessor to stop capturing data which results in pulse
     * task succeeding and thus ending the pulse execution.
     */
    @FXML
    void handleStop() {
        pulse.stop();

        setReadyState();
    }

    /**
     * Displays window to configure the Attenuator.
     * If data capture is running, displays alert window
     * and prevents user to configure the Attenuator.
     *
     * @param event event args
     * @throws IOException
     */
    @FXML
    void handleOpenAttenuatorWindow(ActionEvent event) throws IOException {
        if (running) { // prevent user to setup the Attenuator if data capture is running
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Warning", "Data retrieval is currently running. Stop the pulse execution first to configure the USB Attenuator.");

            return;
        }

        // show Attenuator configuration window
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Attenuator.fxml"));

        Stage stage = new Stage();
        stage.setTitle("USB Attenuator");
        stage.setScene(new Scene(root, 300, 100));
        stage.setResizable(false);

        stage.show();
    }

    /**
     * Simply quits the entire application.
     */
    @FXML
    void handleQuit() {
        Stage stage = (Stage) menuBar.getScene().getWindow();

        stage.close();

        // TODO soft quit: ask RadioProcessor to stop data capture first
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // display API version and connected board count in right status.
        setRightStatus(SpinAPI.INSTANCE.pb_get_version(), SpinAPI.INSTANCE.pb_count_boards());

        setReadyState();
        startCheckForBoardBackgroundTask();
    }

    /**
     * Initialize pulse's callbacks.
     */
    void initPulse() {
        Invokable<Complex[]> updateChartOnFetch = createChartUpdateEvent();

        pulse.onFetch = updateChartOnFetch;
        pulse.onComplete = updateChartOnFetch;
        pulse.onRefresh = (sender, value) -> Platform.runLater(() -> leftStatus.setText("Current scan: " + value));
    }

    /**
     * Factory for pulse task in which data capture itself is running.
     *
     * @return pulse task.
     */
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

    /**
     * Factory forc chart update invokable.
     *
     * @return chart update invokable
     */
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

    /**
     * Updates UI to ready state: ready to start data capture - equivalent to initial state.
     */
    void setReadyState() {
        running = false;

        leftStatus.setText("Ready");

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

    /**
     * Updates UI to running state: state during the data capture.
     */
    void setRunningState() {
        running = true;

        startButton.setDisable(true);
        stopButton.setDisable(false);
    }

    /**
     * Updates rightStatus label with SpinAPI information.
     *
     * @param apiVersion      SpinAPI version string.
     * @param connectedBoards number of connected SpinCore boards.
     */
    void setRightStatus(String apiVersion, int connectedBoards) {
        String boardStatus = String.format("SpinAPI Version: %s, Connected boards: %d", apiVersion, connectedBoards);

        rightStatus.setText(boardStatus);
    }

    /**
     * Starts background task which continuously checks if RadioProcessor is connected.
     */
    void startCheckForBoardBackgroundTask() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), event -> {
            boardConnected = (SpinAPI.INSTANCE.pb_count_boards() > 0);

            setRightStatus(SpinAPI.INSTANCE.pb_get_version(), SpinAPI.INSTANCE.pb_count_boards());
        }), new KeyFrame(Duration.millis(500)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}
