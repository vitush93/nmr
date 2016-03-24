package model;


import api.SpinAPI;
import cz.vithabada.nmr_gui.pulse.Pulse;
import org.apache.commons.math3.complex.Complex;

public class RadioProcessor {

    /**
     * Indicated whether the RadioProcessor is connected.
     */
    private boolean boardConnected = false;

    /**
     * Indicates whether the data capture is running.
     */
    private boolean running = false;

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
        if(running) {
            throw new Exception("Cannot set pulse while scan is running.");
        }

        this.pulse = pulse;
    }

    public Complex[] getData() {
        return pulse.getData();
    }

    public void stop() {
        pulse.stop();

        running = false;
    }

    public void start() {
        pulse.start();

        running = true;
    }

    public boolean isBoardConnected() {
        return boardConnected;
    }

    public void updateBoardStatus() {
        boardConnected = (SpinAPI.INSTANCE.pb_count_boards() > 0);
    }

    public boolean isRunning() {
        return running;
    }

}
