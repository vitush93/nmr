package cz.vithabada.nmr_gui;

import com.dooapp.fxform.FXForm;
import cz.vithabada.nmr_gui.api.FTDI_Device;
import cz.vithabada.nmr_gui.forms.FormFactory;
import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.libs.PTS;
import cz.vithabada.nmr_gui.pulse.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import cz.vithabada.nmr_gui.libs.AlertHelper;
import cz.vithabada.nmr_gui.libs.FFT;
import cz.vithabada.nmr_gui.libs.Invokable;
import cz.vithabada.nmr_gui.model.Experiment;
import cz.vithabada.nmr_gui.model.PlainTextData;
import org.apache.commons.math3.complex.Complex;
import cz.vithabada.nmr_gui.api.SpinAPI;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author Vit Habada
 */
public class MainController implements Initializable {

    /**
     * Currently selected pulse tab.
     */
    private static Experiment.Pulse selectedTab = Experiment.Pulse.HAHN_ECHO;

    /**
     *
     */
    private Experiment experiment;

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

        experiment.init(hahnEchoParameters, selectedTab);

        initPulseEvents(experiment.getRadioProcessor().getPulse());
        initPulseTaskEvents(experiment.getTask());

        Thread t = new Thread(experiment.getTask());
        t.setDaemon(true);
        t.start();

        // update UI to running state
        setRunningState();
    }

    /**
     * Checks if SpinCore RadioProcessor is connected to the PC.
     * Displays alert dialog if the device is not connected.
     *
     * @return true if connected, false otherwise.
     */
    private boolean checkRadioProcessor() {
        if (!experiment.getRadioProcessor().isBoardConnected()) { // board is not present - display alert
            AlertHelper.showAlert(Alert.AlertType.ERROR, "No boards detected", "RadioProcessor is not connected.");
        }

        return experiment.getRadioProcessor().isBoardConnected();
    }

    /**
     * Handler for continuous experiment button.
     * Displays experiment configuration dialog.
     *
     * @param actionEvent
     */
    @FXML
    void handleContButton(ActionEvent actionEvent) {

        // Create the custom dialog.
        Dialog<ContExperiment> dialog = new Dialog<>();
        dialog.setTitle("Experiment dialog");
        dialog.setHeaderText("Start a continuous experiment");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        double ampGainValue = Double.parseDouble(deviceParamsController.getAttTextField().getText());
        double tauValue = hahnEchoParameters.getTau();
        double repetitionDelayValue = hahnEchoParameters.getRepetitionDelay();
        double amplitudeValue = hahnEchoParameters.getAmplitude();
        double ptsFreqValue = Double.parseDouble(deviceParamsController.getPtsTextField().getText());

        ContParameter ampGain = new ContParameter(ContParameter.AMP_GAIN, "Amp gain (dB)", ampGainValue);
        ContParameter tau = new ContParameter(ContParameter.TAU, "Tau (us)", tauValue);
        ContParameter repetitionDelay = new ContParameter(ContParameter.REPETITION_DELAY, "Rep delay (s)", repetitionDelayValue);
        ContParameter amplitude = new ContParameter(ContParameter.AMPLITUDE, "Amplitude", amplitudeValue);
        ContParameter ptsFreq = new ContParameter(ContParameter.PTS_FREQ, "PTS Freq (MHz)", ptsFreqValue);

        ChoiceBox<ContParameter> parameters = new ChoiceBox<>(FXCollections.observableArrayList(
                ampGain, tau, repetitionDelay, amplitude, ptsFreq
        ));
        parameters.setValue(ampGain);
        TextField step = new TextField();
        step.setPromptText("Step");
        step.setText("1");
        TextField iterations = new TextField();
        iterations.setPromptText("Iterations");
        iterations.setText("1");

        grid.add(new Label("Parameter:"), 0, 0);
        grid.add(parameters, 1, 0);
        grid.add(new Label("Step:"), 0, 1);
        grid.add(step, 1, 1);
        grid.add(new Label("Iterations:"), 0, 2);
        grid.add(iterations, 1, 2);

        Node startButton = dialog.getDialogPane().lookupButton(loginButtonType);

        // Do some validation (using the Java 8 lambda syntax).
        step.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                double stepVal = Double.parseDouble(newValue);

                startButton.setDisable(false);
            } catch (NumberFormatException e) {
                startButton.setDisable(true);
            }
        });

        iterations.textProperty().addListener(((observable, oldValue, newValue) -> {
            try {
                int iterationsVal = Integer.parseInt(newValue);

                startButton.setDisable(false);
            } catch (NumberFormatException e) {
                startButton.setDisable(true);
            }
        }));

        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> parameters.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    ContParameter parameterVal = parameters.getValue();
                    double stepVal = Double.parseDouble(step.getText());
                    int iterationsVal = Integer.parseInt(iterations.getText());

                    return new ContExperiment(parameterVal, stepVal, iterationsVal);
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            return null;
        });

        // display the dialog
        Optional<ContExperiment> result = dialog.showAndWait();

        // run the experiment with entered parameters
        result.ifPresent((contExperiment) -> {
            try {
                experiment = contExperiment;
                contExperiment(contExperiment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Configures and runs the continuous experiment.
     *
     * @param contExperiment data from continous experiment dialog.
     */
    private void contExperiment(ContExperiment contExperiment) throws Exception {

        // TODO check if HahnEcho or CPMG...
        contExperiment.init(hahnEchoParameters, selectedTab); // will set task and pulse
        contExperiment.setTask(new Task() {

            @Override
            protected Object call() throws Exception {
                while (contExperiment.getCurrentStep() <= contExperiment.getIterations()) {
                    try {
                        contExperiment.getRadioProcessor().start();

                        contExperiment.incrementCurrentStep(); // TODO check if valid parameter value
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();

                        break;
                    }
                }

                return null;
            }
        });

        // init UI events
        initPulseEvents(contExperiment.getRadioProcessor().getPulse());
        initPulseTaskEvents(contExperiment.getTask());

        // set onDone event - update desired parameter when single scan is finished
        contExperiment.getRadioProcessor().getPulse().onDone = createPulseParameterUpdateEvent(contExperiment);
        contExperiment.getRadioProcessor().getPulse().onComplete = (sender, value) -> {
            Platform.runLater(() -> leftStatus.setText("Using " + contExperiment.getParameter().getName() + " value " + contExperiment.getParameterValue()));
        };

        Thread t = new Thread(contExperiment.getTask());

        t.setDaemon(true);
        t.start();

        setRunningState();
    }

    private Invokable<Void> createPulseParameterUpdateEvent(ContExperiment contExperiment) {
        switch (contExperiment.getParameter().getId()) {
            case ContParameter.AMP_GAIN:
                return (sender, value) -> {
                    int currentValue = Integer.parseInt(deviceParamsController.getAttTextField().getText());
                    int newValue = (int) (currentValue + contExperiment.getCurrentStep() * contExperiment.getStep());

                    FTDI_Device.INSTANCE.device_set_attenuation(newValue);
                };
            case ContParameter.AMPLITUDE:
                if (contExperiment.getCurrentStep() == contExperiment.getIterations()) return (sender, value) -> {
                };

                return (sender, value) -> {
                    if (sender instanceof HahnEcho) {
                        HahnEcho hahnEcho = (HahnEcho) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEcho.getParameters();

                        float newValue = (float) contExperiment.getParameterValue();
                        hahnEchoParameters.setAmplitude(newValue);
                    } else if (sender instanceof HahnEchoCYCLOPS) {
                        HahnEchoCYCLOPS hahnEchoCYCLOPS = (HahnEchoCYCLOPS) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEchoCYCLOPS.getParameters();

                        float newValue = (float) contExperiment.getParameterValue();
                        hahnEchoParameters.setAmplitude(newValue);
                    } else {
                        // TODO other pulse series..
                    }
                };
            case ContParameter.PTS_FREQ:
                return (sender, value) -> {
                    double newValue = contExperiment.getParameterValue();

                    try {
                        PTS.setFrequency(newValue);
                    } catch (PTS.PTSException e) {
                        e.printStackTrace(); // TODO handle PTSException
                    }
                };
            case ContParameter.REPETITION_DELAY:
                return (sender, value) -> {
                    if (sender instanceof HahnEcho) {
                        HahnEcho hahnEcho = (HahnEcho) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEcho.getParameters();

                        double newValue = contExperiment.getParameterValue();
                        hahnEchoParameters.setRepetitionDelay(newValue);
                    } else if (sender instanceof HahnEchoCYCLOPS) {
                        HahnEchoCYCLOPS hahnEchoCYCLOPS = (HahnEchoCYCLOPS) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEchoCYCLOPS.getParameters();

                        double newValue = contExperiment.getParameterValue();
                        hahnEchoParameters.setRepetitionDelay(newValue);
                    } else {
                        // TODO other pulse series..
                    }
                };
            case ContParameter.TAU:
                return (sender, value) -> {
                    if (sender instanceof HahnEcho) {
                        HahnEcho hahnEcho = (HahnEcho) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEcho.getParameters();

                        double newValue = contExperiment.getParameterValue();
                        hahnEchoParameters.setTau(newValue);
                    } else if (sender instanceof HahnEchoCYCLOPS) {
                        HahnEchoCYCLOPS hahnEchoCYCLOPS = (HahnEchoCYCLOPS) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEchoCYCLOPS.getParameters();

                        double newValue = contExperiment.getParameterValue();
                        hahnEchoParameters.setTau(newValue);
                    } else {
                        // TODO other pulse series..
                    }
                };
            default:
                try {
                    throw new Exception("Invalid experiment parameter");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return (sender, value) -> {
                };
        }
    }

    /**
     * Displays file save dialog and saves the recorded data to ASCII file.
     */
    @FXML
    void handleSaveData() {
        if (experiment.getRadioProcessor().getPulse() == null || experiment.getRadioProcessor().getData() == null) {
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
            PlainTextData plainTextData = new PlainTextData(experiment.getRadioProcessor().getData());
            plainTextData.toFile(file);
        }
    }

    /**
     * Asks RadioProcessor to stop capturing data which results in pulse
     * task succeeding and thus ending the pulse execution.
     */
    @FXML
    void handleStop() throws Exception {
        experiment.getTask().setOnSucceeded(event -> setReadyState());
        experiment.getTask().setOnCancelled(event -> {
            experiment.getRadioProcessor().stop();
            setReadyState();
        });
        experiment.getTask().cancel();

        experiment.getRadioProcessor().stop();
    }

    /**
     * Simply quits the entire application.
     */
    @FXML
    private void handleQuit() throws Exception {
        if (isScanRunning()) {
            experiment.getTask().setOnSucceeded(event -> stage.close());

            experiment.getRadioProcessor().stop();
        } else {
            stage.close();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        experiment = new Experiment();

        // display API version and connected board count in right status.
        setRightStatus(SpinAPI.INSTANCE.pb_get_version(), SpinAPI.INSTANCE.pb_count_boards());

        setReadyState();
        startCheckForBoardBackgroundTask();

        FXForm hahnForm = FormFactory.create(rb, hahnEchoParameters, "/fxml/HahnEchoForm.fxml");
        hahnContainer.getChildren().add(hahnForm);

        // add pulse tab selection onChange() listener
        pulseTab.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {

            // on tab change update selected tab
            selectedTab = Experiment.Pulse.values()[(int) newValue];
        });
    }

    /**
     * Initialize pulse's callbacks.
     */
    private void initPulseEvents(Pulse pulse) {
        Invokable<Complex[]> updateCharts = createChartUpdateEvent();

        pulse.onFetch = updateCharts;
        pulse.onComplete = updateCharts;
        pulse.onRefresh = (sender, value) -> Platform.runLater(() -> {
            if (experiment instanceof ContExperiment) {
                ContExperiment contExperiment = (ContExperiment) experiment;
                leftStatus.setText("Current scan: " + value + ", " + contExperiment.getParameter().getName() + " value: " + (contExperiment.getParameterValue() - contExperiment.getStep()));
            } else {
                leftStatus.setText("Current scan: " + value);
            }
        });
    }

    /**
     * Initialize pulse task events.
     *
     * @param task
     * @throws Exception
     */
    private void initPulseTaskEvents(Task task) throws Exception {
        task.setOnSucceeded(this::pulseDone);
        task.setOnFailed(this::pulseError);
    }

    /**
     * Update UI after experiment is finished and show information dialog.
     *
     * @param event
     */
    private void pulseDone(Event event) {
        setReadyState();

        AlertHelper.showAlert(Alert.AlertType.INFORMATION, "Done", "Data capture has been successfully completed.");
    }

    /**
     * Update UI and show information dialog when error occurred.
     *
     * @param event
     */
    private void pulseError(Event event) {
        setReadyState();

        WorkerStateEvent workerStateEvent = (WorkerStateEvent) event;
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

        // TODO disable all parameter inputs when experiment is running
    }

    private boolean isScanRunning() {
        return startButton.isDisabled();
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
            experiment.getRadioProcessor().updateBoardStatus();

            setRightStatus(SpinAPI.INSTANCE.pb_get_version(), SpinAPI.INSTANCE.pb_count_boards());
        }), new KeyFrame(Duration.millis(500)));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    void init(Stage stage) {
        this.stage = stage;

        this.stage.setOnCloseRequest(event -> {
            try {
                handleQuit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
