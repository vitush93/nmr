/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vithabada.nmr_gui.pulse;

import spinapi.SpinAPI;

/**
 *
 * @author vitush
 */
public class HahnEcho {

    private static SpinAPI api;

    public static void start() {
        double ADC_FREQUENCY = 75.0;
        double SPECTROMETER_FREQUENCY = 2;
        int SPECTRAL_WIDTH = 642;
        int NUMBER_OF_SCANS = 100;
        int BYPASS_FIR = 1;
        double ECHO_TIME = 300;
        double TAU = 200;
        int P1_PHASE = 0;
        int P2_PHASE = 0;
        int P1_TIME = 51;
        int P2_TIME = 101;
        int BLANKING_BIT = 2;
        int BLANKING_DELAY = 3;
        float AMPLITUDE = 0.3f;
        double REPETITION_DELAY=1;

        api = SpinAPI.INSTANCE;

        System.out.println("SpinAPI version: " + api.pb_get_version());
        if (api.pb_count_boards() <= 0) {
            System.out.println("Board not connected!");

            return;
        }

        api.pb_set_defaults();
        api.pb_core_clock(ADC_FREQUENCY);
        api.pb_overflow(1, 0);
        api.pb_scan_count(1);

        int dec_amount = api.pb_setup_filters(SPECTRAL_WIDTH / 1000.0, NUMBER_OF_SCANS, BYPASS_FIR);
        double actualSpectralWidth = (ADC_FREQUENCY * 1.0e6) / (double) dec_amount;
        System.out.println("Actual spectral width: " + actualSpectralWidth);

        double scan_time = ECHO_TIME;
        double ringdown_time = TAU - 0.5 * ECHO_TIME;

        int num_points = (int) Math.floor(((scan_time) / 1e6) * actualSpectralWidth);

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
        api.pb_inst_radio_shape (0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER,0,0, 0x00, SpinAPI.CONTINUE, 0, REPETITION_DELAY * 1000.0 * 1000000.0);
        
        // Loop back and do scan again. This will occur num_scans times
        api.pb_inst_radio_shape (0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.PHASE_RESET, SpinAPI.NO_TRIGGER,0,0, 0x00, SpinAPI.END_LOOP, scan_loop_label, 1.0 * 1000.0);
        
        // Then stop the pulse program
        api.pb_inst_radio_shape (0, SpinAPI.PHASE090, SpinAPI.PHASE000, 0, SpinAPI.TX_DISABLE, SpinAPI.NO_PHASE_RESET, SpinAPI.NO_TRIGGER,0,0, 0x00, SpinAPI.STOP, 0, 1.0 * 1000.0);
        
        api.pb_stop_programming ();
        
        api.pb_reset();
        api.pb_start();
        
        
        int scan_count = 0; // Scan count is not deterministic. Scans may be skipped or repeated

      while (api.pb_read_status () != 0x03)    //Wait for the board to complete execution.
      {      
          while( (api.pb_scan_count(0) <= scan_count)&& (api.pb_read_status () != 0x03) )
          {
              api.pb_sleep_ms(100);
          }

          if (api.pb_read_status () != 0x03) 
          {
              System.out.println("Current Scan: " + api.pb_scan_count(0));
          }
          
          scan_count++;
      }
      
      
      api.pb_get_data (num_points, real, imag);
      
      for(int i = 0; i < real.length; i++) {
          System.out.println("(" + real[i] + "," + imag[i] + ")");
      }
    }
}