package com.jrod.reverb;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import javazoom.jl.player.Player;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class RunPlay implements Runnable{
    private String credFileString;
    private String inputText;
    private Mixer.Info mixerInfo;
    private String voiceName;
    private double pitch;
    private double speed;

    public RunPlay(String credFileString, String str, Mixer.Info info, String voiceName, double pitch, double speed) {
        this.credFileString = credFileString;
        this.inputText = str;
        this.mixerInfo = info;
        this.voiceName = voiceName;
        this.pitch = pitch;
        this.speed = speed;
    }

    @Override
    public void run() {
        try {
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream(credFileString)));
            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(this.inputText).build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setName(voiceName)
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3)
                            .setPitch(pitch)
                            .setSpeakingRate(speed)
                            .build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();
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
