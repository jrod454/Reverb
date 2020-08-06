package com.jrod.reverb;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Mixer;
import java.io.ByteArrayInputStream;

public class RunPlay implements Runnable{
    private String inputText;

    public RunPlay(String str) {
        this.inputText = str;
    }

    @Override
    public void run() {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(this.inputText).build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
            Mixer.Info info = mixerInfo[2]; //Edit this number to select output // 0 = Default

            System.out.println(String.format("Name [%s]\n", info.getName()));
            System.out.println(info.getDescription());

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

//            Clip clip = AudioSystem.getClip(info);
//            clip.open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(audioContents.toByteArray())));
//            clip.start();
//            Thread.sleep(10000L);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("oopsie doopsie");
        }
    }
}
