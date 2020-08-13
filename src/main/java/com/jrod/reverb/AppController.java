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
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


public class AppController implements Initializable {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private TabPane tabPane;

    @FXML
    private TextArea textToSayTextArea;
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
    private ComboBox<String> wavenetComboBox;
    private String selectedLangCode;
    private String selectedLangWavenet;
    @FXML
    private Slider pitchSlider;
    @FXML
    private Text pitchSliderValue;
    @FXML
    private Slider speedSlider;
    @FXML
    private Text speedSliderValue;
    @FXML
    private CheckBox enableEnhancedVoice;

    @FXML
    private TextField credTextField;
    private FileChooser googleJsonFileChooser;

    private Mixer.Info selectedOutput;
    private Mixer.Info selectedInput;

    private SoundRecordingUtil recorder;

    private Preferences preferences;

    @FXML
    private TextField recordButtonKeyCode;
    private int lastKeyPressedKeyCode = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing controller.");
        preferences = Preferences.userNodeForPackage(AppController.class);

        initRecordButton();
        initGlobalKeybindings();
        initGoogleCredentials();
        initLocalKeybindings();
        initInputAndOutPut();
        initVoiceAndWavenet();
        initPitch();
        initSpeed();
        initEnhancedVoice();
    }

    private void initRecordButton() {
        System.out.println("Initializing record button.");
        String savedRecordButtonKeyCode = preferences.get("savedRecordButtonKeyCode", "11");
        recordButtonKeyCode.setText(savedRecordButtonKeyCode);
    }

    private void initGlobalKeybindings() {
        System.out.println("Initializing global keybindings.");
        try {
            GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
                @Override
                public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

                }

                @Override
                public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                    lastKeyPressedKeyCode = nativeKeyEvent.getKeyCode();

                    if (nativeKeyEvent.getKeyCode() == Integer.parseInt(recordButtonKeyCode.getText()) && !isPressed) {
                        recordButtonPressedAction(null);
                        isPressed = true;
                    }
                }

                @Override
                public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
                    if (nativeKeyEvent.getKeyCode() == Integer.parseInt(recordButtonKeyCode.getText()) && isPressed) {
                        try {
                            recordButtonReleasedAction(null);
                            isPressed = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGoogleCredentials() {
        System.out.println("Initializing google credentials.");
        googleJsonFileChooser = new FileChooser();
        googleJsonFileChooser.setTitle("Select Google Credential JSON file");

        String savedGoogleCredFile = preferences.get("savedGoogleCredFile", "No file selected!");
        credTextField.setText(savedGoogleCredFile);
    }

    private void initLocalKeybindings() {
        System.out.println("Initializing local keybindings.");
        tabPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER && isControlPressed && !isEnterPressed) {
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
                if (event.getCode() == KeyCode.ENTER && isEnterPressed) {
                    isEnterPressed = false;
                } else if (event.getCode() == KeyCode.CONTROL) {
                    isControlPressed = false;
                }
            }
        });
    }

    private void initInputAndOutPut() {
        System.out.println("Initializing input and output device.");
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

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
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
    }

    private void initVoiceAndWavenet() {
        System.out.println("Initializing voice and wavenet.");
        LanguageProfiles profiles = new LanguageProfiles();
        voiceNameComboBox.getItems().addAll(profiles.getNames());
        voiceNameComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                preferences.put("savedVoiceName", newValue);
                wavenetComboBox.getItems().setAll(profiles.getWavenetsForName(newValue));
                selectedLangCode = profiles.getCodeForName(newValue);
            }
        });

        wavenetComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                preferences.put("savedWavenet", newValue);
                selectedLangWavenet = newValue;
            }
        });

        String savedWavenet = preferences.get("savedWavenet", "en-US-Wavenet-A");
        wavenetComboBox.setValue(savedWavenet);
        String savedVoiceName = preferences.get("savedVoiceName", "English (US)");
        voiceNameComboBox.setValue(savedVoiceName);
    }

    private void initPitch() {
        System.out.println("Initializing pitch.");
        pitchSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                pitchSliderValue.setText(String.format("%1.2f", newValue.doubleValue()));
                preferences.put("savedPitch", Double.toString(newValue.doubleValue()));
            }
        });

        String savedPitch = preferences.get("savedPitch", "0");
        pitchSlider.setValue(Double.parseDouble(savedPitch));
        pitchSliderValue.setText(String.format("%1.2f", Double.parseDouble(savedPitch)));
    }

    private void initSpeed() {
        System.out.println("Initializing speed.");
        speedSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                speedSliderValue.setText(String.format("%1.2f", newValue.doubleValue()));
                preferences.put("savedSpeed", Double.toString(newValue.doubleValue()));
            }
        });

        String savedSpeed = preferences.get("savedSpeed", "1");
        speedSlider.setValue(Double.parseDouble(savedSpeed));
        speedSliderValue.setText(String.format("%1.2f", Double.parseDouble(savedSpeed)));
    }

    private void initEnhancedVoice() {
        System.out.println("Initializing enhanced voice.");
        enableEnhancedVoice.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                preferences.put("savedEnableEnhancedVoice", newValue.toString());
            }
        });
        String savedEnableEnhancedVoice = preferences.get("savedEnableEnhancedVoice", "false");
        enableEnhancedVoice.setSelected(Boolean.parseBoolean(savedEnableEnhancedVoice));
    }

    @FXML
    protected void setRecordKeyCode() {
        preferences.put("savedRecordButtonKeyCode", Integer.toString(lastKeyPressedKeyCode));
        recordButtonKeyCode.setText(Integer.toString(lastKeyPressedKeyCode));
        System.out.println("Record button keybinding set to: " + lastKeyPressedKeyCode);
    }

    @FXML
    protected void playButtonAction(ActionEvent event) throws Exception {
        processTextAndPlayAudio(textToSayTextArea.getText());
    }

    @FXML
    protected void recordButtonPressedAction(MouseEvent event) {
        System.out.println("Pressed record button.");

        recorder = new SoundRecordingUtil(selectedInput);

        Thread recordThread = new Thread(recorder);
        recordThread.start();
    }

    @FXML
    protected void recordButtonReleasedAction(MouseEvent event) throws Exception {
        System.out.println("Released record button.");
        recorder.stop();
        //We check the time after the stop. Since the stop includes the artificial recording delay.
        Instant start = Instant.now();
        try {
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credTextField.getText())));
            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            SpeechClient speechClient = SpeechClient.create(settings);

            ByteString audioBytes = ByteString.copyFrom(recorder.getData());
            // Builds the sync recognize request
            RecognitionConfig.Builder builder = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(32000)
                    .setLanguageCode("en-US");
            if (enableEnhancedVoice.selectedProperty().getValue()) {
                System.out.println("Using enhanced voice recognition for request.");
                builder.setUseEnhanced(true);
                builder.setModel("video");
            }
            RecognitionConfig config = builder.build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();
            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();
            if (results.size() == 0) {
                System.out.println("No results");
            }
            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Speech-to-text result: %s%n", alternative.getTranscript());
                processTextAndPlayAudio(alternative.getTranscript());
                textToSayTextArea.setText(alternative.getTranscript());
                Instant finish = Instant.now();
                long timeElapsed = Duration.between(start, finish).toMillis();
                float timeInSeconds = timeElapsed / 1000.0f;
                System.out.printf("Total translation took: %.3f seconds%n", timeInSeconds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTextAndPlayAudio(String text) throws Exception {
        RunPlay worker = new RunPlay(credTextField.getText(), text, selectedOutput, pitchSlider.getValue(), speedSlider.getValue(), selectedLangCode, selectedLangWavenet);
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
            System.out.println("Google credential json set to: " + file.getAbsolutePath());
        }
    }
}
