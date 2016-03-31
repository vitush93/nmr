package cz.vithabada.nmr_gui.libs;

import cz.vithabada.nmr_gui.api.SpinAPI;

/**
 * Provides cleaner interface for USB-PTS attenuator configuration (uses SpinAPI).
 *
 * @author Vit Habada
 */
public class PTS {

    /**
     * Sets attenuation frequency to the given value.
     *
     * @param freq desired frequency.
     * @throws PTSException if SpinAPI returned error.
     */
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

    /**
     * @author Vit Habada
     */
    public static class PTSException extends Exception {
    }

}
