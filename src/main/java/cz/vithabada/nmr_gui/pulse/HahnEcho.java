package cz.vithabada.nmr_gui.pulse;

import cz.vithabada.nmr_gui.forms.HahnEchoParameters;
import org.apache.commons.math3.complex.Complex;
import cz.vithabada.nmr_gui.api.SpinAPI;

/**
 * Hahn echo pulse series programming.
 * Configures the entire pulse series to the SpinCore RadioProcessor's registers and
 * controls the experiment.
 *
 * @author Vit Habada
 */
public class HahnEcho extends Pulse<Complex[]> {

    /**
     * SpinCore API reference.
     */
    private final SpinAPI api;

    /**
     * Collected data in current scan.
     */
    private Complex[] data;

    /**
     * Flag which indicates whether the experiment is running.
     */
    private boolean running = false;

    /**
     * Contains validated pulse parameters.
     */
    private HahnEchoParameters parameters;

    /**
     * @param hahnEchoParameters pulse parameters.
     */
    public HahnEcho(HahnEchoParameters hahnEchoParameters) {
        this.api = SpinAPI.INSTANCE;
        this.parameters = hahnEchoParameters;
    }

    /**
     * Programs and starts the experiment.
     */
    @Override
    public void start() {
        running = true;

        double ADC_FREQUENCY = parameters.getAdcFrequency();
        double SPECTROMETER_FREQUENCY = parameters.getSpectrometerFrequency();
        int SPECTRAL_WIDTH = parameters.getSpectralWidth();
        int NUMBER_OF_SCANS = parameters.getNumberOfScans();
        double ECHO_TIME = parameters.getEchoTime();
        double TAU = parameters.getTau();
        int P1_PHASE = parameters.getP1Phase();
        int P2_PHASE = parameters.getP2Phase();
        int P1_TIME = parameters.getP1Time();
        int P2_TIME = parameters.getP2Time();
        int BLANKING_BIT = parameters.getBlankingBit();
        double BLANKING_DELAY = parameters.getBlankingDelay();
        float AMPLITUDE = parameters.getAmplitude();
        double REPETITION_DELAY = parameters.getRepetitionDelay();

        System.out.println("SpinAPI version: " + api.pb_get_version());
        if (api.pb_count_boards() <= 0) {
            System.out.println("Board not connected!");

            return;
        }

        api.pb_init();

        api.pb_set_debug(1);

        api.pb_set_defaults();
        api.pb_core_clock(ADC_FREQUENCY);
        api.pb_overflow(1, 0);
        api.pb_scan_count(1);

        System.out.println("desired SW: " + SPECTRAL_WIDTH / 1000.0);

        int dec_amount = api.pb_setup_filters(SPECTRAL_WIDTH / 1000.0, NUMBER_OF_SCANS, SpinAPI.BYPASS_FIR);
        if (dec_amount < 0) {
            System.out.println("ERROR: " + api.pb_get_error());

            return;
        }

        System.out.println("Dec_amount: " + dec_amount);
        double actualSpectralWidth = (ADC_FREQUENCY * 1.0e6) / (double) dec_amount;
        System.out.println("Actual spectral width: " + actualSpectralWidth);

        double ringdown_time = TAU - 0.5 * ECHO_TIME;

        int num_points = (int) Math.floor(((ECHO_TIME) / 1e6) * actualSpectralWidth);

        synchronized (this) {
            this.data = new Complex[num_points];
        }

        api.pb_set_num_points(num_points);
        api.pb_set_scan_segments(1);

        int[] real = new int[num_points];
        int[] imag = new int[num_points];

        api.pb_start_programming(SpinAPI.FREQ_REGS);
        api.pb_set_freq(SPECTROMETER_FREQUENCY);
        api.pb_stop_programming();

        api.pb_start_programming(SpinAPI.COS_PHASE_REGS);
        api.pb_set_phase(0.0);
        api.pb_set_phase(90.0);
        api.pb_set_phase(180.0);
        api.pb_set_phase(270.0);
        api.pb_stop_programming();

        api.pb_start_programming(SpinAPI.SIN_PHASE_REGS);
        api.pb_set_phase(0.0);
        api.pb_set_phase(90.0);
        api.pb_set_phase(180.0);
        api.pb_set_phase(270.0);
        api.pb_stop_programming();

        api.pb_start_programming(SpinAPI.TX_PHASE_REGS);
        api.pb_set_phase(P1_PHASE);
        api.pb_set_phase(P2_PHASE);
        api.pb_stop_programming();

        api.pb_set_amp(AMPLITUDE, 0);

        api.pb_start_programming(SpinAPI.PULSE_PROGRAM);

        int scan_loop_label = api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, (1 << BLANKING_BIT), SpinAPI.LOOP, NUMBER_OF_SCANS, BLANKING_DELAY * 1000000.0);

        // 1st (90 degree) pulse
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_ENABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, (1 << BLANKING_BIT), SpinAPI.CONTINUE, 0, P1_TIME * 1000.0);

        // if do_90_trigger = 1: start scan and wait time tau
        // if do_90_trigger = 0: wait time tau
        //  JB 5/20/14 : power amplifier is kept off during acquisition of the 90 degree response to reduce noise in the signal
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, 0x00, SpinAPI.CONTINUE, 0, TAU * 1000.0);

        //  JB 5/20/14 : a delay of half the blanking delay is used to deblank the amplifier for the 180 degree pulse
        //  was set for a 10W PA, may need changing if using iSpin's with different amplifier
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, (1 << BLANKING_BIT), SpinAPI.CONTINUE, 0, BLANKING_DELAY * 1000000.0);

        // 2nd (180 degree) pulse 
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 1, SpinAPI.TX_ENABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, (1 << BLANKING_BIT), SpinAPI.CONTINUE, 0, P2_TIME * 1000.0);

        // and then wait time 2*(myScan->tau) before proceeding to the next instruction.
        // total duration is about 3*tau)
        // Nov 15, 2013---turn off amp for lower noise
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, 0x00, SpinAPI.CONTINUE, 0, ringdown_time * 1000.0);

        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.DO_TRIGGER, 0, 0, 0x00, SpinAPI.CONTINUE, 0, ECHO_TIME * 1000.0);

        // At about this time the num_points data points have been scanned, 
        // so the scanning stops.  
        // Allow sample to relax before acquiring another scan
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, 0x00, SpinAPI.CONTINUE, 0, REPETITION_DELAY * 1000.0 * 1000000.0);

        // Loop back and do scan again. This will occur num_scans times
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, 0x00, SpinAPI.END_LOOP, scan_loop_label, 1.0 * 1000.0);

        // Then stop the pulse program
        api.pb_inst_radio_shape(0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER, 0, 0, 0x00, SpinAPI.STOP, 0, 1.0 * 1000.0);

        api.pb_stop_programming();

        api.pb_reset();
        api.pb_start();

        while (api.pb_read_status() != 0x03) //Wait for the board to complete execution.
        {
            if (!running) {
                break;
            }

            api.pb_sleep_ms(1000);
            api.pb_get_data(num_points, real, imag);

            synchronized (this) {
                createData(real, imag);
                onFetch.invoke(this, data);
                onRefresh.invoke(this, api.pb_scan_count(0));
            }

            System.out.println("Current Scan: " + api.pb_scan_count(0));
        }

        synchronized (this) {
            onComplete.invoke(this, data);
            onRefresh.invoke(this, NUMBER_OF_SCANS);
            onDone.invoke(this, null);
        }
    }

    /**
     * Updates the data property with Complex data.
     *
     * @param real real part of the collected data.
     * @param imag imaginary part of the collected data.
     */
    private void createData(int[] real, int[] imag) {
        for (int i = 0; i < real.length; i++) {
            data[i] = new Complex(real[i], imag[i]);
        }
    }

    /**
     * Stops the experiment.
     */
    @Override
    public void stop() {
        running = false;

        api.pb_stop();

        // wait for RadioProcessor to stop pulse execution
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves content of the data property, even when the experiment is running.
     *
     * @return currently collected data.
     */
    @Override
    synchronized public Complex[] getData() {
        if (data == null) {
            return null;
        }

        Complex[] copy = new Complex[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);

        return copy;
    }

    public HahnEchoParameters getParameters() {
        return parameters;
    }
}
