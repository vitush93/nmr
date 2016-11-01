package cz.vithabada.nmr_gui.model;

import com.sun.deploy.util.ArrayUtil;
import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.libs.FFT;
import cz.vithabada.nmr_gui.pulse.ContExperiment;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

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
    private XYChart.Series<Number, Number> spectrumSeries;

    private LinkedHashMap<Number, Number> spectrumMap = new LinkedHashMap<>();
    private HashSet<Number> keys = new HashSet<>();

    public PTSChartViewModel(ContExperiment contExperiment, HahnEchoParameters hahnEchoParameters, LineChart<Number, Number> chart) {
        this.lineChart = chart;
        this.contExperiment = contExperiment;
        this.hahnEchoParameters = hahnEchoParameters;

        this.spectrumSeries = new XYChart.Series<>();
        this.spectrumSeries.setName("Spectrum");

        Platform.runLater(() -> {
            chart.getData().add(spectrumSeries);

            spectrumSeries.nodeProperty().get().setStyle("-fx-stroke-width: 4px;");
        });
    }

    public void addData(double ptsFreq, Complex[] data) {
        Complex[] mod = FFT.modulFFT(data);
        mod = FFT.fixFFTdata(mod);

        this.dataSet.put(ptsFreq, mod);

        final XYChart.Series<Number, Number> modul = new XYChart.Series<>();
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);

        modul.setName(df.format(ptsFreq) + " MHz");
        int[] pointFrequencies = new int[mod.length];
        int deltaF = (this.hahnEchoParameters.getSpectralWidth() / this.hahnEchoParameters.getNumPoints()) * 1000;

        int initialIndex;
        if (mod.length % 2 == 0) {
            initialIndex = mod.length / 2;
        } else {
            initialIndex = (int) Math.floor(mod.length / 2);
        }

        int ptsFreqI = (int)(ptsFreq * 1e6);

        pointFrequencies[initialIndex] = ptsFreqI;

        int index = initialIndex - 1;
        for (int i = 1; i < mod.length; i++) {
            if (index < 0) break;

            pointFrequencies[index--] = ptsFreqI - (i * deltaF);
        }

        index = initialIndex + 1;
        for (int i = 1; i < mod.length; i++) {
            if (index >= mod.length) break;

            pointFrequencies[index++] = ptsFreqI + (i * deltaF);
        }

        for (int i = 0; i < mod.length; i++) {
            keys.add(pointFrequencies[i]);

            modul.getData().add(new XYChart.Data<>(
                    pointFrequencies[i],
                    mod[i].getReal()
            ));
        }

        Platform.runLater(() -> {
            this.lineChart.getData().add(modul);

            spectrumMap.clear();
            for (XYChart.Series<Number, Number> series : lineChart.getData()) {
                if (series == spectrumSeries) continue;

                for (XYChart.Data<Number, Number> item : series.getData()) {
                    Number key = item.getXValue();
                    Number value = item.getYValue();

                    if (spectrumMap.containsKey(key)) {
                        int compare = new BigDecimal(
                                spectrumMap.get(key).toString())
                                .compareTo(new BigDecimal(
                                                value.toString()
                                        )
                                );

                        if (compare < 0) {
                            spectrumMap.put(key, value);
                        }
                    } else {
                        spectrumMap.put(key, value);
                    }
                }
            }

            spectrumSeries.getData().clear();

            for (Number key : spectrumMap.keySet()) {
                spectrumSeries.getData().add(new XYChart.Data<>(
                        key,
                        spectrumMap.get(key)
                ));
            }

            spectrumSeries.getNode().toFront();
        });
    }

}
