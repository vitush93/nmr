package cz.vithabada.nmr_gui.libs;


import cz.vithabada.nmr_gui.api.SpinAPI;

public class NumPointsResolver {

    private static SpinAPI api;

    static {
        api = SpinAPI.INSTANCE;
    }

    public static int getNumPoints(int SPECTRAL_WIDTH, int NUMBER_OF_SCANS, double ADC_FREQUENCY, double ECHO_TIME) throws Exception {

        api.pb_init();

        api.pb_set_debug(1);

        api.pb_set_defaults();
        api.pb_core_clock(ADC_FREQUENCY);
        api.pb_overflow(1, 0);
        api.pb_scan_count(1);

        int dec_amount = api.pb_setup_filters(SPECTRAL_WIDTH / 1000.0, NUMBER_OF_SCANS, SpinAPI.BYPASS_FIR);
        if (dec_amount < 0) {
            throw new Exception(api.pb_get_error());
        }

        double actualSpectralWidth = (ADC_FREQUENCY * 1.0e6) / (double) dec_amount;
        int num_points = (int) Math.floor(((ECHO_TIME) / 1e6) * actualSpectralWidth);

        api.pb_reset();

        return num_points;
    }

}
