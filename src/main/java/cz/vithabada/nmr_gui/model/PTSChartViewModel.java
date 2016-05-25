package cz.vithabada.nmr_gui.model;

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

    public void addData(double ptsFreq, Complex[] data) {
        this.dataSet.put(ptsFreq, data);
    }

}
