package model;

import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import cz.vithabada.nmr_gui.forms.Parameters;
import cz.vithabada.nmr_gui.pulse.HahnEcho;
import cz.vithabada.nmr_gui.pulse.HahnEchoCYCLOPS;
import javafx.concurrent.Task;
import org.apache.commons.math3.complex.Complex;

public class Experiment {

    private RadioProcessor radioProcessor;

    private Task task;

    public Experiment() {
        this.radioProcessor = new RadioProcessor();
    }

    public void init(Parameters parameters, Pulse pulseEnum) throws Exception {
        cz.vithabada.nmr_gui.pulse.Pulse<Complex[]> pulse = null;

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
        task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                radioProcessor.start();

                return null;
            }
        };
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

    public enum Pulse {
        HAHN_ECHO,
        CPMG
    }

}
