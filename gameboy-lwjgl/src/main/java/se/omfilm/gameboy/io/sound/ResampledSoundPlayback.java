package se.omfilm.gameboy.io.sound;

public class ResampledSoundPlayback implements SoundPlayback {
    private final SoundPlayback delegate;
    private final int steps;
    private final Filter filter;

    private int previousLeft = 0;
    private int previousRight = 0;

    public ResampledSoundPlayback(SoundPlayback delegate, Filter filter) {
        this.delegate = delegate;
        this.steps = delegate.sampleRate() / SoundPlayback.SAMPLING_RATE;
        if ((delegate.sampleRate() % SoundPlayback.SAMPLING_RATE) != 0) {
            throw new UnsupportedOperationException("Sampling rate of playback must be even dividable by the sampling rate of the emulator");
        }

        this.filter = filter;
    }

    public void start() {
        delegate.start();
    }

    public void stop() {
        delegate.stop();
    }

    public void output(int left, int right) {
        for (int i = 0; i < steps; i++) {
            delegate.output(filter.apply(left, previousLeft, i, steps), filter.apply(right, previousRight, i, steps));
        }
        previousLeft = left;
        previousRight = right;
    }

    public enum Filter {
        FLAT {
            public int apply(int current, int previous, int step, int steps) {
                return current;
            }
        },
        LINEAR {
            public int apply(int current, int previous, int step, int steps) {
                double perStep = ((double) current - previous) / steps;
                return (int) (current - ((steps - step - 1) * perStep));
            }
        };

        public abstract int apply(int current, int previous, int step, int steps);
    }
}
