package cz.vithabada.nmr_gui;

import cz.vithabada.nmr_gui.pulse.Pulse;
import cz.vithabada.nmr_gui.pulse.RandomDataSource;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import libs.Complex;
import libs.Invokable;

public class MainController implements Initializable {

    @FXML
    LineChart<String, Number> lineChart;

    @FXML
    Button startButton;

    @FXML
    NumberAxis xAxis;

    @FXML
    NumberAxis yAxis;

    boolean started = false;

    Pulse source;

    @FXML
    private void handleStart(ActionEvent event) {
        if (started) {
            startButton.setText("Start");
            source.stop();

            started = false;

            return;
        }

        started = true;
        startButton.setText("Stop");

        Task task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                source.start();

                return null;
            }
        };

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Invokable<Complex[]> event = createChartUpdateEvent();

        source = new RandomDataSource(10);
        source.onFetch.add(event);
    }

    /**
     * Event that updates chart with a new data.
     *
     * @return Invokable
     */
    private Invokable<Complex[]> createChartUpdateEvent() {
        return new Invokable<Complex[]>() {

            @Override
            public void invoke(Object sender, Complex[] value) {
                lineChart.getData().clear();

                final XYChart.Series real = new XYChart.Series();
                real.setName("Real");

                final XYChart.Series imag = new XYChart.Series();
                imag.setName("Imaginary");

                int count = 0;
                for (Complex c : value) {
                    real.getData().add(new XYChart.Data(count, c.getReal()));
                    imag.getData().add(new XYChart.Data(count, c.getImag()));

                    count++;
                }

                lineChart.getData().add(real);
                lineChart.getData().add(imag);
            }
        };
    }
}
