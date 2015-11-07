package cz.vithabada.nmr_gui.forms;

import javafx.beans.property.*;

public class HahnEchoParameters {

    DoubleProperty adcFrequency = new SimpleDoubleProperty();
    DoubleProperty spectrometerFrequency = new SimpleDoubleProperty();
    IntegerProperty spectralWidth = new SimpleIntegerProperty();
    IntegerProperty numberOfScans = new SimpleIntegerProperty();
    DoubleProperty echoTime = new SimpleDoubleProperty();
    DoubleProperty tau = new SimpleDoubleProperty();
    IntegerProperty p1Phase = new SimpleIntegerProperty();
    IntegerProperty p2Phase = new SimpleIntegerProperty();
    IntegerProperty p1Time = new SimpleIntegerProperty();
    IntegerProperty p2Time = new SimpleIntegerProperty();
    IntegerProperty blankingBit = new SimpleIntegerProperty();
    DoubleProperty blankingDelay = new SimpleDoubleProperty();
    FloatProperty amplitude = new SimpleFloatProperty();
    DoubleProperty repetitionDelay = new SimpleDoubleProperty();

    public double getAdcFrequency() {
        return adcFrequency.get();
    }

    public DoubleProperty adcFrequencyProperty() {
        return adcFrequency;
    }

    public void setAdcFrequency(double adcFrequency) {
        this.adcFrequency.set(adcFrequency);
    }

    public double getSpectrometerFrequency() {
        return spectrometerFrequency.get();
    }

    public DoubleProperty spectrometerFrequencyProperty() {
        return spectrometerFrequency;
    }

    public void setSpectrometerFrequency(double spectrometerFrequency) {
        this.spectrometerFrequency.set(spectrometerFrequency);
    }

    public int getSpectralWidth() {
        return spectralWidth.get();
    }

    public IntegerProperty spectralWidthProperty() {
        return spectralWidth;
    }

    public void setSpectralWidth(int spectralWidth) {
        this.spectralWidth.set(spectralWidth);
    }

    public int getNumberOfScans() {
        return numberOfScans.get();
    }

    public IntegerProperty numberOfScansProperty() {
        return numberOfScans;
    }

    public void setNumberOfScans(int numberOfScans) {
        this.numberOfScans.set(numberOfScans);
    }

    public double getEchoTime() {
        return echoTime.get();
    }

    public DoubleProperty echoTimeProperty() {
        return echoTime;
    }

    public void setEchoTime(double echoTime) {
        this.echoTime.set(echoTime);
    }

    public double getTau() {
        return tau.get();
    }

    public DoubleProperty tauProperty() {
        return tau;
    }

    public void setTau(double tau) {
        this.tau.set(tau);
    }

    public int getP1Phase() {
        return p1Phase.get();
    }

    public IntegerProperty p1PhaseProperty() {
        return p1Phase;
    }

    public void setP1Phase(int p1Phase) {
        this.p1Phase.set(p1Phase);
    }

    public int getP2Phase() {
        return p2Phase.get();
    }

    public IntegerProperty p2PhaseProperty() {
        return p2Phase;
    }

    public void setP2Phase(int p2Phase) {
        this.p2Phase.set(p2Phase);
    }

    public int getP1Time() {
        return p1Time.get();
    }

    public IntegerProperty p1TimeProperty() {
        return p1Time;
    }

    public void setP1Time(int p1Time) {
        this.p1Time.set(p1Time);
    }

    public int getP2Time() {
        return p2Time.get();
    }

    public IntegerProperty p2TimeProperty() {
        return p2Time;
    }

    public void setP2Time(int p2Time) {
        this.p2Time.set(p2Time);
    }

    public int getBlankingBit() {
        return blankingBit.get();
    }

    public IntegerProperty blankingBitProperty() {
        return blankingBit;
    }

    public void setBlankingBit(int blankingBit) {
        this.blankingBit.set(blankingBit);
    }

    public double getBlankingDelay() {
        return blankingDelay.get();
    }

    public DoubleProperty blankingDelayProperty() {
        return blankingDelay;
    }

    public void setBlankingDelay(double blankingDelay) {
        this.blankingDelay.set(blankingDelay);
    }

    public float getAmplitude() {
        return amplitude.get();
    }

    public FloatProperty amplitudeProperty() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude.set(amplitude);
    }

    public double getRepetitionDelay() {
        return repetitionDelay.get();
    }

    public DoubleProperty repetitionDelayProperty() {
        return repetitionDelay;
    }

    public void setRepetitionDelay(double repetitionDelay) {
        this.repetitionDelay.set(repetitionDelay);
    }
}
