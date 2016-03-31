package cz.vithabada.nmr_gui.pulse;

import cz.vithabada.nmr_gui.MainController;

public class ContExperiment {
    ContParameter parameter;
    double step;
    int iterations;

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
}
