package cz.vithabada.nmr_gui;

import com.dooapp.fxform.FXForm;
import cz.vithabada.nmr_gui.api.FTDI_Device;
import cz.vithabada.nmr_gui.forms.FormFactory;
import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.libs.*;
import cz.vithabada.nmr_gui.model.PTSChartViewModel;
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
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
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

    /**
     *
     */
    private PTSChartViewModel ptsChartViewModel;

    @FXML
    TabPane pulseTab;

    @FXML
    Tab statsPlot;

    @FXML
    Tab modPlot;

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
    LineChart<Number, Number> statsChart;

    @FXML
    LineChart<Number, Number> spectrumChart;

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

    private final Object lock = new Object();

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

        deviceParamsController.setAttenuation(null);
        deviceParamsController.setGain();
        deviceParamsController.setPts();

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

        if (!checkRadioProcessor()) return;

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


        try {
            int num_points = NumPointsResolver.getNumPoints(hahnEchoParameters.getSpectralWidth(), hahnEchoParameters.getNumberOfScans(), hahnEchoParameters.getAdcFrequency(), hahnEchoParameters.getEchoTime());
            double recStep = ((double)hahnEchoParameters.getSpectralWidth() / 100) / num_points;

            grid.add(new Label("PTS Freq. step multiplier:"), 0, 0);
            grid.add(new Label(recStep + ""), 1, 0);
        } catch (Exception e) {
            grid.add(new Label("ERROR: " + e.getMessage()), 0, 0);

            e.printStackTrace();
        }
        grid.add(new Label("Parameter:"), 0, 1);
        grid.add(parameters, 1, 1);
        grid.add(new Label("Step:"), 0, 2);
        grid.add(step, 1, 2);
        grid.add(new Label("Iterations:"), 0, 3);
        grid.add(iterations, 1, 3);

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

        statsChart.getXAxis().setAutoRanging(false);
        ((NumberAxis) statsChart.getXAxis()).setLowerBound(contExperiment.getParameter().getInitialValue());
        ((NumberAxis) statsChart.getXAxis()).setUpperBound(contExperiment.getParameter().getInitialValue() + contExperiment.getStep() * contExperiment.getIterations());
        ((NumberAxis) statsChart.getXAxis()).setTickUnit(contExperiment.getStep());

        // TODO check if HahnEcho or CPMG...
        contExperiment.init(hahnEchoParameters, selectedTab); // will set task and pulse
        contExperiment.setTask(new Task() {

            @Override
            protected Object call() throws Exception {
                while (contExperiment.getCurrentIteration() <= contExperiment.getIterations() + 1) {
                    try {
                        contExperiment.getRadioProcessor().start();
                        contExperiment.onScan.invoke(this, contExperiment.getRadioProcessor().getData());

                        contExperiment.incrementCurrentStep(); // TODO check if valid parameter value

                        if (contExperiment.getParameter().getId() == ContParameter.PTS_FREQ
                                && contExperiment.getCurrentIteration() <= contExperiment.getIterations() + 1
                                && SpinAPI.INSTANCE.pb_read_status() == 0x03) {
                            Platform.runLater(() -> {
                                synchronized (lock) {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Tuning");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Please tune the thing.");

                                    alert.showAndWait();

                                    lock.notify();
                                }
                            });

                            synchronized (lock) {
                                lock.wait();
                            }
                        }

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
        contExperiment.getRadioProcessor().getPulse().onComplete = (sender, value) -> Platform.runLater(() -> leftStatus.setText("Using " + contExperiment.getParameter().getName() + " value " + contExperiment.getCurrentParameterValue()));

        // prepare cont experiment plots
        statsPlot.setDisable(false);
        if (contExperiment.getParameter().getId() == ContParameter.PTS_FREQ) {
            spectrumChart.getData().clear();
            spectrumChart.setDisable(false);
            spectrumChart.getXAxis().setAutoRanging(false);
            modPlot.setDisable(false);

            double ptsFreq = contExperiment.getParameter().getInitialValue();
            double swMHZ = (double) hahnEchoParameters.getSpectralWidth() / 1000;
            ((NumberAxis) spectrumChart.getXAxis()).setLowerBound((ptsFreq - swMHZ / 2) * 1e6);
            ((NumberAxis) spectrumChart.getXAxis()).setUpperBound((ptsFreq + contExperiment.getIterations() * contExperiment.getStep() + swMHZ / 2) * 1e6);
            ((NumberAxis) spectrumChart.getXAxis()).setTickUnit(100000);

            ptsChartViewModel = new PTSChartViewModel(contExperiment, hahnEchoParameters, spectrumChart);

            contExperiment.onScan = spectrumChartUpdate();
        } else {
            contExperiment.onScan = createStatsChartUpdateEvent(statsChart);
        }

        deviceParamsController.setAttenuation(null);
        deviceParamsController.setGain();
        deviceParamsController.setPts();

        Thread t = new Thread(contExperiment.getTask());

        t.setDaemon(true);
        t.start();

        setRunningState();
    }

    private Invokable<Complex[]> spectrumChartUpdate() {
        return (sender, value) -> {
            System.out.println("Spectrum update running!");

            ptsChartViewModel.addData(((ContExperiment) experiment).getNextParameterValue(), value);

            Invokable<Complex[]> statsChartUpdate = createStatsChartUpdateEvent(statsChart);
            statsChartUpdate.invoke(sender, value);
        };
    }

    private Invokable<Void> createPulseParameterUpdateEvent(ContExperiment contExperiment) {
        switch (contExperiment.getParameter().getId()) {
            case ContParameter.AMP_GAIN:
                return (sender, value) -> {
                    int currentValue = Integer.parseInt(deviceParamsController.getAttTextField().getText());
                    int newValue = (int) (currentValue + contExperiment.getCurrentIteration() * contExperiment.getStep());

                    FTDI_Device.INSTANCE.device_set_attenuation(newValue);
                };
            case ContParameter.AMPLITUDE:
                if (contExperiment.getCurrentIteration() == contExperiment.getIterations()) return (sender, value) -> {
                };

                return (sender, value) -> {
                    if (sender instanceof HahnEcho) {
                        HahnEcho hahnEcho = (HahnEcho) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEcho.getParameters();

                        float newValue = (float) contExperiment.getNextParameterValue();
                        hahnEchoParameters.setAmplitude(newValue);
                    } else if (sender instanceof HahnEchoCYCLOPS) {
                        HahnEchoCYCLOPS hahnEchoCYCLOPS = (HahnEchoCYCLOPS) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEchoCYCLOPS.getParameters();

                        float newValue = (float) contExperiment.getNextParameterValue();
                        hahnEchoParameters.setAmplitude(newValue);
                    } else {
                        // TODO other pulse series..
                    }
                };
            case ContParameter.PTS_FREQ:
                return (sender, value) -> {
                    double newValue = contExperiment.getNextParameterValue();

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

                        double newValue = contExperiment.getNextParameterValue();
                        hahnEchoParameters.setRepetitionDelay(newValue);
                    } else if (sender instanceof HahnEchoCYCLOPS) {
                        HahnEchoCYCLOPS hahnEchoCYCLOPS = (HahnEchoCYCLOPS) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEchoCYCLOPS.getParameters();

                        double newValue = contExperiment.getNextParameterValue();
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

                        double newValue = contExperiment.getNextParameterValue();
                        hahnEchoParameters.setTau(newValue);
                    } else if (sender instanceof HahnEchoCYCLOPS) {
                        HahnEchoCYCLOPS hahnEchoCYCLOPS = (HahnEchoCYCLOPS) sender;
                        HahnEchoParameters hahnEchoParameters = hahnEchoCYCLOPS.getParameters();

                        double newValue = contExperiment.getNextParameterValue();
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
            plainTextData.toFile(hahnEchoParameters, file); // TODO change parameters accordingly
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
                leftStatus.setText("Current scan: " + value + ", " + contExperiment.getParameter().getName() + " value: " + (contExperiment.getNextParameterValue() - contExperiment.getStep()));
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
        workerStateEvent.getSource().getException().printStackTrace();
        leftStatus.setText("Error has occured during program execution: " + workerStateEvent.getSource().getException().getMessage());
    }

    /**
     * Factory for chart update invokable.
     *
     * @return chart update invokable
     */
    private Invokable<Complex[]> createDataChartUpdateEvent(LineChart<Number, Number> lineChart) {
        String[] parts = leftStatus.getText().replaceAll("\\s+", "").split("(,)|(:)");

        int num_scans = 1;
        if (parts.length >= 2) {
            num_scans = Integer.parseInt(parts[1]);
        }

        final int finalNum_scans1 = num_scans;
        return (sender, value) -> Platform.runLater(() -> {
            lineChart.getData().clear();

            final XYChart.Series<Number, Number> real = new XYChart.Series<>();
            final XYChart.Series<Number, Number> imag = new XYChart.Series<>();

            real.setName("Real");
            imag.setName("Imaginary");

            for (int i = 0, valueLength = value.length; i < valueLength; i++) {
                Complex c = value[i];

                real.getData().add(new XYChart.Data<>(experiment.getEchoTime() / valueLength * i, c.getReal() / finalNum_scans1));
                imag.getData().add(new XYChart.Data<>(experiment.getEchoTime() / valueLength * i, c.getImaginary() / finalNum_scans1));
            }

            lineChart.getData().add(real);
            lineChart.getData().add(imag);
        });
    }

    /**
     * Factory for chart update invokable.
     *
     * @return chart update invokable
     */
    private Invokable<Complex[]> createFFTChartUpdateEvent(LineChart<Number, Number> lineChart) {
        String[] parts = leftStatus.getText().replaceAll("\\s+", "").split("(,)|(:)");

        int num_scans = 1;
        if (parts.length >= 2) {
            num_scans = Integer.parseInt(parts[1]);
        }

        final int finalNum_scans1 = num_scans;
        return (sender, value) -> Platform.runLater(() -> {
            lineChart.getData().clear();

            final XYChart.Series<Number, Number> real = new XYChart.Series<>();
            final XYChart.Series<Number, Number> imag = new XYChart.Series<>();

            real.setName("Real");
            imag.setName("Imaginary");

            for (int i = 0, valueLength = value.length; i < valueLength; i++) {
                Complex c = value[i];

                real.getData().add(new XYChart.Data<>((experiment.getSpectralWidth() / (valueLength * 1000)) * i + deviceParamsController.getPTSFreq() - experiment.getSpectralWidth() / 2000, c.getReal() / finalNum_scans1));
                imag.getData().add(new XYChart.Data<>((experiment.getSpectralWidth() / (valueLength * 1000)) * i + deviceParamsController.getPTSFreq() - experiment.getSpectralWidth() / 2000, c.getImaginary() / finalNum_scans1));
            }

            lineChart.getData().add(real);
            lineChart.getData().add(imag);
        });
    }

    /**
     * @param lineChart
     * @return
     * @deprecated
     */
    private Invokable<Complex[]> createModChartUpdateEvent(LineChart<Number, Number> lineChart) {
        return (sender, value) -> {

            // compute FFT modulus
            ContExperiment contExperiment = (ContExperiment) experiment;
            Complex[] fftMod = FFT.modulFFT(contExperiment.getRadioProcessor().getPulse().getData());

            // plot data
            final XYChart.Series<Number, Number> dataSeries;
            if (lineChart.getData().size() == 0) { // no data series present
                dataSeries = new XYChart.Series<>();
                dataSeries.setName("Data");

                // TODO add data to chart
            } else {

            }
        };
    }

    private Invokable<Complex[]> createStatsChartUpdateEvent(LineChart<Number, Number> lineChart) {
        Platform.runLater(() -> lineChart.getXAxis().setLabel(((ContExperiment) experiment).getParameter().getName()));

        return (sender, value) -> Platform.runLater(() -> {
            ContExperiment contExperiment = (ContExperiment) experiment;
            Complex[] data = contExperiment.getRadioProcessor().getPulse().getData();

            Complex[] modulFFT = FFT.modulFFT(data);
            Complex[] modul = FFT.modul(data);

            double left = deviceParamsController.getPTSFreq() - experiment.getSpectrometerFrequency() - experiment.getSpectralWidth() / 2000;
            double right = deviceParamsController.getPTSFreq() - experiment.getSpectrometerFrequency() + experiment.getSpectralWidth() / 2000;

            int fftPointIndex = data.length / 2;

            double modulMax = FFT.dataMax(modul);
            double modulFFTMax = FFT.dataMax(modulFFT);
            double modulInt = FFT.dataIntegral(modul);
            double modulFFTInt = FFT.dataIntegral(modulFFT);
            double fftValue = modulFFT[fftPointIndex].getReal();

            final XYChart.Series<Number, Number> modulMaxSeries;
            final XYChart.Series<Number, Number> modulFFTMaxSeries;
            final XYChart.Series<Number, Number> modulIntSeries;
            final XYChart.Series<Number, Number> modulFFTIntSeries;

            String fftSeriesName = "FFT Mod at " + (deviceParamsController.getPTSFreq() - experiment.getSpectrometerFrequency()) + "MHz";
            double x = contExperiment.getParameter().getInitialValue() + contExperiment.getStep() * (contExperiment.getCurrentIteration() - 2);
            if (lineChart.getData().size() == 0) {
                modulMaxSeries = new XYChart.Series<>();
                modulFFTMaxSeries = new XYChart.Series<>();
                modulIntSeries = new XYChart.Series<>();
                modulFFTIntSeries = new XYChart.Series<>();

                modulMaxSeries.setName("Mod Max");
                modulFFTMaxSeries.setName("FFT Mod Max");
                modulIntSeries.setName("Mod Integral");
                modulFFTIntSeries.setName(fftSeriesName);

                modulMaxSeries.getData().add(new XYChart.Data<>(x, modulMax));
                modulFFTMaxSeries.getData().add(new XYChart.Data<>(x, modulFFTMax));
                modulIntSeries.getData().add(new XYChart.Data<>(x, modulInt));
                modulFFTIntSeries.getData().add(new XYChart.Data<>(x, fftValue));

                lineChart.getData().add(modulMaxSeries);
                lineChart.getData().add(modulFFTMaxSeries);
                lineChart.getData().add(modulIntSeries);
                lineChart.getData().add(modulFFTIntSeries);
            } else {
                for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                    String seriesName = series.getName();
                    if (seriesName.equals("Mod Max")) {
                        series.getData().add(new XYChart.Data<>(x, modulMax));
                    } else if (seriesName.equals("FFT Mod Max")) {
                        series.getData().add(new XYChart.Data<>(x, modulFFTMax));
                    } else if (seriesName.equals("Mod Integral")) {
                        series.getData().add(new XYChart.Data<>(x, modulInt));
                    } else if (seriesName.equals(fftSeriesName)) {
                        series.getData().add(new XYChart.Data<>(x, fftValue));
                    }
                }
            }
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

            dataChart.getXAxis().setAutoRanging(false);
            ((NumberAxis) dataChart.getXAxis()).setLowerBound(0);
            ((NumberAxis) dataChart.getXAxis()).setUpperBound(experiment.getEchoTime());

            Invokable<Complex[]> dataChartUpdate = createDataChartUpdateEvent(dataChart);
            dataChartUpdate.invoke(sender, value);

            double left = deviceParamsController.getPTSFreq() - (experiment.getSpectralWidth() / 2000);
            double right = deviceParamsController.getPTSFreq() + (experiment.getSpectralWidth() / 2000);

            fftChart.getXAxis().setAutoRanging(false);
            ((NumberAxis) fftChart.getXAxis()).setLowerBound(left);
            ((NumberAxis) fftChart.getXAxis()).setUpperBound(right);
            ((NumberAxis) fftChart.getXAxis()).setTickUnit(0.1);
            Invokable<Complex[]> fftChartUpdate = createFFTChartUpdateEvent(fftChart);

            Complex[] transformed = FFT.transform(value);
            Complex[] flippedFFT = FFT.fixFFTdata(transformed);
            fftChartUpdate.invoke(sender, flippedFFT);

            modulChart.getXAxis().setAutoRanging(false);
            ((NumberAxis) modulChart.getXAxis()).setLowerBound(left);
            ((NumberAxis) modulChart.getXAxis()).setUpperBound(right);
            ((NumberAxis) modulChart.getXAxis()).setTickUnit(0.1);
            Invokable<Complex[]> modulChartUpdate = createFFTChartUpdateEvent(modulChart);

            Complex[] modul = FFT.modulFFT(value);
            Complex[] flippedModul = FFT.fixFFTdata(modul);
            modulChartUpdate.invoke(sender, flippedModul);
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
