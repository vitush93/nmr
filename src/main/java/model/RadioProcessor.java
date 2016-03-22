package model;


import api.SpinAPI;
import cz.vithabada.nmr_gui.pulse.Pulse;
import org.apache.commons.math3.complex.Complex;

public class RadioProcessor {

    private boolean boardConnected = false;

    private boolean running = false;

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
