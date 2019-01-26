package se.omfilm.gameboy.io.sound;

public class ResampledSoundPlayback implements SoundPlayback {
    private static final int DEFAULT_SAMPLING_RATE = SoundPlayback.SAMPLING_RATE * 6;

    private final SoundPlayback delegate;
    private final Filter filter;
    private final int outputSamplingRate;
    private int steps;

    private int previousLeft = 0;
    private int previousRight = 0;

    public ResampledSoundPlayback(JavaSoundPlayback delegate, Filter filter) {
        this(delegate, filter, DEFAULT_SAMPLING_RATE);
    }

    public ResampledSoundPlayback(SoundPlayback delegate, Filter filter, int outputSamplingRate) {
        this.delegate = delegate;
        this.filter = filter;
        this.outputSamplingRate = outputSamplingRate;
    }

    public void start(int samplingRate) {
        if ((outputSamplingRate % samplingRate) != 0) {
            throw new UnsupportedOperationException("Sampling rate of playback must be even dividable by the sampling rate of the emulator");
        }
        this.steps = outputSamplingRate / samplingRate;
        delegate.start(outputSamplingRate);
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
