package libs;


import jssc.SerialPort;
import jssc.SerialPortException;

public class RS232_Attenuator {

    SerialPort serialPort;

    boolean portOpened = false;

    public RS232_Attenuator(String port) {
        this.serialPort = new SerialPort(port);
    }

    void one() throws SerialPortException {
        serialPort.writeByte((byte) 0);
        spin();
        serialPort.setDTR(true);
        spin();
        serialPort.setDTR(false);
        spin();
    }

    void zero() throws SerialPortException {
        serialPort.setDTR(true);
        spin();
        serialPort.setDTR(false);
        spin();
    }

    byte createMask(boolean bw, boolean db40, byte gain) {
        byte mask = gain;

        if (bw) {
            mask |= (1 << 7);
        }

        if (db40) {
            mask |= (1 << 6);
        }

        return mask;
    }

    void spin() {
        for (int i = 0; i < 1000000; i++) {
        }
    }

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

}

