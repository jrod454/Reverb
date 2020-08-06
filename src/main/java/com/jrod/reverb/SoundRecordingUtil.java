package com.jrod.reverb;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SoundRecordingUtil {
    private static final int BUFFER_SIZE = 4096;
    private ByteArrayOutputStream recordBytes;
    private TargetDataLine audioLine;
    private AudioFormat format;

    private boolean isRunning;

    AudioFormat getAudioFormat() {
        float sampleRate = 32000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }

    public void start() throws LineUnavailableException{
        format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        System.out.println("stuck1");
        // checks if system supports the data line
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException(
                    "The system does not support the specified format.");
        }

        audioLine = AudioSystem.getTargetDataLine(format);

        System.out.println("stuck2");

        audioLine.open(format, BUFFER_SIZE * 5);
        audioLine.start();

        System.out.println("stuck3");

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = 0;

        recordBytes = new ByteArrayOutputStream();
        isRunning = true;

        while (isRunning) {
            bytesRead = audioLine.read(buffer, 0, buffer.length);
            recordBytes.write(buffer, 0, bytesRead);
        }
    }

    public void stop() throws IOException, InterruptedException {
        Thread.sleep(1000);
        isRunning = false;
        if (audioLine != null) {
            System.out.println("stuck15555");
            audioLine.flush();
            System.out.println("stuck6771");
            audioLine.close();
            System.out.println("stuck13333332222");
        }
    }

    public void save(File wavFile) throws IOException {
        byte[] audioData = recordBytes.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format,
                audioData.length / format.getFrameSize());

        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);

        audioInputStream.close();
        recordBytes.close();
    }
}
