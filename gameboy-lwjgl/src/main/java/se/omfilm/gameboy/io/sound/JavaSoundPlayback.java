package se.omfilm.gameboy.io.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

//TODO: use lwjgl for sound too
public class JavaSoundPlayback implements SoundPlayback {
    private static final int AUDIO_DELAY = 10;
    private SourceDataLine line;

    private byte[] outputBuffer;
    private int bufferPosition = 0;

    public void start(int samplingRate) {
        AudioFormat format = new AudioFormat(samplingRate, BITS_PER_SAMPLE, CHANNELS, true, true);
        outputBuffer = new byte[(int) (((double) samplingRate) / 1000 * AUDIO_DELAY) * CHANNELS];
        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, outputBuffer.length);
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

        if (line.available() >= bufferPosition) {
            line.write(outputBuffer, 0, bufferPosition);
            bufferPosition = 0;
        } else {
            bufferPosition = bufferPosition % outputBuffer.length;
        }
    }
}
