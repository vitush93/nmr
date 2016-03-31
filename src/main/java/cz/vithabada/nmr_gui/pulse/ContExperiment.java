package cz.vithabada.nmr_gui.pulse;

import cz.vithabada.nmr_gui.forms.Parameters;
import cz.vithabada.nmr_gui.libs.Invokable;
import cz.vithabada.nmr_gui.model.Experiment;
import org.apache.commons.math3.complex.Complex;

public class ContExperiment {
    private ContParameter parameter;
    private Experiment experiment;
    private double step;
    private int iterations;

    Invokable<Complex[]> onScan;
    Invokable<Object> onScanComplete;
    Invokable<Void> onError;

    public ContExperiment(ContParameter p, double s, int i) {
        this.parameter = p;
        this.step = s;
        this.iterations = i;
    }

    public ContParameter getParameter() {
        return parameter;
    }

    public void setParameter(ContParameter parameter) {
        this.parameter = parameter;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void init(Parameters parameters, Experiment.Pulse pulseEnum) throws Exception {
        experiment.init(parameters, pulseEnum);
    }

    public void start() {
        // TODO start continuous experiment
    }
}
