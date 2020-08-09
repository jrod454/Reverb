package com.jrod.reverb;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


public class AppController implements Initializable {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TabPane tabPane;

    @FXML
    private Button playButton;
    @FXML
    private Text outputText;
    @FXML
    private TextArea textToSayTextArea;
    @FXML
    private Button recordButton;
    private boolean isPressed = false;
    private boolean isEnterPressed = false;
    private boolean isControlPressed = false;

    //Settings
    @FXML
    private ComboBox<Mixer.Info> outputComboBoxSettings;
    @FXML
    private ComboBox<Mixer.Info> inputComboBoxSettings;
    @FXML
    private ComboBox<String> voiceNameComboBox;
    @FXML
    private Slider pitchSlider;
    @FXML
    private Text pitchSliderValue;
    @FXML
    private Slider speedSlider;
    @FXML
    private Text speedSliderValue;

    @FXML
    private TextField credTextField;
    @FXML
    private Button selectCredButton;
    private FileChooser googleJsonFileChooser;

    private Mixer.Info selectedOutput;
    private Mixer.Info selectedInput;

    private SoundRecordingUtil recorder;

    private boolean isRunning;

    private Preferences preferences;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing controller.");
        preferences = Preferences.userNodeForPackage(AppController.class);

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfo) {
            System.out.println(info.getName());
        }

        googleJsonFileChooser = new FileChooser();
        googleJsonFileChooser.setTitle("Select Google Credential JSON file");

        String savedGoogleCredFile = preferences.get("savedGoogleCredFile", "No file selected!");
        credTextField.setText(savedGoogleCredFile);

        tabPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.BACK_QUOTE && !isPressed) {
                    recordButtonPressedAction(null);
                    isPressed = true;
                } else if (event.getCode() == KeyCode.ENTER && isControlPressed && !isEnterPressed) {
                    try {
                        playButtonAction(null);
                        isEnterPressed = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (event.getCode() == KeyCode.CONTROL) {
                    isControlPressed = true;
                }
            }
        });

        tabPane.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.BACK_QUOTE && isPressed) {
                    try {
                        recordButtonReleasedAction(null);
                        isPressed = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (event.getCode() == KeyCode.ENTER && isEnterPressed) {
                    isEnterPressed = false;
                } else if (event.getCode() == KeyCode.CONTROL) {
                    isControlPressed = false;
                }
            }
        });

        outputComboBoxSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                preferences.put("savedSelectedOutputDevice", outputComboBoxSettings.getValue().getName());
                selectedOutput = outputComboBoxSettings.getValue();
            }
        });
        inputComboBoxSettings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                preferences.put("savedSelectedInputDevice", inputComboBoxSettings.getValue().getName());
                selectedInput = inputComboBoxSettings.getValue();
            }
        });

        outputComboBoxSettings.getItems().addAll(mixerInfo);
        inputComboBoxSettings.getItems().addAll(mixerInfo);

        String savedOutputDevice = preferences.get("savedSelectedOutputDevice", mixerInfo[0].getName());
        String savedInputDevice = preferences.get("savedSelectedInputDevice", mixerInfo[0].getName());
        for (Mixer.Info info : mixerInfo) {
            if (info.getName().equals(savedOutputDevice)) {
                selectedOutput = info;
                outputComboBoxSettings.setValue(info);
            }
            if (info.getName().equals(savedInputDevice)) {
                selectedInput = info;
                inputComboBoxSettings.setValue(info);
            }
        }

        voiceNameComboBox.getItems().addAll(
                "en-US-Wavenet-A",
                "en-US-Wavenet-B",
                "en-US-Wavenet-C",
                "en-US-Wavenet-D",
                "en-US-Wavenet-E",
                "en-US-Wavenet-F");

        String savedVoiceName = preferences.get("savedVoiceName", "en-US-Wavenet-A");
        voiceNameComboBox.setValue(savedVoiceName);

        voiceNameComboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                preferences.put("savedVoiceName", voiceNameComboBox.getValue());
            }
        });

        pitchSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pitchSliderValue.setText(String.format("%1.2f", newValue.doubleValue()));
                preferences.put("savedPitch", Double.toString(newValue.doubleValue()));
            }
        });

        speedSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                speedSliderValue.setText(String.format("%1.2f", newValue.doubleValue()));
                preferences.put("savedSpeed", Double.toString(newValue.doubleValue()));
            }
        });

        String savedPitch = preferences.get("savedPitch", "0");
        String savedSpeed = preferences.get("savedSpeed", "1");
        pitchSlider.setValue(Double.parseDouble(savedPitch));
        pitchSliderValue.setText(String.format("%1.2f", Double.parseDouble(savedPitch)));
        speedSlider.setValue(Double.parseDouble(savedSpeed));
        speedSliderValue.setText(String.format("%1.2f", Double.parseDouble(savedSpeed)));
    }

    @FXML
    protected void playButtonAction(ActionEvent event) throws Exception {
        processTextAndPlayAudio(textToSayTextArea.getText());
    }

    @FXML
    protected void recordButtonPressedAction(MouseEvent event) {
        System.out.println("pressed record");

        recorder = new SoundRecordingUtil(selectedInput);

        Thread recordThread = new Thread(recorder);
        recordThread.start();
    }

    @FXML
    protected void recordButtonReleasedAction(MouseEvent event) throws Exception {
        System.out.println("released record");
        recorder.stop();
        try {
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credTextField.getText())));
            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            SpeechClient speechClient = SpeechClient.create(settings);

            ByteString audioBytes = ByteString.copyFrom(recorder.getData());
            System.out.println("released 1");
            // Builds the sync recognize request
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(32000)
                            .setLanguageCode("en-US")
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();
            System.out.println("released 2");
            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            System.out.println("released 3");
            List<SpeechRecognitionResult> results = response.getResultsList();
            System.out.println("released 4");
            if (results.size() == 0) {
                System.out.println("No results");
            }
            for (SpeechRecognitionResult result : results) {
                System.out.println("results");
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
                processTextAndPlayAudio(alternative.getTranscript());
                textToSayTextArea.setText(alternative.getTranscript());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTextAndPlayAudio(String text) throws Exception {
        System.out.println("processing text");
        RunPlay worker = new RunPlay(credTextField.getText(), text, selectedOutput, voiceNameComboBox.getValue(), pitchSlider.getValue(), speedSlider.getValue());
        Thread thread = new Thread(worker);
        thread.start();
    }

    @FXML
    protected void chooseGoogleCredFile(ActionEvent event) {
        Stage stage = (Stage) anchorPane.getScene().getWindow();
        File file = googleJsonFileChooser.showOpenDialog(stage);
        if (file != null) {
            credTextField.setText(file.getAbsolutePath());
            preferences.put("savedGoogleCredFile", file.getAbsolutePath());
        }
    }
}
