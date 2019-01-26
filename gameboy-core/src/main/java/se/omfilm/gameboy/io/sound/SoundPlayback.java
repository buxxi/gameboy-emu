package se.omfilm.gameboy.io.sound;

public interface SoundPlayback {
    int SAMPLING_RATE = 8192;
    int BITS_PER_SAMPLE = 8;
    int CHANNELS = 2;

    void start(int samplingRate);

    void stop();

    void output(int left, int right);
}
