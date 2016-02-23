package cz.vithabada.nmr_gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import libs.AlertHelper;
import spinapi.SpinAPI;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class DeviceParamsController implements Initializable {

    @FXML
    Label gainLabel;

    @FXML
    Button gainButton;

    @FXML
    Label ptsLabel;

    @FXML
    Button ptsButton;

    @FXML
    CheckBox bwCheckbox;

    @FXML
    CheckBox db40Checkbox;

    @FXML
    TextField gainTextField;

    @FXML
    TextField attTextField;

    @FXML
    TextField ptsTextField;

    SerialPort serialPort;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // disable serial port settings if no serial port is present
        String[] ports = SerialPortList.getPortNames();
        if (ports.length == 0) {
            gainButton.setDisable(true);
            bwCheckbox.setDisable(true);
            db40Checkbox.setDisable(true);
            gainTextField.setDisable(true);
        } else {
            serialPort = new SerialPort("COM2"); // FIXME assumes COM2

            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
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

    void spin() {
        for (int i = 0; i < 1000000; i++) {
        }
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

    @FXML
    void setGain() throws SerialPortException {
        boolean set = false;

        byte gain = 0;
        try {
            gain = Byte.parseByte(gainTextField.getText());

            if (gain < 0 || gain > 63) {
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Invalid input", "Gain value must be between 0 and 63");

                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Invalid input", "Gain (dB): Please enter a valid number.");

            return;
        }

        try {
            byte mask = createMask(bwCheckbox.isSelected(), db40Checkbox.isSelected(), gain);

            for (int i = 7; i >= 0; i--) {
                if (((1 << i) & mask) != 0) {
                    one();
                } else {
                    zero();
                }
            }

            set = true;
        } catch (SerialPortException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }

        if (set) {
            setStatusLabel(gainLabel, DeviceStatus.OK);
        } else {
            setStatusLabel(gainLabel, DeviceStatus.FAIL);
        }
    }

    void setStatusLabel(Label label, DeviceStatus deviceStatus) {
        label.setText(deviceStatus.toString());
    }

    @FXML
    void setPts() {
        double maxFreq = 250;
        int is160 = 0;
        int is3200 = 0;
        int allowPhase = 0;
        int noPTS = 1;

        try {
            double freq = Double.parseDouble(ptsTextField.getText());

            if (SpinAPI.INSTANCE.set_pts(maxFreq, is160, is3200, allowPhase, noPTS, freq, 0) != 0) {
                setStatusLabel(ptsLabel, DeviceStatus.FAIL);
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", SpinAPI.INSTANCE.spinpts_get_error());
            } else {
                setStatusLabel(ptsLabel, DeviceStatus.OK);
            }
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Invalid input", "Please enter a valid number.");
        }
    }
}

enum DeviceStatus {
    OK,
    FAIL,
    Ready
}