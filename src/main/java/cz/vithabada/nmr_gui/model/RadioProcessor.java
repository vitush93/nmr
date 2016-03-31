package cz.vithabada.nmr_gui.model;


import cz.vithabada.nmr_gui.api.SpinAPI;
import cz.vithabada.nmr_gui.pulse.Pulse;
import org.apache.commons.math3.complex.Complex;

public class RadioProcessor {

    /**
     * Indicated whether the RadioProcessor is connected.
     */
    private boolean boardConnected = false;

    /**
     * Pulse to be executed. Allows running data capture to be stopped from any method in this class.
     */
    private Pulse<Complex[]> pulse;

    public RadioProcessor() {

    }

    public Pulse<Complex[]> getPulse() {
        return pulse;
    }

    public void setPulse(Pulse<Complex[]> pulse) throws Exception {
        this.pulse = pulse;
    }

    public Complex[] getData() {
        return pulse.getData();
    }

    public void stop() {
        pulse.stop();
    }

    public void start() {
        pulse.start();
    }

    public boolean isBoardConnected() {
        return boardConnected;
    }

    public void updateBoardStatus() {
        boardConnected = (SpinAPI.INSTANCE.pb_count_boards() > 0);
    }

}
