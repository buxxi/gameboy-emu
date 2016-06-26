package se.omfilm.gameboy.io.sound;

public interface SoundPlayback {
    int SAMPLING_RATE = 44100;
    int BITS_PER_SAMPLE = 16;
    int CHANNELS = 1;

    void start();

    void stop();

    void write(byte[] buffer, int offset);
}
