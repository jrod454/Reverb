package com.jrod.reverb;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;
import sun.audio.AudioDataStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Mixer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

public class AppController {
    @FXML private Button testButton;
    @FXML private Text outputText;
    @FXML private TextArea textToSayTextArea;

    @FXML protected void handleTestButtonAction(ActionEvent event) throws Exception{
        outputText.setText("sadfsa" + Math.random());
        RunPlay worker = new RunPlay(textToSayTextArea.getText());
        Thread thread = new Thread(worker);
        thread.start();
    }
}
