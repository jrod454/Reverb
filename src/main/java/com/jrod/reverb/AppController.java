package com.jrod.reverb;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.*;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.protobuf.ByteString;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class AppController {
    @FXML private Button testButton;
    @FXML private Text outputText;
    @FXML private TextArea textToSayTextArea;

    private static final int BUFFER_SIZE = 4096;
    private ByteArrayOutputStream recordBytes;
    private TargetDataLine audioLine;
    private AudioFormat format;
    private SoundRecordingUtil recorder;

    private boolean isRunning;

    @FXML protected void handleTestButtonAction(ActionEvent event) throws Exception{
        outputText.setText("sadfsa" + Math.random());
        processTextAndPlayAudio(textToSayTextArea.getText());
    }

    @FXML protected void recordButtonPressedAction(MouseEvent event) throws Exception{
        System.out.println("pressed record");

        recorder = new SoundRecordingUtil();

        Thread recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Start recording...");
                    recorder.start();
                } catch (LineUnavailableException ex) {
                    ex.printStackTrace();
                    System.exit(-1);
                }
            }
        });
        recordThread.start();
    }

    @FXML protected void recordButtonReleasedAction(MouseEvent event) throws Exception{
        System.out.println("released record");
        recorder.stop();
        recorder.save(new File("testoutput.wav"));
        try {
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(getClass().getClassLoader().getResourceAsStream("Reverb-23a41ae3c3d9.json")));
            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            SpeechClient speechClient = SpeechClient.create(settings);

            // The path to the audio file to transcribe
            String fileName = "testoutput.wav";

            // Reads the audio file into memory
            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            ByteString audioBytes = ByteString.copyFrom(data);

            // Builds the sync recognize request
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                            .setSampleRateHertz(32000)
                            .setLanguageCode("en-US")
                            .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

            // Performs speech recognition on the audio file
            RecognizeResponse response = speechClient.recognize(config, audio);
            List<SpeechRecognitionResult> results = response.getResultsList();

            for (SpeechRecognitionResult result : results) {
                // There can be several alternative transcripts for a given chunk of speech. Just use the
                // first (most likely) one here.
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcription: %s%n", alternative.getTranscript());
                processTextAndPlayAudio(alternative.getTranscript());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTextAndPlayAudio(String text) throws Exception{
        RunPlay worker = new RunPlay(text);
        Thread thread = new Thread(worker);
        thread.start();
    }
}
