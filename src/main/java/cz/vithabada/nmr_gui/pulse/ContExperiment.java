package cz.vithabada.nmr_gui.pulse;

import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.libs.Invokable;
import cz.vithabada.nmr_gui.model.Experiment;
import javafx.concurrent.Task;
import org.apache.commons.javaflow.Continuation;
import org.apache.commons.math3.complex.Complex;

import java.util.List;

/**
 * Contains logic for continuous experiments.
 *
 * @author Vit Habada
 */
public class ContExperiment extends Experiment {

    /**
     * Invokes before scan starts.
     */
    public Invokable<Object> onScanStart;

    /**
     * Invokes when individual scan is finished.
     */
    public Invokable<Complex[]> onScan;

    /**
     * Invokes when entire experiment is successfully done.
     */
    public Invokable<Object> onScanComplete;

    /**
     * Invokes when any error occurs.
     */
    public Invokable<Void> onError;

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
     *
     */
    private int currentStep = 1;

    /**
     * Collected data from all scans.
     */
    private List<Complex[]> dataCollection;

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

    public int getCurrentStep() {
        return currentStep;
    }

    public void incrementCurrentStep() {
        currentStep++;
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

    public List<Complex[]> getDataCollection() {
        return dataCollection;
    }

    public double getParameterValue() {
        return parameter.getInitialValue() + step * currentStep;
    }

    /**
     * Starts the experiment.
     */
    public void start() {

        Task t = new Task() {

            @Override
            protected Object call() throws Exception {
                onScanStart.invoke(ContExperiment.this, null);

                radioProcessor.getPulse().onComplete = new Invokable<Complex[]>() {

                    @Override
                    public void invoke(Object sender, Complex[] value) {

                    }
                };

                radioProcessor.getPulse().start();

                onScanComplete.invoke(ContExperiment.this, null);

                return null;
            }
        };

        switch (parameter.getId()) {
            case ContParameter.AMP_GAIN:
                break;
            case ContParameter.AMPLITUDE:
                break;
            case ContParameter.PTS_FREQ:
                break;
            case ContParameter.REPETITION_DELAY:
                break;
            case ContParameter.TAU:
                break;
            default:
                try {
                    throw new Exception("Invalid experiment parameter");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

        Thread thread = new Thread(() -> {
            // TODO parameter loop
            task.run();
        });

        thread.setDaemon(true);
        thread.start();

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
