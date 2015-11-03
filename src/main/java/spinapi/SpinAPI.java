package spinapi;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface SpinAPI extends Library {

    static final int FREQ_REGS = 1;
    static final int COS_PHASE_REGS = 51;
    static final int SIN_PHASE_REGS = 50;
    static final int TX_PHASE_REGS = 2;
    static final int PULSE_PROGRAM = 0;
    static final int PHASE000 = 0;
    static final int PHASE090 = 1;
    static final int PHASE180 = 2;
    static final int PHASE270 = 3;
    static final int TX_ENABLE = 1;
    static final int TX_DISABLE = 0;
    static final int PHASE_RESET = 1;
    static final int NO_TRIGGER = 0;
    static final int LOOP = 2;
    static final int NO_PHASE_RESET = 0;
    static final int CONTINUE = 0;
    static final int STOP = 1;
    static final int DO_TRIGGER = 1;
    static final int END_LOOP = 3;
    static final int BYPASS_FIR = 0x0100;

    SpinAPI INSTANCE = (SpinAPI) Native.loadLibrary((System.getProperty("sun.arch.data.model").contains("64")) ? "spinapi64.dll" : "spinapi.dll", SpinAPI.class);

    String pb_get_version();

    String pb_get_error();

    int pb_count_boards();

    int pb_set_defaults();

    void pb_core_clock(double clock_freq);

    int pb_overflow(int reset, int of);

    int pb_scan_count(int reset);

    int pb_setup_filters(double spectral_width, int scan_repetitions, int cmd);

    int pb_set_num_points(int num_points);

    int pb_set_scan_segments(int num_segments);

    int pb_start_programming(int device);

    int pb_set_freq(double freq);

    int pb_stop_programming();

    int pb_set_phase(double phase);

    int pb_set_amp(float amp, int addr);

    int pb_inst_radio_shape(int freq,
            int cos_phase,
            int sin_phase,
            int tx_phase,
            int tx_enable,
            int phase_reset,
            int trigger_scan,
            int use_shape,
            int amp,
            int flags,
            int inst,
            int inst_data,
            double length);

    int pb_reset();

    int pb_stop();

    int pb_start();

    int pb_read_status();

    void pb_sleep_ms(int miliseconds);

    int pb_get_data(int num_points, int[] real_data, int[] imag_data);
    
    void pb_set_debug(int debug);
    
    int pb_init();
}
