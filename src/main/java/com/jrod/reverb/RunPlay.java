package com.jrod.reverb;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import javazoom.jl.player.JavaSoundAudioDeviceFactory;
import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class RunPlay implements Runnable{
    private String credFileString;
    private String inputText;
    private Mixer.Info mixerInfo;
    private double pitch;
    private double speed;
    private String langCode;
    private String wavenet;
    private boolean useSSML;

    public RunPlay(String credFileString, String inputText, Mixer.Info mixerInfo, double pitch, double speed, String langCode, String wavenet, boolean useSSML) {
        this.credFileString = credFileString;
        this.inputText = inputText;
        this.mixerInfo = mixerInfo;
        this.pitch = pitch;
        this.speed = speed;
        this.langCode = langCode;
        this.wavenet = wavenet;
        this.useSSML = useSSML;
    }

    @Override
    public void run() {
        try {
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credFileString)));
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);

//            ListVoicesRequest request = ListVoicesRequest.getDefaultInstance();
//
//            // Performs the list voices request
//            ListVoicesResponse response1 = textToSpeechClient.listVoices(request);
//            List<Voice> voices = response1.getVoicesList();
//
//            for (Voice voice : voices) {
//                // Display the voice's name. Example: tpc-vocoded
//                System.out.format("Name: %s\n", voice.getName());
//
//                // Display the supported language codes for this voice. Example: "en-us"
//                List<ByteString> languageCodes = voice.getLanguageCodesList().asByteStringList();
//                for (ByteString languageCode : languageCodes) {
//                    System.out.format("Supported Language: %s\n", languageCode.toStringUtf8());
//                }
//
//                // Display the SSML Voice Gender
//                System.out.format("SSML Voice Gender: %s\n", voice.getSsmlGender());
//
//                // Display the natural sample rate hertz for this voice. Example: 24000
//                System.out.format("Natural Sample Rate Hertz: %s\n\n", voice.getNaturalSampleRateHertz());
//            }

            SynthesisInput input = null;
            if(useSSML) {
                input = SynthesisInput.newBuilder().setSsml(this.inputText).build();
            } else {
                input = SynthesisInput.newBuilder().setText(this.inputText).build();
            }

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode(langCode)
                            .setName(wavenet)
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3)
                            .setPitch(pitch)
                            .setSpeakingRate(speed)
                            .build();

            System.out.println("Text-to-speech request sent to google for processing.");
            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();
            System.out.println("Text-to-speech response received from google.");
//            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
//            Mixer.Info info = mixerInfo[2]; //Edit this number to select output // 0 = Default
//
//            System.out.println(String.format("Name [%s]\n", info.getName()));
//            System.out.println(info.getDescription());

//            // Write the response to the output file.
//            try (OutputStream out = new FileOutputStream("output.mp3")) {
//                out.write(audioContents.toByteArray());
//                System.out.println("Audio content written to file \"output.mp3\"");
//            }

//            Media hit = new Media(getClass().getResource("output.mp3").toURI().toString());
//            Media hit = new Media(audioContents.toString());
//            MediaPlayer mediaPlayer = new MediaPlayer(hit);
//            mediaPlayer.play();

            System.out.println("Playing text-to-speech response audio.");
            Player player = new Player(audioContents.newInput());
            player.play();
//
//            AdvancedPlayer advancedPlayer = new AdvancedPlayer(audioContents.newInput());

//            Clip clip = AudioSystem.getClip(mixerInfo);
//            byte[] audioData = audioContents.toByteArray();
//            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
//            AudioInputStream audioInputStream = new AudioInputStream(bais, getAudioFormat(),
//                    audioData.length / getAudioFormat().getFrameSize());
//            clip.open(audioInputStream);
//            clip.start();
//            Thread.sleep(10000L);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("oopsie doopsie");
        }
    }
    AudioFormat getAudioFormat() {
        float sampleRate = 48000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }
}
