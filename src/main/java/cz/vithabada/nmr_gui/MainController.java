package cz.vithabada.nmr_gui;

import com.dooapp.fxform.FXForm;
import cz.vithabada.nmr_gui.forms.FormFactory;
import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.pulse.HahnEcho;
import cz.vithabada.nmr_gui.pulse.HahnEchoCYCLOPS;
import cz.vithabada.nmr_gui.pulse.Pulse;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import libs.AlertHelper;
import libs.FFT;
import libs.Invokable;
import model.PlainTextData;
import model.RadioProcessor;
import org.apache.commons.math3.complex.Complex;
import api.SpinAPI;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class MainController implements Initializable {

    public static PulseTab selectedTab = PulseTab.HAHN_ECHO;

    private RadioProcessor radioProcessor;

    @FXML
    TabPane pulseTab;

    @FXML
    GridPane deviceParams;

    @FXML
    AnchorPane hahnContainer;

    @FXML
    GridPane dataTab;

    @FXML
    DataTabController dataTabController;

    @FXML
    DeviceParamsController deviceParamsController;

    @FXML
    LineChart<Number, Number> dataChart;

    @FXML
    LineChart<Number, Number> fftChart;

    @FXML
    LineChart<Number, Number> modulChart;

    @FXML
    Button startButton;

    @FXML
    Button contButton;

    @FXML
    Button stopButton;

    @FXML
    MenuBar menuBar;

    @FXML
    Menu dataMenu;

    @FXML
    Label leftStatus;

    @FXML
    Label rightStatus;

    private Stage stage;

    /**
     * Task which contains pulse execution.
     */
    private Task pulseTask;

    /**
     * Holds HahnEcho form values.
     */
    private HahnEchoParameters hahnEchoParameters = new HahnEchoParameters();

    /**
     * Starts currently selected pulse with given parameters.
     * If no board is connected, displays alert window.
     */
    @FXML
    void handleStart() throws Exception {
        if (!checkRadioProcessor()) return;

        // TODO refactor validation so that it can be used with other pulses
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<HahnEchoParameters>> constraintViolationSet = validator.validate(hahnEchoParameters);

        if (constraintViolationSet.size() > 0) {
            constraintViolationSet.stream().forEach(violation -> AlertHelper.showAlert(Alert.AlertType.ERROR, "Invalid value", "Invalid value for " + violation.getPropertyPath() + ": " + violation.getMessage()));

            return;
        }

        //pulse = createPulse();
        Pulse<Complex[]> pulse = createPulse();
        radioProcessor.setPulse(pulse);

        initPulse();

        // start data capture task in background thread
        pulseTask = createPulseTask();
        pulseTask.setOnSucceeded(this::pulseDone);
        pulseTask.setOnFailed(this::pulseError);

        Thread t = new Thread(pulseTask);
        t.setDaemon(true);
        t.start();

        // update UI to running state
        setRunningState();
    }

    private boolean checkRadioProcessor() {
        if (!radioProcessor.isBoardConnected()) { // board is not present - display alert
            AlertHelper.showAlert(Alert.AlertType.ERROR, "No boards detected", "RadioProcessor is not connected.");
        }

        return radioProcessor.isBoardConnected();
    }

    @FXML
    void handleContButton(ActionEvent actionEvent) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ContWindow.fxml"));
            root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Start a continuous scan");
            Scene scene = new Scene(root, 500, 140);
            stage.setScene(scene);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSaveData() {
        if (radioProcessor.getPulse() == null || radioProcessor.getData() == null) {
            AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Nothing to save", "Start data capture first.");

            return;
        }

        // display file browser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            if (file.exists()) {

                // if file exists, delete existing file
                boolean oldDeleted = file.delete();
                if (!oldDeleted) {
                    AlertHelper.showAlert(Alert.AlertType.ERROR, "Could not save data", "File could not be overwritten.");

                    return;
                }
            }

            // write to file
            PlainTextData plainTextData = new PlainTextData(radioProcessor.getData());
            plainTextData.toFile(file);
        }
    }

    private Pulse<Complex[]> createPulse() {
        if (selectedTab == PulseTab.HAHN_ECHO) {
            if (hahnEchoParameters.getCyclops()) {
                return new HahnEchoCYCLOPS(hahnEchoParameters);
            } else {
                return new HahnEcho(hahnEchoParameters);
            }
        } else if (selectedTab == PulseTab.CPMG) {
            return null; // TODO CPMG
        } else {
            return null;
        }
    }

    /**
     * Asks RadioProcessor to stop capturing data which results in pulse
     * task succeeding and thus ending the pulse execution.
     */
    @FXML
    void handleStop() {
        pulseTask.setOnSucceeded(event -> setReadyState());

        radioProcessor.stop();
    }

    /**
     * Simply quits the entire application.
     */
    @FXML
    private void handleQuit() {
        if (radioProcessor.isRunning()) {
            pulseTask.setOnSucceeded(event -> stage.close());

            radioProcessor.stop();
        } else {
            stage.close();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        radioProcessor = new RadioProcessor();

        // display API version and connected board count in right status.
        setRightStatus(SpinAPI.INSTANCE.pb_get_version(), SpinAPI.INSTANCE.pb_count_boards());

        setReadyState();
        startCheckForBoardBackgroundTask();

        FXForm hahnForm = FormFactory.create(rb, hahnEchoParameters, "/fxml/HahnEchoForm.fxml");
        hahnContainer.getChildren().add(hahnForm);

        // add pulse tab selection onChange() listener
        pulseTab.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {

            // on tab change update selected tab
            selectedTab = PulseTab.values()[(int) newValue];
        });
    }

    /**
     * Initialize pulse's callbacks.
     */
    private void initPulse() {
        Invokable<Complex[]> updateCharts = createChartUpdateEvent();

        radioProcessor.getPulse().onFetch = updateCharts;
        radioProcessor.getPulse().onComplete = updateCharts;
        radioProcessor.getPulse().onRefresh = (sender, value) -> Platform.runLater(() -> leftStatus.setText("Current scan: " + value));
    }

    /**
     * Factory for pulse task in which data capture itself is running.
     *
     * @return pulse task.
     */
    private Task createPulseTask() {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                radioProcessor.start();

                return null;
            }
        };
    }

    private void pulseDone(Event event) {
        setReadyState();

        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Done", "Data capture has been successfully completed.");
    }

    private void pulseError(Event event) {
        setReadyState();

        WorkerStateEvent workerStateEvent = (WorkerStateEvent)event;
        leftStatus.setText("Error has occured during program execution: " + workerStateEvent.getSource().getException().getMessage());
    }

    /**
     * Factory for chart update invokable.
     *
     * @return chart update invokable
     */
    private Invokable<Complex[]> createChartUpdateEvent(LineChart<Number, Number> lineChart) {
        return (sender, value) -> Platform.runLater(() -> {
            lineChart.getData().clear();

            final XYChart.Series<Number, Number> real = new XYChart.Series<>();
            final XYChart.Series<Number, Number> imag = new XYChart.Series<>();

            real.setName("Real");
            imag.setName("Imaginary");

            for (int i = 0, valueLength = value.length; i < valueLength; i++) { // TODO compute actual X axis
                Complex c = value[i];

                real.getData().add(new XYChart.Data<>(i, c.getReal()));
                imag.getData().add(new XYChart.Data<>(i, c.getImaginary()));
            }

            lineChart.getData().add(real);
            lineChart.getData().add(imag);
        });
    }

    /**
     * Factory for chart update invokable.
     * Wraps the createChartUpdateEvent(LineChart).
     * Updates both data plot and fft plot.
     *
     * @return chart update invokable
     */
    private Invokable<Complex[]> createChartUpdateEvent() {
        return (sender, value) -> {
            Invokable<Complex[]> dataChartUpdate = createChartUpdateEvent(dataChart);
            dataChartUpdate.invoke(sender, value);

            Invokable<Complex[]> fftChartUpdate = createChartUpdateEvent(fftChart);

            Complex[] transformed = FFT.transform(value);
            fftChartUpdate.invoke(sender, transformed);

            Invokable<Complex[]> modulChartUpdate = createChartUpdateEvent(modulChart);
            Complex[] modul = FFT.modul(value);
            modulChartUpdate.invoke(sender, modul);
        };
    }

    /**
     * Updates UI to ready state: ready to start data capture - equivalent to initial state.
     */
    private void setReadyState() {
        leftStatus.setText("Ready");

        startButton.setDisable(false);
        stopButton.setDisable(true);
        dataMenu.setDisable(false);
    }

    /**
     * Updates UI to running state: state during the data capture.
     */
    private void setRunningState() {
        startButton.setDisable(true);
        stopButton.setDisable(false);
        dataMenu.setDisable(true);
    }

    /**
     * Updates rightStatus label with SpinAPI information.
     *
     * @param apiVersion      SpinAPI version string.
     * @param connectedBoards number of connected SpinCore boards.
     */
    private void setRightStatus(String apiVersion, int connectedBoards) {
        String boardStatus = String.format("SpinAPI Version: %s, Connected boards: %d", apiVersion, connectedBoards);

        rightStatus.setText(boardStatus);
    }

    /**
     * Starts background task which continuously checks if RadioProcessor is connected.
     */
    private void startCheckForBoardBackgroundTask() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), event -> {
            radioProcessor.updateBoardStatus();

            setRightStatus(SpinAPI.INSTANCE.pb_get_version(), SpinAPI.INSTANCE.pb_count_boards());
        }), new KeyFrame(Duration.millis(500)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    void init(Stage stage) {
        this.stage = stage;

        this.stage.setOnCloseRequest(event -> handleQuit());
    }

    private enum PulseTab {
        HAHN_ECHO,
        CPMG
    }
}
