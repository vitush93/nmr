package cz.vithabada.nmr_gui.forms;

import javafx.beans.property.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class HahnEchoParameters extends Parameters {

    DoubleProperty adcFrequency = new SimpleDoubleProperty(75);
    DoubleProperty spectrometerFrequency = new SimpleDoubleProperty(2);
    IntegerProperty spectralWidth = new SimpleIntegerProperty(642);
    IntegerProperty numberOfScans = new SimpleIntegerProperty(100);
    DoubleProperty echoTime = new SimpleDoubleProperty(300);
    DoubleProperty tau = new SimpleDoubleProperty(200);
    IntegerProperty p1Phase = new SimpleIntegerProperty(0);
    IntegerProperty p2Phase = new SimpleIntegerProperty(0);
    IntegerProperty p1Time = new SimpleIntegerProperty(5);
    IntegerProperty p2Time = new SimpleIntegerProperty(10);
    IntegerProperty blankingBit = new SimpleIntegerProperty(2);
    DoubleProperty blankingDelay = new SimpleDoubleProperty(0.002);
    FloatProperty amplitude = new SimpleFloatProperty(0.3f);
    DoubleProperty repetitionDelay = new SimpleDoubleProperty(0.1);

    @Min(value = 0)
    @Max(value = 100)
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

    @Max(value = 10000)
    public int getSpectralWidth() {
        return spectralWidth.get();
    }

    public IntegerProperty spectralWidthProperty() {
        return spectralWidth;
    }

    public void setSpectralWidth(int spectralWidth) {
        this.spectralWidth.set(spectralWidth);
    }

    @Min(value = 1)
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
