package com.jrod.reverb;

//import sun.audio.AudioDataStream;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SoundRecordingUtil implements Runnable{
    private static final int BUFFER_SIZE = 4096;
    private ByteArrayOutputStream recordBytes;
    private TargetDataLine audioLine;
    private AudioFormat format;
    private Mixer.Info mixerInfo;

    private boolean isRunning;

    public SoundRecordingUtil(Mixer.Info mixerInfo) {
        this.mixerInfo = mixerInfo;
    }

    AudioFormat getAudioFormat() {
        float sampleRate = 32000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
                bigEndian);
    }

    @Override
    public void run() {
        format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        System.out.println("stuck1");

        try {
            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException(
                        "The system does not support the specified format.");
            }

            audioLine = AudioSystem.getTargetDataLine(format, mixerInfo);

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
            if (audioLine != null) {
                audioLine.flush();
                audioLine.close();
            }
            try {
                recordBytes.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws InterruptedException {
        Thread.sleep(1000);
        isRunning = false;
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

    public byte[] getData() throws Exception{
        byte[] audioData = recordBytes.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format,
                audioData.length / format.getFrameSize());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public AudioInputStream getStream() {
        byte[] audioData = recordBytes.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        return new AudioInputStream(bais, format,
                audioData.length / format.getFrameSize());
    }
}
