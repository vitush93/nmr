package cz.vithabada.nmr_gui.model;

import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.libs.FFT;
import cz.vithabada.nmr_gui.pulse.ContExperiment;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.complex.Complex;

import java.util.HashMap;

public class PTSChartViewModel {

    private double initialPTSFreq;
    private double delta;
    private double lowerPlotBound;
    private double upperPlotBound;
    private double spectralWidth;
    private double spectrometerFrequency;

    private HashMap<Double, Complex[]> dataSet = new HashMap<>();
    private LineChart<Number, Number> lineChart;
    private HahnEchoParameters hahnEchoParameters;
    private ContExperiment contExperiment;

    public PTSChartViewModel(ContExperiment contExperiment, HahnEchoParameters hahnEchoParameters, LineChart<Number, Number> chart) {
        this.lineChart = chart;
        this.contExperiment = contExperiment;
        this.hahnEchoParameters = hahnEchoParameters;
    }

    public void addData(double ptsFreq, Complex[] data) {
        Complex[] mod = FFT.modul(data);
        this.dataSet.put(ptsFreq, mod);

        final XYChart.Series<Number, Number> modul = new XYChart.Series<>();
        double[] pointFrequencies = new double[mod.length];
        double deltaF = this.hahnEchoParameters.getSpectralWidth() / this.hahnEchoParameters.getNumPoints();

        int initialIndex;
        if (mod.length % 2 == 0) {
            initialIndex = mod.length / 2;
        } else {
            initialIndex = (int) Math.floor(mod.length / 2);
        }

        pointFrequencies[initialIndex] = ptsFreq;

        int index = initialIndex - 1;
        while (index > 0) {
            pointFrequencies[index--] = ptsFreq - deltaF;
        }

        index = initialIndex + 1;
        while (index < mod.length) {
            pointFrequencies[index++] = ptsFreq + deltaF;
        }

        for (int i = 0; i < mod.length; i++) {
            modul.getData().add(new XYChart.Data<>(
                    pointFrequencies[i],
                    mod[i].getReal()
            ));
        }

        this.lineChart.getData().add(modul);
    }

}
