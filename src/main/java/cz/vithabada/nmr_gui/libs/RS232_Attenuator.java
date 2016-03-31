package cz.vithabada.nmr_gui.libs;


import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Handles configuration of custom-made attenuator connected via Serial Port.
 *
 * @author Vit Habada
 */
public class RS232_Attenuator {

    /**
     * Serial port instance.
     * Handles the serial port communication.
     */
    private SerialPort serialPort;

    /**
     * Flag that indicates whether the port has already been opened.
     * Prevents attenuator from going crazy from repeated port opening.
     */
    private boolean portOpened = false;

    /**
     *
     * @param port name of the serial port, ex. COM1
     */
    public RS232_Attenuator(String port) {
        this.serialPort = new SerialPort(port);
    }

    /**
     * Sends 1 to the attenuator's register.
     *
     * @throws SerialPortException
     */
    private void one() throws SerialPortException {
        serialPort.writeByte((byte) 0);
        spin();
        serialPort.setDTR(true);
        spin();
        serialPort.setDTR(false);
        spin();
    }

    /**
     * Sends 0 to the attenuator's register.
     *
     * @throws SerialPortException
     */
    private void zero() throws SerialPortException {
        serialPort.setDTR(true);
        spin();
        serialPort.setDTR(false);
        spin();
    }

    /**
     * Constructs binary number to be transfered to the attenuator's register.
     *
     * @param bw Band Width (MHz).
     * @param db40 add 40dB.
     * @param gain actual attenuator gain.
     * @return constructed bitmask.
     */
    private byte createMask(boolean bw, boolean db40, byte gain) {
        byte mask = gain;

        if (bw) {
            mask |= (1 << 7);
        }

        if (db40) {
            mask |= (1 << 6);
        }

        return mask;
    }

    /**
     * For active waiting between transfers to the serial port.
     */
    private void spin() {
        for (int i = 0; i < 1000000; i++) {
        }
    }

    /**
     * Configures the attenuator.
     *
     * @param gain attenuation (dB).
     * @param bw Band Width (MHz).
     * @param db40 Add 40dB to the attenuation.
     * @throws RS232_AttenuatorException
     * @throws SerialPortException
     */
    public void setGain(byte gain, boolean bw, boolean db40) throws RS232_AttenuatorException, SerialPortException {
        if (gain < 0 || gain > 63) {
            throw new RS232_AttenuatorException("Gain value must be between 0 and 63");
        }

        if (!portOpened) {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            portOpened = true;
        }

        byte mask = createMask(bw, db40, gain);

        for (int i = 7; i >= 0; i--) {
            if (((1 << i) & mask) != 0) {
                one();
            } else {
                zero();
            }
        }
    }

    /**
     * Attenuator's exception class.
     *
     * @author Vit Habada
     */
    public static class RS232_AttenuatorException extends Exception {
        RS232_AttenuatorException(String message) {
            super(message);
        }
    }
}

