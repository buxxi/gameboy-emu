package se.omfilm.gameboy.io.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

//TODO: use lwjgl for sound too
public class JavaSoundPlayback implements SoundPlayback {
    private static final int AUDIO_DELAY = 100;
    private SourceDataLine line;

    private final byte[] outputBuffer = new byte[(int) (((double) SAMPLING_RATE * CHANNELS) / 1000 * AUDIO_DELAY)];
    private int bufferPosition = 0;

    public void start() {
        AudioFormat format = new AudioFormat(SAMPLING_RATE, BITS_PER_SAMPLE, CHANNELS, true, true);
        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open();
            line.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace(); //TODO
        }
    }

    public void stop() {
        line.drain();
        line.stop();
    }

    public void output(int left, int right) {
        outputBuffer[bufferPosition++] = (byte) left;
        outputBuffer[bufferPosition++] = (byte) right;
        bufferPosition = bufferPosition % outputBuffer.length;

        if (bufferPosition == 0) {
            line.write(outputBuffer, 0, outputBuffer.length);
        }
    }
}
