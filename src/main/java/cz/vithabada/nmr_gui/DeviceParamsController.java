package cz.vithabada.nmr_gui;

import cz.vithabada.nmr_gui.libs.PTS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import jssc.SerialPortException;
import jssc.SerialPortList;
import cz.vithabada.nmr_gui.libs.AlertHelper;
import cz.vithabada.nmr_gui.libs.RS232_Attenuator;
import cz.vithabada.nmr_gui.api.FTDI_Device;
import cz.vithabada.nmr_gui.api.SpinAPI;

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
    Label attLabel;

    @FXML
    Button attGain;

    @FXML
    TextField attTextField;

    @FXML
    TextField ptsTextField;

    RS232_Attenuator rs232Attenuator;

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
            rs232Attenuator = new RS232_Attenuator("COM2");
        }
    }

    @FXML
    void setGain() throws SerialPortException {
        try {
            byte gain = Byte.parseByte(gainTextField.getText());

            rs232Attenuator.setGain(gain, bwCheckbox.isSelected(), db40Checkbox.isSelected());
            rs232Attenuator.setGain(gain, bwCheckbox.isSelected(), db40Checkbox.isSelected());

            setStatusLabel(gainLabel, DeviceStatus.OK);
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Invalid input", "Gain (dB): Please enter a valid number.");
        } catch (RS232_Attenuator.RS232_AttenuatorException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Invalid input", e.getMessage());
        } catch (SerialPortException e) {
            AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    void setStatusLabel(Label label, DeviceStatus deviceStatus) {
        label.setText(deviceStatus.toString());
    }

    @FXML
    void setPts() {
        try {
            double freq = getPTSFreq();

            try {
                PTS.setFrequency(freq);

                setStatusLabel(ptsLabel, DeviceStatus.OK);
            } catch (PTS.PTSException e) {
                setStatusLabel(ptsLabel, DeviceStatus.FAIL);
                AlertHelper.showAlert(Alert.AlertType.ERROR, "Error", SpinAPI.INSTANCE.spinpts_get_error());
            }
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Invalid input", "Please enter a valid number.");
        }
    }

    @FXML
    void setAttenuation(ActionEvent actionEvent) {
        try {
            int attenuation = Integer.parseInt(attTextField.getText());
            if (attenuation < 0 || attenuation > 31) {
                AlertHelper.showAlert(Alert.AlertType.WARNING, "Invalid input", "Attenuation must be between 0 and 31.");

                return;
            }

            String deviceMessage = FTDI_Device.INSTANCE.device_set_attenuation(attenuation);

            if (!deviceMessage.equals("all fine")) {
                setStatusLabel(attLabel, DeviceStatus.FAIL);
            } else {
                setStatusLabel(attLabel, DeviceStatus.OK);
            }
        } catch (NumberFormatException e) {
            AlertHelper.showAlert(Alert.AlertType.WARNING, "Invalid input", "Please enter a valid number.");
        }
    }

    public TextField getGainTextField() {
        return gainTextField;
    }

    public double getPTSFreq() throws NumberFormatException {
        return Double.parseDouble(ptsTextField.getText());
    }

    public TextField getAttTextField() {
        return attTextField;
    }

    public TextField getPtsTextField() {
        return ptsTextField;
    }
}

enum DeviceStatus {
    OK,
    FAIL,
    Ready
}