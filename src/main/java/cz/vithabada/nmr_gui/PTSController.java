package cz.vithabada.nmr_gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import spinapi.SpinAPI;

import java.net.URL;
import java.util.ResourceBundle;

public class PTSController implements Initializable {

    @FXML
    TextField frequency;

    @FXML
    Label label;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO USB-PTS interface
    }

    /**
     * Will set PTS frequency for model 250.
     */
    @FXML
    public void handleSetFrequencyAction() {
        double maxFreq = 250;
        int is160 = 0;
        int is3200 = 0;
        int allowPhase = 0;
        int noPTS = 1;

        try {
            double freq = Double.parseDouble(frequency.getText());

            label.setText("");

            if (freq < 0) {
                label.setText("Frequency must be > 0");
            }

            if (freq > maxFreq) {
                label.setText("Frequency must be < " + maxFreq);
            }

            if (SpinAPI.INSTANCE.set_pts(maxFreq, is160, is3200, allowPhase, noPTS, freq, 0) != 0) {
                label.setText(SpinAPI.INSTANCE.spinpts_get_error());
            } else {
                label.setText("Frequency set");
            }
        } catch (NumberFormatException e) {
            label.setText("Invalid number format.");
        }
    }

}
