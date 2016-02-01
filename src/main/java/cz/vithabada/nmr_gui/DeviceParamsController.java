package cz.vithabada.nmr_gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import libs.AlertHelper;
import spinapi.SpinAPI;

import java.io.PrintWriter;
import java.io.StringWriter;
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
    TextField bwTextField;

    @FXML
    TextField gainTextField;

    @FXML
    TextField attTextField;

    @FXML
    TextField ptsTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void setGain() {
        String[] portNames = SerialPortList.getPortNames();

        if (portNames.length == 0) {
            setStatusLabel(gainLabel, DeviceStatus.FAIL);
            AlertHelper.showAlert(Alert.AlertType.WARNING, "No serial port detected", "No serial ports detected on this system.");

            return;
        }

        boolean set = false;

        // send gain data to serial ports
        for (String port : portNames) {
            SerialPort serialPort = new SerialPort(port);

            try {
                serialPort.openPort();

                // TODO send data to serial port
                set = true;
            } catch (SerialPortException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", sw.toString());
            }
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