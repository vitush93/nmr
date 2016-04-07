package cz.vithabada.nmr_gui.model;

import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.forms.Parameters;
import cz.vithabada.nmr_gui.pulse.HahnEcho;
import cz.vithabada.nmr_gui.pulse.HahnEchoCYCLOPS;
import javafx.concurrent.Task;
import org.apache.commons.math3.complex.Complex;

/**
 * Contains experiment management logic.
 *
 * @author Vit Habada
 */
public class Experiment {

    /**
     * RadioProcessor configurator.
     */
    protected RadioProcessor radioProcessor;

    /**
     * Task for the experiment run.
     */
    protected Task task;

    public Experiment() {
        this.radioProcessor = new RadioProcessor();
    }

    /**
     * Initiates experiment with given parameters.
     *
     * @param parameters pulse parameters.
     * @param pulseEnum pulse type.
     * @throws Exception
     */
    public void init(Parameters parameters, Pulse pulseEnum) throws Exception {
        cz.vithabada.nmr_gui.pulse.Pulse<Complex[]> pulse = null;

        // create pulse from Parameters
        if (pulseEnum == Pulse.HAHN_ECHO) {
            HahnEchoParameters hahnEchoParameters = (HahnEchoParameters) parameters;

            if (hahnEchoParameters.getCyclops()) {
                pulse = new HahnEchoCYCLOPS(hahnEchoParameters);
            } else {
                pulse = new HahnEcho(hahnEchoParameters);
            }
        } else if (pulseEnum == Pulse.CPMG) {
            // TODO
        }

        // set RadioProcessor pulse
        radioProcessor.setPulse(pulse);

        // initialize pulse task
        setTask(new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                radioProcessor.start();

                return null;
            }
        });
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getTask() throws Exception {
        if (task == null) {
            throw new Exception("Pulse task not initialized.");
        }

        return task;
    }

    public RadioProcessor getRadioProcessor() {
        return radioProcessor;
    }

    /**
     * Determines the pulse series type.
     */
    public enum Pulse {
        HAHN_ECHO,
        CPMG
    }

}
