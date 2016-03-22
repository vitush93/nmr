package cz.vithabada.nmr_gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class ContWindowController implements Initializable {

    public Button startContButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void handleStartCont(ActionEvent actionEvent) {
        // TODO refactor
//        if (!isBoardConnected()) return;
//
//        pulse = createPulse(); // create pulse with initial parameters
//        initPulse();
//
//        pulseTask = createPulseTask();
    }
}
