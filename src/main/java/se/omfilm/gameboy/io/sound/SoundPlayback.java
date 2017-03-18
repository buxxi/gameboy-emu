package se.omfilm.gameboy.io.sound;

public interface SoundPlayback {
    int SAMPLING_RATE = 44100;
    int BITS_PER_SAMPLE = 8;
    int CHANNELS = 2;

    void start();

    void stop();

    void output(int left, int right);
}
