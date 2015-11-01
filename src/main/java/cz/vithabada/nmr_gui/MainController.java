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
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import libs.Complex;
import libs.Invokable;

public class MainController implements Initializable {

    @FXML
    LineChart<Number, Number> lineChart;

    @FXML
    Button startButton;

    boolean started = false;
    Pulse source;
    final XYChart.Series<Number, Number> real = new XYChart.Series();
    final XYChart.Series<Number, Number> imag = new XYChart.Series();

    @FXML
    void handleStart(ActionEvent event) {
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

        source = new RandomDataSource(128);
        source.onFetch.add(event);

        real.setName("Real");
        imag.setName("Imaginary");
    }

    /**
     * Event that updates chart with a new data.
     *
     * @return Invokable
     */
    Invokable<Complex[]> createChartUpdateEvent() {
        return new Invokable<Complex[]>() {

            @Override
            public void invoke(Object sender, Complex[] value) {
                real.getData().clear();
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
        };
    }
}
