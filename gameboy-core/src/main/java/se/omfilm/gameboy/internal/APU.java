package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.ByteArrayMemory;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.io.sound.SoundPlayback;
import se.omfilm.gameboy.util.EnumByValue;
import se.omfilm.gameboy.util.Runner.Counter;

import static se.omfilm.gameboy.io.sound.SoundPlayback.SAMPLING_RATE;
import static se.omfilm.gameboy.util.Runner.counter;

public class APU {
    private static final int WAVE_PATTERNS = 32;

    private final SoundPlayback device;

    private final Counter durationCounter = counter(this::stepDuration, CPU.FREQUENCY / 256);
    private final Counter sampleCounter = counter(this::playSample, CPU.FREQUENCY / SAMPLING_RATE);
    private final Counter envelopeCounter = counter(this::stepEnvelopes, CPU.FREQUENCY / 64);
    private final Counter sweepCounter = counter(this::stepSweep, CPU.FREQUENCY / 128);

    private final SquareWaveSound sound1 = new SquareWaveSound();
    private final SquareWaveSound sound2 = new SquareWaveSound();
    private final WaveSound sound3 = new WaveSound();
    private final NoiseSound sound4 = new NoiseSound();
    private final Sound[] allSounds = new Sound[] { sound1, sound2, sound3, sound4 };

    private final Channel leftChannel = new Channel(Terminal.LEFT);
    private final Channel rightChannel = new Channel(Terminal.RIGHT);

    private final NibbleArrayMemory wavePatternRAM = new NibbleArrayMemory(0xFF30, new byte[WAVE_PATTERNS]);

    private boolean enabled = false;

    public APU(SoundPlayback device) {
        this.device = device;
        device.start(SAMPLING_RATE); //TODO: this should probably be called from somewhere else and not the constructor
    }

    public void step(int cycles) {
        for (int i = 0; i < cycles; i++) {
            durationCounter.step();
            stepFrequency(); //Commenting out this makes the emulator run i 200fps more, can it be optimized?
            envelopeCounter.step();
            sweepCounter.step();
            sampleCounter.step();
        }
    }

    private void stepDuration() {
        for (Sound sound : allSounds) {
            sound.stepDuration();
        }
    }

    private void stepEnvelopes() {
        for (Sound sound : allSounds) {
            if (sound.enabledWithDAC()) {
                sound.stepEnvelope();
            }
        }
    }

    private void stepSweep() {
        if (sound1.enabledWithDAC()) {
            sound1.sweep.step();
        }
    }

    private void stepFrequency() {
        for (Sound sound : allSounds) {
            if (sound.enabledWithDAC()) {
                sound.stepFrequency();
            }
        }
    }

    private void playSample() {
        if (enabled) {
            int left = mixSample(leftChannel);
            int right = mixSample(rightChannel);
            device.output(left, right);
        } else {
            device.output(0, 0);
        }
    }

    private int mixSample(Channel channel) {
        int amp = 0;
        for (Sound sound : allSounds) {
            amp += sound.sample(channel.terminal);
        }
        amp *= channel.volume;
        amp = amp / 5; //TODO: better way to do this?

        return amp;
    }

    public void reset() {
        sweep(SoundId.SOUND1_SQUARE_WAVE, 0x80);
        length(SoundId.SOUND1_SQUARE_WAVE, 0xBF);
        envelope(SoundId.SOUND1_SQUARE_WAVE, 0xF3);
        highFrequency(SoundId.SOUND1_SQUARE_WAVE, 0xBF);
        length(SoundId.SOUND2_SQUARE_WAVE, 0x3F);
        envelope(SoundId.SOUND2_SQUARE_WAVE, 0x00);
        highFrequency(SoundId.SOUND2_SQUARE_WAVE, 0xBF);
        soundControl(SoundId.SOUND3_WAVE, 0x7F);
        length(SoundId.SOUND3_WAVE, 0xFF);
        outputLevel(SoundId.SOUND3_WAVE, 0x9F);
        highFrequency(SoundId.SOUND3_WAVE, 0xBF);
        length(SoundId.SOUND4_NOISE, 0xFF);
        envelope(SoundId.SOUND4_NOISE, 0x00);
        polynomialCounter(SoundId.SOUND4_NOISE, 0x00);
        highFrequency(SoundId.SOUND4_NOISE, 0xBF);
        channelControl(0x77);
        outputTerminal(0xF3);
        soundEnabled(0xF1);
    }

    public int soundEnabled() {
        return  (enabled ? 0b1000_0000 : 0) |
                0b0111_0000 |
                (sound4.enabledWithDAC() ? 0b0000_1000 : 0) |
                (sound3.enabledWithDAC() ? 0b0000_0100 : 0) |
                (sound2.enabledWithDAC() ? 0b0000_0010 : 0) |
                (sound1.enabledWithDAC() ? 0b0000_0001 : 0);
    }

    public void soundEnabled(int data) {
        enabled = (data & 0b1000_0000) != 0;
        if (!enabled) {
            sound1.reset();
            sound2.reset();
            sound3.reset();
            sound4.reset();
            leftChannel.reset();
            rightChannel.reset();
        }
    }

    public int channelControl() {
        return  (leftChannel.voiceInEnabled ? 0b1000_0000 : 0) |
                (leftChannel.volume << 4) |
                (rightChannel.voiceInEnabled ? 0b0000_1000 : 0) |
                rightChannel.volume;
    }

    public void channelControl(int data) {
        if (!enabled) {
            return;
        }
        leftChannel.voiceInEnabled = (data & 0b1000_0000) != 0;
        leftChannel.volume = (data & 0b0111_0000) >> 4;
        rightChannel.voiceInEnabled = (data & 0b0000_1000) != 0;
        rightChannel.volume = data & 0b0000_0111;
    }

    public int outputTerminal() {
        return  sound4.terminal.bits << 3 |
                sound3.terminal.bits << 2 |
                sound2.terminal.bits << 1 |
                sound1.terminal.bits;
    }

    public void outputTerminal(int data) {
        if (!enabled) {
            return;
        }
        sound4.terminal = Terminal.fromBinary((data & 0b1000_1000) >> 3);
        sound3.terminal = Terminal.fromBinary((data & 0b0100_0100) >> 2);
        sound2.terminal = Terminal.fromBinary((data & 0b0010_0010) >> 1);
        sound1.terminal = Terminal.fromBinary(data & 0b0001_0001);
    }

    public Memory wavePatternRAM() {
        return wavePatternRAM;
    }

    public void lowFrequency(SoundId soundId, int data) {
        if (!enabled) {
            return;
        }

        Frequency frequency = frequencyFromId(soundId);
        int mergedData = (frequency.value & ~0xFF) | data;
        switch (soundId) {
            case SOUND1_SQUARE_WAVE -> sound1.frequency = new Frequency(mergedData, 32);
            case SOUND2_SQUARE_WAVE -> sound2.frequency = new Frequency(mergedData, 32);
            case SOUND3_WAVE -> sound3.frequency = new Frequency(mergedData, 64);
        }
    }

    public void highFrequency(SoundId soundId, int data) {
        if (!enabled) {
            return;
        }

        Sound sound = soundFromId(soundId);
        Frequency frequency = frequencyFromId(soundId);
        Duration duration = durationFromId(soundId);

        boolean trigger = (data & 0b1000_0000) != 0;
        boolean enabled = (data & 0b0100_0000) != 0;
        int mergedData = (frequency.value & ~0xFF00) | ((data & 0b0000_0111) << 8);

        sound.enabled = sound.enabled || trigger;
        duration.trigger(trigger, enabled);

        switch (soundId) {
            case SOUND1_SQUARE_WAVE -> sound1.frequency = new Frequency(mergedData, 32);
            case SOUND2_SQUARE_WAVE -> sound2.frequency = new Frequency(mergedData, 32);
            case SOUND3_WAVE -> sound3.frequency = new Frequency(mergedData, 64);
        }
    }

    public int highFrequency(SoundId soundId) {
        return durationFromId(soundId).enabled ? 0b1111_1111 : 0b1011_1111;
    }

    public void envelope(SoundId soundId, int data) {
        if (!enabled) {
            return;
        }

        int initialVolume = (data & 0b1111_0000) >> 4;
        boolean dacEnabled = (data & 0b1111_1000) != 0;
        EnvelopeDirection direction = (data & 0b0000_1000) != 0 ? EnvelopeDirection.INCREASE : EnvelopeDirection.DECREASE;
        int steps = (data & 0b000_0111);

        Envelope envelope = new Envelope(initialVolume, direction, steps);

        switch (soundId) {
            case SOUND1_SQUARE_WAVE -> {
                sound1.dacEnabled = dacEnabled;
                sound1.envelope = envelope;
                if (!sound1.dacEnabled) {
                    sound1.enabled = false;
                }
            }
            case SOUND2_SQUARE_WAVE -> {
                sound2.dacEnabled = dacEnabled;
                sound2.envelope = envelope;
                if (!sound2.dacEnabled) {
                    sound2.enabled = false;
                }
            }
            case SOUND4_NOISE -> {
                sound4.dacEnabled = dacEnabled;
                sound4.envelope = envelope;
                if (!sound4.dacEnabled) {
                    sound4.enabled = false;
                }
            }
        }
    }

    public int envelope(SoundId soundId) {
        Envelope envelope = envelopeFromId(soundId);
        return  (envelope.initialVolume << 4) |
                (envelope.direction == EnvelopeDirection.INCREASE ? 0b0000_1000 : 0) |
                envelope.steps;
    }

    public void length(SoundId soundId, int data) {
        if (!enabled) {
            return;
        }

        Duration duration = durationFromId(soundId);
        duration.set(duration.mode == DurationMode.LONG ? data : (data & 0b0011_1111));

        switch (soundId) {
            case SOUND1_SQUARE_WAVE -> sound1.waveDutyMode = WaveDutyMode.fromValue(data >> 6);
            case SOUND2_SQUARE_WAVE -> sound2.waveDutyMode = WaveDutyMode.fromValue(data >> 6);
        }
    }

    public int length(SoundId soundId) {
        return switch (soundId) {
            case SOUND1_SQUARE_WAVE -> sound1.waveDutyMode.bits << 6 | 0b0011_1111;
            case SOUND2_SQUARE_WAVE -> sound2.waveDutyMode.bits << 6 | 0b0011_1111;
            case SOUND3_WAVE, SOUND4_NOISE -> 0b1111_1111;
        };
    }

    public int sweep(SoundId soundId) {
        if (soundId != SoundId.SOUND1_SQUARE_WAVE) {
            throw new IllegalArgumentException("Only sound1 can sweep.");
        }
        return  0b1000_0000 |
                (sound1.sweep.time << 4) |
                (sound1.sweep.mode == SweepMode.SUBTRACTION ? 0b0000_1000 : 0) |
                sound1.sweep.shifts;
    }

    public int soundControl(SoundId soundId) {
        if (soundId != SoundId.SOUND3_WAVE) {
            throw new IllegalArgumentException("Only sound3 can turn on/off.");
        }
        return sound3.dacEnabled ? 0b1111_1111 : 0b0111_1111;
    }

    public int outputLevel(SoundId soundId) {
        if (soundId != SoundId.SOUND3_WAVE) {
            throw new IllegalArgumentException("Only sound3 can read output levels.");
        }
        return sound3.wavePatternMode.bits << 5 | 0b1001_1111;
    }

    public int polynomialCounter(SoundId soundId) {
        if (soundId != SoundId.SOUND4_NOISE) {
            throw new IllegalArgumentException("Only sound4 can read polynomial counter.");
        }
        return  (sound4.polynomial.shiftClock << 4) |
                (sound4.polynomial.step == PolynomialStep._7_STEPS ? 0b0000_1000 : 0) |
                sound4.polynomial.dividingRatio;
    }

    public void sweep(SoundId soundId, int data) {
        if (soundId != SoundId.SOUND1_SQUARE_WAVE) {
            throw new IllegalArgumentException("Only sound1 can sweep.");
        }
        if (!enabled) {
            return;
        }

        int time = (data & 0b0111_0000) >> 4;
        SweepMode mode = (data & 0b0000_1000) != 0 ? SweepMode.SUBTRACTION : SweepMode.ADDITION;
        int shifts = data & 0b0000_0111;
        sound1.sweep = new Sweep(time, mode, shifts);
    }

    public void soundControl(SoundId soundId, int data) {
        if (soundId != SoundId.SOUND3_WAVE) {
            throw new IllegalArgumentException("Only sound3 can turn on/off.");
        }
        if (!enabled) {
            return;
        }

        sound3.dacEnabled = (data & 0b1000_0000) != 0;
        if (!sound3.dacEnabled) {
            sound3.enabled = false;
        }
    }

    public void outputLevel(SoundId soundId, int data) {
        if (soundId != SoundId.SOUND3_WAVE) {
            throw new IllegalArgumentException("Only sound3 can set output levels.");
        }
        if (!enabled) {
            return;
        }
        sound3.wavePatternMode = WavePatternMode.fromValue((data & 0b0110_0000) >> 5);
    }

    public void polynomialCounter(SoundId soundId, int data) {
        if (soundId != SoundId.SOUND4_NOISE) {
            throw new IllegalArgumentException("Only sound4 can set polynomial counter.");
        }
        if (!enabled) {
            return;
        }

        int shiftClock = (data & 0b1111_0000) >> 4;
        PolynomialStep steps = (data & 0b0000_1000) != 0 ? PolynomialStep._7_STEPS : PolynomialStep._15_STEPS;
        int dividingRatio = (data & 0b0000_0111);
        sound4.polynomial = new Polynomial(shiftClock, steps, dividingRatio);
    }

    private Envelope envelopeFromId(SoundId soundId) {
        return switch (soundId) {
            case SOUND1_SQUARE_WAVE -> sound1.envelope;
            case SOUND2_SQUARE_WAVE -> sound2.envelope;
            case SOUND4_NOISE -> sound4.envelope;
            default -> throw new IllegalArgumentException("Sound " + soundId + " has no envelope");
        };
    }

    private Frequency frequencyFromId(SoundId soundId) {
        return switch (soundId) {
            case SOUND1_SQUARE_WAVE -> sound1.frequency;
            case SOUND2_SQUARE_WAVE -> sound2.frequency;
            case SOUND3_WAVE -> sound3.frequency;
            case SOUND4_NOISE -> new Frequency(1, 1); //TODO
        };
    }

    private Sound soundFromId(SoundId soundId) {
        return allSounds[soundId.id - 1];
    }

    private Duration durationFromId(SoundId soundId) {
        return soundFromId(soundId).duration;
    }

    private class SquareWaveSound extends Sound {
        Frequency frequency = new Frequency(0, 32);
        Envelope envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
        WaveDutyMode waveDutyMode = WaveDutyMode.EIGHT;
        Sweep sweep = new Sweep(0, SweepMode.ADDITION, 0); //Should only be used in sound1

        public SquareWaveSound() {
            super(DurationMode.SHORT);
        }

        public int sample(Terminal terminal) {
            if (disabledFor(terminal)) {
                return 0;
            }

            return waveData() * envelope.volume();
        }

        public void stepEnvelope() {
            envelope.step();
        }

        public void stepFrequency() {
            frequency.step();
        }

        public void reset() {
            super.reset();
            frequency = new Frequency(0, 32);
            envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
            waveDutyMode = WaveDutyMode.EIGHT;
            sweep = new Sweep(0, SweepMode.ADDITION, 0);
        }

        private int waveData() {
            int phase = frequency.phase;
            return switch (waveDutyMode) {
                case HALF -> phase < 16 ? 1 : 0;
                case EIGHT -> phase < 4 ? 1 : 0;
                case QUARTER -> phase < 8 ? 1 : 0;
                case THREE_QUARTERS -> phase < 24 ? 1 : 0;
            };
        }
    }

    private class WaveSound extends Sound {
        Frequency frequency = new Frequency(0, 64);
        WavePatternMode wavePatternMode = WavePatternMode.MUTE;

        public WaveSound() {
            super(DurationMode.LONG);
        }

        public int sample(Terminal terminal) {
            if (disabledFor(terminal)) {
                return 0;
            }
            return switch (wavePatternMode) {
                case MUTE -> 0;
                case NORMAL -> wavePatternRAM.getNibble(frequency.phase);
                case SHIFTED_ONCE -> wavePatternRAM.getNibble(frequency.phase) >> 1;
                case SHIFTED_TWICE -> wavePatternRAM.getNibble(frequency.phase) >> 2;
            };
        }

        public void stepEnvelope() {

        }

        public void stepFrequency() {
            frequency.step();
        }

        public void reset() {
            super.reset();
            dacEnabled = false;
            frequency = new Frequency(0, 64);
            wavePatternMode = WavePatternMode.MUTE;
        }
    }

    private class NoiseSound extends Sound {
        Envelope envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
        Polynomial polynomial = new Polynomial(0, PolynomialStep._15_STEPS, 0);

        public NoiseSound() {
            super(DurationMode.SHORT);
        }

        public int sample(Terminal terminal) {
            if (disabledFor(terminal)) {
                return 0;
            }
            if (polynomial.produceSample()) {
                return envelope.volume();
            }
            return 0;
        }

        public void stepEnvelope() {
            envelope.step();
        }

        public void stepFrequency() {
            polynomial.step();
        }

        public void reset() {
            super.reset();
            envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
            polynomial = new Polynomial(0, PolynomialStep._15_STEPS, 0);
        }
    }

    private abstract class Sound {
        boolean enabled = false;
        boolean dacEnabled = false;
        Terminal terminal = Terminal.NONE;
        final Duration duration;

        public Sound(DurationMode durationMode) {
            duration = new Duration(0, this, durationMode);
        }

        public abstract int sample(Terminal terminal);

        public abstract void stepEnvelope();

        public abstract void stepFrequency();

        protected boolean disabledFor(Terminal terminal) {
            return !enabledWithDAC() || (this.terminal != Terminal.STEREO && this.terminal != terminal);
        }

        public void stepDuration() {
            duration.step();
        }

        public boolean enabledWithDAC() {
            return enabled && dacEnabled;
        }

        public void reset() {
            enabled = false;
            duration.reset();
            terminal = Terminal.NONE;
        }
    }

    private class Duration {
        private final Sound sound;
        private final DurationMode mode;

        private int counter;
        private boolean enabled = false;

        public Duration(int counter, Sound sound, DurationMode mode) {
            this.counter = counter;
            this.sound = sound;
            this.mode = mode;
        }

        public void step() {
            if (enabled && counter > 0) {
                this.counter--;

                if (this.counter == 0) {
                    sound.enabled = false;
                }
            }
        }

        public void set(int length) {
            counter = mode.from(length);
        }

        public void trigger(boolean trigger, boolean enable) {
            if (enable && !enabled && durationCounter.isFirstHalf()) {
                step();
                if (counter == 0 && trigger) {
                    counter = mode.maxValue() - 1;
                }
            }

            if (counter == 0 && trigger) {
                counter = mode.maxValue();
            }

            this.enabled = enable;
        }

        public void reset() {
            enabled = false;
            counter = 0;
        }
    }

    private class Sweep {
        private final int time;
        private final SweepMode mode;
        private final int shifts;

        private final Counter sweepCounter;
        private int shadowFrequency;

        public Sweep(int time, SweepMode mode, int shifts) {
            this.time = time;
            this.mode = mode;
            this.shifts = shifts;
            this.sweepCounter = counter(this::changeFrequency, time);
            if (time != 0) {
                shadowFrequency = sound1.frequency.value;
            }
        }

        public void step() {
            if (time != 0) {
                sweepCounter.step();
            }
        }

        private void changeFrequency() {
            int change = shadowFrequency >> shifts;
            if (mode == SweepMode.ADDITION) {
                shadowFrequency += change;
            } else {
                shadowFrequency -= change;
            }

            if (shadowFrequency > 2047) {
                sound1.enabled = false;
            } else {
                sound1.frequency = new Frequency(shadowFrequency, sound1.frequency.multiplier);
            }
        }
    }

    private static class Channel {
        private final Terminal terminal;
        private boolean voiceInEnabled = false;
        private int volume = 0;

        public Channel(Terminal terminal) {
            this.terminal = terminal;
        }

        public void reset() {
            voiceInEnabled = false;
            volume = 0;
        }
    }

    private static class Frequency {
        private final int value;

        private final Counter counter;
        private final int multiplier;

        private int phase = 0;

        public Frequency(int value, int multiplier) {
            this.value = value;
            this.multiplier = multiplier;
            this.counter = counter(this::increasePhase, initialCounterValue());
        }

        public void step() {
            counter.step();
        }

        private void increasePhase() {
            phase = (phase + 1) % WAVE_PATTERNS;
        }

        private int initialCounterValue() {
            int hz = CPU.FREQUENCY / (multiplier * (2048 - value));
            return (CPU.FREQUENCY / hz / WAVE_PATTERNS);
        }
    }

    private static class Envelope {
        private final int initialVolume;
        private final EnvelopeDirection direction;
        private final int steps;

        private final Counter stepCounter;
        private int volume;

        public Envelope(int initialVolume, EnvelopeDirection direction, int steps) {
            this.initialVolume = initialVolume;
            this.direction = direction;
            this.steps = steps;

            this.stepCounter = counter(this::changeVolume, steps);
            this.volume = initialVolume;
        }

        public void step() {
            if (steps == 0) {
                return;
            }
            stepCounter.step();
        }

        public int volume() {
            return volume;
        }

        private void changeVolume() {
            if (direction == EnvelopeDirection.INCREASE && volume < 0xF) {
                volume++;
            } else if (direction == EnvelopeDirection.DECREASE && volume > 0){
                volume--;
            }
        }
    }

    private static class Polynomial {
        private final int shiftClock;
        private final PolynomialStep step;
        private final int dividingRatio;

        private final Counter counter;
        private int lfsr = 0b0111_1111_1111_1111;

        public Polynomial(int shiftClock, PolynomialStep steps, int dividingRatio) {
            this.shiftClock = shiftClock;
            step = steps;
            this.dividingRatio = dividingRatio;
            int frequency = resolveDivisor() << shiftClock;
            counter = new Counter(this::trigger, CPU.FREQUENCY / frequency);
        }

        public boolean produceSample() {
            return (lfsr & 0b0000_0000_0000_0001) == 0;
        }

        public void step() {
            counter.step();
        }

        private void trigger() {
            int xored = (lfsr & 0b0000_0000_0000_0001) ^ ((lfsr & 0b0000_0000_0000_0010) >> 1);
            lfsr = lfsr >> 1;
            if (xored != 0) {
                lfsr = lfsr | 0b0100_0000_0000_0000;
            }

            if (step == PolynomialStep._7_STEPS) {
                lfsr = lfsr & 0b0111_1111;
                if (xored != 0) {
                    lfsr = lfsr | 0b0100_0000;
                }
            }
        }

        private int resolveDivisor() {
            return switch (dividingRatio) {
                case 1 -> 16;
                case 2 -> 32;
                case 3 -> 48;
                case 4 -> 64;
                case 5 -> 80;
                case 6 -> 96;
                case 7 -> 112;
                default -> 8;
            };
        }
    }

    private enum Terminal implements EnumByValue.ComparableByInt {
        NONE(0b0000_0000),
        LEFT(0b0000_0001),
        RIGHT(0b0001_0000),
        STEREO(0b0001_0001);

        private final static EnumByValue<Terminal> valuesCache = EnumByValue.create(values(), Terminal.class, Terminal::missing);
        private final int bits;

        Terminal(int bits) {
            this.bits = bits;
        }

        static Terminal fromBinary(int input) {
            return valuesCache.fromValue(input);
        }

        public int compareTo(int value) {
            return value - bits;
        }

        public static void missing(int value) {
            throw new IllegalArgumentException("Invalid terminal value: " + value);
        }
    }

    private enum EnvelopeDirection {
        DECREASE,
        INCREASE
    }

    private enum DurationMode {
        SHORT(64), // Length = (64-t1)*(1/256) seconds
        LONG(256); // Length = (256-t1)*(1/2) seconds

        private final int maxValue;

        DurationMode(int maxValue) {
            this.maxValue = maxValue;
        }

        public int maxValue() {
            return maxValue;
        }

        public int from(int value) {
            return maxValue() - value;
        }
    }

    private enum WavePatternMode implements EnumByValue.ComparableByInt {
        MUTE            (0b00),
        NORMAL          (0b01),
        SHIFTED_ONCE    (0b10),
        SHIFTED_TWICE   (0b11);

        private final static EnumByValue<WavePatternMode> valuesCache = EnumByValue.create(values(), WavePatternMode.class, WavePatternMode::missing);
        private final int bits;

        WavePatternMode(int bits) {
            this.bits = bits;
        }

        public static WavePatternMode fromValue(int input) {
            return valuesCache.fromValue(input);
        }

        public int compareTo(int value) {
            return value - bits;
        }

        public static void missing(int input) {
            throw new IllegalArgumentException("Invalid WavePatternMode value: " + input);
        }
    }

    private enum WaveDutyMode implements EnumByValue.ComparableByInt {
        EIGHT(0b00),
        QUARTER(0b01),
        HALF(0b10),
        THREE_QUARTERS(0b11);

        private final static EnumByValue<WaveDutyMode> valuesCache = EnumByValue.create(values(), WaveDutyMode.class, WaveDutyMode::missing);
        private final int bits;

        WaveDutyMode(int bits) {
            this.bits = bits;
        }

        public static WaveDutyMode fromValue(int input) {
            return valuesCache.fromValue(input);
        }

        public int compareTo(int value) {
            return value - bits;
        }

        public static void missing(int input) {
            throw new IllegalArgumentException("Invalid WaveDutyMode value: " + input);
        }
    }

    private enum SweepMode {
        ADDITION,
        SUBTRACTION
    }

    private enum PolynomialStep {
        _15_STEPS,
        _7_STEPS
    }

    public enum SoundId {
        SOUND1_SQUARE_WAVE(1),
        SOUND2_SQUARE_WAVE(2),
        SOUND3_WAVE(3),
        SOUND4_NOISE(4);

        private final int id;

        SoundId(int id) {
            this.id = id;
        }
    }

    /**
     * The wave table samples is only 4 bit and is written with a whole byte representing 2 samples.
     * So reuse the ByteArrayMemory and make it twice as big so the nibbles can be accessed without bitshifting.
     */
    private static class NibbleArrayMemory extends ByteArrayMemory {
        public NibbleArrayMemory(int offset, byte[] data) {
            super(offset, data);
        }

        @Override
        public int readByte(int address) {
            int index = (address - offset) * 2;
            return (this.data[index] << 4) | this.data[index + 1];
        }

        @Override
        public void writeByte(int address, int data) {
            int index = (address - offset) * 2;
            this.data[index] = (byte) ((data >> 4) & 0b0000_1111);
            this.data[index + 1] = (byte) (data & 0b0000_1111);
        }

        public byte getNibble(int index) {
            return this.data[index];
        }
    }
}
