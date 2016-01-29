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

import java.net.URL;
import java.util.ResourceBundle;

public class DeviceParamsController implements Initializable {

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
        for (String s : portNames) {
            System.out.println(s); // TODO write data to correct serial port
        }

        SerialPort serialPort = new SerialPort("COM1");
        try {
            serialPort.openPort();
            String data = serialPort.readString();
            System.out.println(data);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
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
                ptsLabel.setText("Fail");
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", SpinAPI.INSTANCE.spinpts_get_error());
            } else {
                ptsLabel.setText("OK");
            }
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Invalid input", "Please enter a valid number.");
        }
    }
}
