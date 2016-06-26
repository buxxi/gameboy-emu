package se.omfilm.gameboy.io.sound;

import javax.sound.sampled.*;

import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;

//TODO: use lwjgl for sound too
public class JavaSoundPlayback implements SoundPlayback {
    private SourceDataLine line;

    public void start() {
        AudioFormat format = new AudioFormat(PCM_SIGNED, SoundPlayback.SAMPLING_RATE, SoundPlayback.BITS_PER_SAMPLE, SoundPlayback.CHANNELS, 2, SoundPlayback.SAMPLING_RATE, false);
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

    public void write(byte[] buffer, int offset) {
        line.write(buffer, 0, offset);
    }
}
