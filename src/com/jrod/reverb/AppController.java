package com.jrod.reverb;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class AppController {
    @FXML private Button testButton;
    @FXML private Text outputText;

    @FXML protected void handleTestButtonAction(ActionEvent event) {
        outputText.setText("sadfsa" + Math.random());
    }
}
