package cz.vithabada.nmr_gui.pulse;

import cz.vithabada.nmr_gui.libs.Invokable;
import cz.vithabada.nmr_gui.model.Experiment;
import org.apache.commons.math3.complex.Complex;

/**
 * Contains logic for continuous experiments.
 *
 * @author Vit Habada
 */
public class ContExperiment extends Experiment {

    /**
     * Selected continuous experiment parameter.
     */
    private ContParameter parameter;

    /**
     * Step size for the experiment.
     */
    private double step;

    /**
     * Number of iterations for the experiment.
     * After each iteration, selected parameter is increased by specified step.
     */
    private int iterations;

    /**
     * Invokes when individual scan is finished.
     */
    Invokable<Complex[]> onScan;

    /**
     * Invokes when entire experiment is successfully done.
     */
    Invokable<Object> onScanComplete;

    /**
     * Invokes when any error occurs.
     */
    Invokable<Void> onError;

    /**
     * @param p selected experiment parameter.
     * @param s step size.
     * @param i number of iterations.
     */
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

    /**
     * Starts the experiment.
     */
    public void start() {
        /* TODO
        main thread -> {
            experiment thread (change parameter in loop) -> {
                single scan thread -> {
                    onScan -> collect scan data, update graphs
                }
            }
        }
         */
    }
}
