package cz.vithabada.nmr_gui.libs;

import cz.vithabada.nmr_gui.api.SpinAPI;

public class PTS {

    public static void setFrequency(double freq) throws PTSException {
        double maxFreq = 250;
        int is160 = 0;
        int is3200 = 0;
        int allowPhase = 0;
        int noPTS = 1;

        if (SpinAPI.INSTANCE.set_pts(maxFreq, is160, is3200, allowPhase, noPTS, freq, 0) != 0) {
            throw new PTSException();
        }
    }

    public static class PTSException extends Exception {
    }

}
