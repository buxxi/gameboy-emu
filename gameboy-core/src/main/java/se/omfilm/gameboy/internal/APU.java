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
            stepDuration();
            stepFrequency(); //Commenting out this makes the emulator run i 200fps more, can it be optimized?
            envelopeCounter.step();
            sweepCounter.step();
            sampleCounter.step();
        }
    }

    private void stepDuration() {
        for (Sound sound : allSounds) {
            if (sound.enabled) {
                sound.stepDuration();
            }
        }
    }

    private void stepEnvelopes() {
        for (Sound sound : allSounds) {
            if (sound.enabled) {
                sound.stepEnvelope();
            }
        }
    }

    private void stepSweep() {
        if (sound1.enabled) {
            sound1.sweep.step();
        }
    }

    private void stepFrequency() {
        for (Sound sound : allSounds) {
            if (sound.enabled) {
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

    private void restartSound(SoundId sound) {
        allSounds[sound.id - 1].reset();
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
        soundMode(SoundId.SOUND4_NOISE, 0xBF);
        channelControl(0x77);
        outputTerminal(0xF3);
        soundEnabled(0xF1);
    }

    public int soundEnabled() {
        return  (enabled ? 0b1000_0000 : 0) |
                0b0111_0000 |
                (enabled && sound4.enabled && sound4.terminal != Terminal.NONE ? 0b0000_1000 : 0) |
                (enabled && sound3.enabled && sound3.terminal != Terminal.NONE ? 0b0000_0100 : 0) |
                (enabled && sound2.enabled && sound2.terminal != Terminal.NONE ? 0b0000_0010 : 0) |
                (enabled && sound1.enabled && sound1.terminal != Terminal.NONE ? 0b0000_0001 : 0);
    }

    public void soundEnabled(int data) {
        if ((data & 0b1000_0000) == 0) {
            sweep(SoundId.SOUND1_SQUARE_WAVE, 0x00);
            length(SoundId.SOUND1_SQUARE_WAVE, 0x00);
            envelope(SoundId.SOUND1_SQUARE_WAVE, 0x00);
            lowFrequency(SoundId.SOUND1_SQUARE_WAVE, 0x00);
            highFrequency(SoundId.SOUND1_SQUARE_WAVE, 0x00);
            length(SoundId.SOUND2_SQUARE_WAVE, 0x00);
            envelope(SoundId.SOUND2_SQUARE_WAVE, 0x00);
            lowFrequency(SoundId.SOUND2_SQUARE_WAVE, 0x00);
            highFrequency(SoundId.SOUND2_SQUARE_WAVE, 0x00);
            soundControl(SoundId.SOUND3_WAVE, 0x00);
            length(SoundId.SOUND3_WAVE, 0x00);
            outputLevel(SoundId.SOUND3_WAVE, 0x00);
            lowFrequency(SoundId.SOUND3_WAVE, 0x00);
            highFrequency(SoundId.SOUND3_WAVE, 0x00);
            length(SoundId.SOUND4_NOISE, 0x00);
            envelope(SoundId.SOUND4_NOISE, 0x00);
            polynomialCounter(SoundId.SOUND4_NOISE, 0x00);
            soundMode(SoundId.SOUND4_NOISE, 0x00);
            channelControl(0x00);
            outputTerminal(0x00);
            enabled = false;
            sound1.enabled = false;
            sound2.enabled = false;
            sound3.enabled = false;
            sound4.enabled = false;
        } else {
            enabled = true;
            sound1.enabled = true;
            sound2.enabled = true;
            sound4.enabled = true;
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
            case SOUND1_SQUARE_WAVE:
                sound1.frequency = new Frequency(mergedData, 32);
                break;
            case SOUND2_SQUARE_WAVE:
                sound2.frequency = new Frequency(mergedData, 32);
                break;
            case SOUND3_WAVE:
                sound3.frequency = new Frequency(mergedData, 64);
                break;
        }
    }

    public void highFrequency(SoundId soundId, int data) {
        if (!enabled) {
            return;
        }
        Frequency frequency = frequencyFromId(soundId);
        int mergedData = (frequency.value & ~0xFF00) | ((data & 0b0000_0111) << 8);
        SoundMode mode = (data & 0b0100_0000) != 0 ? SoundMode.USE_DURATION : SoundMode.REPEAT;
        switch (soundId) {
            case SOUND1_SQUARE_WAVE:
                sound1.frequency = new Frequency(mergedData, 32);
                sound1.soundMode = mode;
                break;
            case SOUND2_SQUARE_WAVE:
                sound2.frequency = new Frequency(mergedData, 32);
                sound2.soundMode = mode;
                break;
            case SOUND3_WAVE:
                sound3.frequency = new Frequency(mergedData, 64);
                sound3.soundMode = mode;
                break;
            case SOUND4_NOISE:
                sound4.soundMode = mode;
                break;
        }
        if ((data & 0b1000_0000) != 0) {
            restartSound(soundId);
        }
    }

    public int highFrequency(SoundId soundId) {
        return allSounds[soundId.id - 1].soundMode == SoundMode.USE_DURATION ? 0b1111_1111 : 0b1011_1111;
    }

    public void envelope(SoundId soundId, int data) {
        if (!enabled) {
            return;
        }

        int initialVolume = (data & 0b1111_0000) >> 4;
        EnvelopeDirection direction = (data & 0b0000_1000) != 0 ? EnvelopeDirection.INCREASE : EnvelopeDirection.DECREASE;
        int steps = (data & 0b000_0111);

        Envelope envelope = new Envelope(initialVolume, direction, steps);

        switch (soundId) {
            case SOUND1_SQUARE_WAVE:
                sound1.envelope = envelope;
                break;
            case SOUND2_SQUARE_WAVE:
                sound2.envelope = envelope;
                break;
            case SOUND4_NOISE:
                sound4.envelope = envelope;
                break;
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

        switch(soundId) {
            case SOUND1_SQUARE_WAVE:
                sound1.waveDutyMode = WaveDutyMode.fromValue(data >> 6);
                sound1.duration = new Duration(data & 0b0011_1111, DurationMode.SHORT, sound1);
                break;
            case SOUND2_SQUARE_WAVE:
                sound2.waveDutyMode = WaveDutyMode.fromValue(data >> 6);
                sound2.duration = new Duration(data & 0b0011_1111, DurationMode.SHORT, sound2);
                break;
            case SOUND3_WAVE:
                sound3.duration = new Duration(data, DurationMode.LONG, sound3);
                break;
            case SOUND4_NOISE:
                sound4.duration = new Duration(data & 0b0011_1111, DurationMode.SHORT, sound4);
                break;
        }
    }

    public int length(SoundId soundId) {
        switch (soundId) {
            case SOUND1_SQUARE_WAVE:
                return sound1.waveDutyMode.bits << 6 | 0b0011_1111;
            case SOUND2_SQUARE_WAVE:
                return sound2.waveDutyMode.bits << 6 | 0b0011_1111;
            case SOUND3_WAVE:
                return sound3.enabled ? sound3.duration.length : 0b1111_1111;
            case SOUND4_NOISE:
                return sound4.enabled && sound4.terminal != Terminal.NONE ? (sound4.duration.length | 0b1100_0000) : 0xFF;
            default:
                throw new IllegalArgumentException("Sound " + soundId + " has no length");
        }
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
        return sound3.enabled ? 0b1111_1111 : 0b0111_1111;
    }

    public int outputLevel(SoundId soundId) {
        if (soundId != SoundId.SOUND3_WAVE) {
            throw new IllegalArgumentException("Only sound3 can read output levels.");
        }
        return sound3.wavePatternMode.bits << 5 | 0b1001_1111;
    }

    public int soundMode(SoundId soundId) {
        if (soundId != SoundId.SOUND4_NOISE) {
            throw new IllegalArgumentException("Only sound4 can read sound mode.");
        }
        return sound4.soundMode == SoundMode.USE_DURATION ? 0b1111_1111 : 0b1011_1111;
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

        sound3.enabled = (data & 0b1000_0000) != 0;
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

    public void soundMode(SoundId soundId, int data) {
        if (soundId != SoundId.SOUND4_NOISE) {
            throw new IllegalArgumentException("Only sound4 can set sound mode.");
        }
        if (!enabled) {
            return;
        }

        sound4.soundMode = (data & 0b0100_0000) != 0 ? SoundMode.USE_DURATION : SoundMode.REPEAT;
        if ((data & 0b1000_0000) != 0) {
            restartSound(soundId);
        }
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
        switch (soundId) {
            case SOUND1_SQUARE_WAVE:
                return sound1.envelope;
            case SOUND2_SQUARE_WAVE:
                return sound2.envelope;
            case SOUND4_NOISE:
                return sound4.envelope;
            default:
                throw new IllegalArgumentException("Sound " + soundId + " has no envelope");
        }
    }

    private Frequency frequencyFromId(SoundId soundId) {
        switch (soundId) {
            case SOUND1_SQUARE_WAVE:
                return sound1.frequency;
            case SOUND2_SQUARE_WAVE:
                return sound2.frequency;
            case SOUND3_WAVE:
                return sound3.frequency;
            default:
                throw new IllegalArgumentException("Sound " + soundId + " has no frequency");
        }
    }

    private class SquareWaveSound extends Sound {
        Frequency frequency = new Frequency(0, 32);
        Envelope envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
        WaveDutyMode waveDutyMode = WaveDutyMode.HALF;
        Sweep sweep = new Sweep(0, SweepMode.ADDITION, 0); //Should only be used in sound1

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
            frequency.reset();
            envelope.reset();
            duration.reset();
            enabled = true;
        }

        private int waveData() {
            int phase = frequency.phase;
            switch (waveDutyMode) {
                case HALF:
                    return phase < 16 ? 1 : 0;
                case EIGHT:
                    return phase < 4 ? 1 : 0;
                case QUARTER:
                    return phase < 8 ? 1 : 0;
                case THREE_QUARTERS:
                    return phase < 24 ? 1 : 0;
            }
            return 0;
        }
    }

    private class WaveSound extends Sound {
        Frequency frequency = new Frequency(0, 64);
        WavePatternMode wavePatternMode = WavePatternMode.MUTE;

        public int sample(Terminal terminal) {
            if (disabledFor(terminal)) {
                return 0;
            }
            switch (wavePatternMode) {
                case MUTE:
                    return 0;
                case NORMAL:
                    return wavePatternRAM.getNibble(frequency.phase);
                case SHIFTED_ONCE:
                    return wavePatternRAM.getNibble(frequency.phase) >> 1;
                case SHIFTED_TWICE:
                    return wavePatternRAM.getNibble(frequency.phase) >> 2;
            }
            return 0;
        }

        public void stepEnvelope() {

        }

        public void stepFrequency() {
            frequency.step();
        }

        public void reset() {
            sound3.frequency.reset();
            sound3.duration.reset();
            sound3.enabled = true;
        }
    }

    private class NoiseSound extends Sound {
        Envelope envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
        Polynomial polynomial = new Polynomial(0, PolynomialStep._15_STEPS, 0);
        SoundMode soundMode = SoundMode.USE_DURATION;

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
            sound4.duration.reset();
            sound4.enabled = true;
        }
    }

    private abstract class Sound {
        boolean enabled = false;
        Terminal terminal = Terminal.NONE;
        Duration duration = new Duration(0, DurationMode.SHORT, this);
        SoundMode soundMode = SoundMode.REPEAT;

        public abstract int sample(Terminal terminal);

        protected boolean disabledFor(Terminal terminal) {
            return !enabled || (this.terminal != Terminal.STEREO && this.terminal != terminal);
        }

        protected void disable() {
            this.enabled = false;
        }

        public void stepDuration() {
            duration.step();
        }

        public abstract void stepEnvelope();

        public abstract void stepFrequency();

        public abstract void reset();
    }

    private class Duration {
        private final int length;
        private final DurationMode mode;
        private final Counter counter;
        private final Sound sound;

        private boolean enabled;

        public Duration(int length, DurationMode mode, Sound sound) {
            this.length = length;
            this.mode = mode;
            this.counter = counter(this::trigger, initialCounterValue());
            this.enabled = sound.soundMode == SoundMode.USE_DURATION;
            this.sound = sound;
        }

        public void step() {
            if (!enabled) {
                return;
            }

            counter.step();
        }

        public void reset() {

        }

        private void trigger() {
            this.enabled = false;
            sound.disable();
        }

        private int initialCounterValue() {
            if (mode == DurationMode.LONG) {
                return (256 - length) * (CPU.FREQUENCY / 2);
            } else {
                return (64 - length) * (CPU.FREQUENCY / 256);
            }
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

    private class Channel {
        private final Terminal terminal;
        private boolean voiceInEnabled = false;
        private int volume = 0;

        public Channel(Terminal terminal) {
            this.terminal = terminal;
        }
    }

    private class Frequency {
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

        public void reset() {
            counter.reset();
            phase = 0;
        }

        private void increasePhase() {
            phase = (phase + 1) % WAVE_PATTERNS;
        }

        private int initialCounterValue() {
            int hz = CPU.FREQUENCY / (multiplier * (2048 - value));
            return (CPU.FREQUENCY / hz / WAVE_PATTERNS);
        }
    }

    private class Envelope {
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

        public void reset() {
            stepCounter.reset();
            volume = initialVolume;
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

    private class Polynomial {
        private final int shiftClock;
        private final PolynomialStep step;
        private final int dividingRatio;

        private Counter counter;
        private int lfsr = 0b0111_1111_1111_1111;

        public Polynomial(int shiftClock, PolynomialStep steps, int dividingRatio) {
            this.shiftClock = shiftClock;
            step = steps;
            this.dividingRatio = dividingRatio;
            reset();
        }

        public boolean produceSample() {
            return (lfsr & 0b0000_0000_0000_0001) == 0;
        }

        public void step() {
            counter.step();
        }

        private void reset() {
            int frequency = resolveDivisor() << shiftClock;
            counter = new Counter(this::trigger, CPU.FREQUENCY / frequency);
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
            switch (dividingRatio) {
                case 1: return 16;
                case 2: return 32;
                case 3: return 48;
                case 4: return 64;
                case 5: return 80;
                case 6: return 96;
                case 7: return 112;
                default: return 8;
            }
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

    private enum SoundMode {
        USE_DURATION,
        REPEAT
    }

    private enum DurationMode {
        SHORT,
        LONG
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
    private class NibbleArrayMemory extends ByteArrayMemory {
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
