package se.omfilm.gameboy.io.sound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//TODO: use lwjgl for sound too
public class JavaSoundPlayback implements SoundPlayback {
    private static final Logger log = LoggerFactory.getLogger(JavaSoundPlayback.class);
    private static final int BUFFER_TIME_MS = 100;

    private final ReentrantReadWriteLock bufferLock = new ReentrantReadWriteLock();
    private SourceDataLine line;

    private byte[] outputBuffer;
    private int writeBufferPosition = 0;
    private int readBufferPosition = 0;
    private boolean playing = true;

    public void start(int samplingRate) {
        AudioFormat format = new AudioFormat(samplingRate, BITS_PER_SAMPLE, CHANNELS, true, true);
        outputBuffer = new byte[(int) (((double) samplingRate) / 1000 * BUFFER_TIME_MS) * CHANNELS];
        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, outputBuffer.length);
            line.start();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        Executors.newSingleThreadExecutor().execute(this::playback);
    }

    public void stop() {
        playing = false;
    }

    public void output(int left, int right) {
        bufferLock.writeLock().lock();
        try {
            outputBuffer[writeBufferPosition++] = (byte) left;
            outputBuffer[writeBufferPosition++] = (byte) right;
            writeBufferPosition = writeBufferPosition % outputBuffer.length;
        } finally {
            bufferLock.writeLock().unlock();
        }
    }

    private int available() {
        if (writeBufferPosition > readBufferPosition) {
            return writeBufferPosition - readBufferPosition;
        } else {
            return outputBuffer.length - readBufferPosition + writeBufferPosition;
        }
    }

    private void playback() {
        while (playing) {
            int availableInput = line.available();
            if (availableInput > 0) {
                bufferLock.readLock().lock();
                try {
                    int availableOutput = available();
                    int outputCount = Math.min(availableInput, availableOutput);
                    writeToLine(outputCount);
                } finally {
                    bufferLock.readLock().unlock();
                }
            } else {
                //TODO: how to handle wait for available?
                log.debug("No available bytes in output device");
            }
        }

        line.drain();
        line.stop();
    }

    private void writeToLine(int outputCount) {
        if (readBufferPosition + outputCount > outputBuffer.length) {
            int before = outputBuffer.length - readBufferPosition;
            writeToLine(before);
            writeToLine(outputCount - before);
            return;
        }

        line.write(outputBuffer, readBufferPosition, outputCount);
        readBufferPosition = (readBufferPosition + outputCount) % outputBuffer.length;
    }
}
