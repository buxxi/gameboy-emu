package se.omfilm.gameboy.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.memory.ByteArrayMemory;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.io.sound.SoundPlayback;
import se.omfilm.gameboy.util.Runner.Counter;


import static se.omfilm.gameboy.io.sound.SoundPlayback.SAMPLING_RATE;
import static se.omfilm.gameboy.util.Runner.counter;

public class APU {
    private static final Logger log = LoggerFactory.getLogger(APU.class);
    private static final int WAVE_PATTERNS = 32;

    private final SoundPlayback device;

    private final Counter sampleCounter = counter(this::playSample, CPU.FREQUENCY / SAMPLING_RATE);
    private final Counter envelopeCounter = counter(this::stepEnvelopes, CPU.FREQUENCY / 64);
    private final Counter sweepCounter = counter(this::stepSweep, CPU.FREQUENCY / 128);

    private final SquareWaveSound sound1 = new SquareWaveSound();
    private final SquareWaveSound sound2 = new SquareWaveSound();
    private final WaveSound sound3 = new WaveSound();
    private final NoiseSound sound4 = new NoiseSound();

    private final Channel leftChannel = new Channel(Terminal.LEFT);
    private final Channel rightChannel = new Channel(Terminal.RIGHT);

    private final NibbleArrayMemory wavePatternRAM = new NibbleArrayMemory(0xFF30, new byte[WAVE_PATTERNS]);

    public APU(SoundPlayback device) {
        this.device = device;
        device.start(); //TODO
    }

    public void step(int cycles, Interrupts interrupts) {
        for (int i = 0; i < cycles; i++) {
            stepDuration();
            stepFrequency();
            envelopeCounter.step();
            sweepCounter.step();
            sampleCounter.step();
        }
    }

    private void stepDuration() {
        if (sound1.enabled) {
            sound1.duration.step();
        }
        if (sound2.enabled) {
            sound2.duration.step();
        }
        if (sound3.enabled) {
            sound3.duration.step();
        }
    }

    private void stepEnvelopes() {
        if (sound1.enabled) {
            sound1.envelope.step();
        }
        if (sound2.enabled) {
            sound2.envelope.step();
        }
    }

    private void stepSweep() {
        if (sound1.enabled) {
            sound1.sweep.step();
        }
    }

    private void stepFrequency() {
        if (sound1.enabled) {
            sound1.frequency.step();
        }
        if (sound2.enabled) {
            sound2.frequency.step();
        }
        if (sound3.enabled) {
            sound3.frequency.step();
        }
    }

    private void playSample() {
        if (anySoundEnabled()) {
            int left = mixSample(leftChannel);
            int right = mixSample(rightChannel);
            device.output(left, right);
        } else {
            device.output(0, 0);
        }
    }

    private int mixSample(Channel channel) {
        int amp = 0;
        amp += sound1.sample(channel.terminal);
        amp += sound2.sample(channel.terminal);
        amp += sound3.sample(channel.terminal);
        amp += sound4.sample(channel.terminal);
        amp *= channel.volume;
        amp = amp / 5; //TODO: better way to do this?

        return amp;
    }

    private boolean anySoundEnabled() {
        return sound1.enabled || sound2.enabled || sound3.enabled || sound4.enabled;
    }

    private void restartSound(int soundId) {
        switch (soundId) {
            case 1:
                sound1.frequency.reset();
                sound1.envelope.reset();
                sound1.duration.reset();
                sound1.enabled = true;
                break;
            case 2:
                sound2.frequency.reset();
                sound2.envelope.reset();
                sound2.duration.reset();
                sound2.enabled = true;
                break;
            case 3:
                sound3.frequency.reset();
                sound3.duration.reset();
                sound3.enabled = true;
                break;
            default:
                log.warn("Restarting sound " + soundId + " not implemented");
        }
    }

    public void reset() {
        sweep(1, 0x80);
        length(1, 0xBF);
        envelope(1, 0xF3);
        highFrequency(1, 0xBF);
        length(2, 0x3F);
        envelope(2, 0x00);
        highFrequency(2, 0xBF);
        soundControl(3, 0x7F);
        length(3, 0xFF);
        outputLevel(3, 0x9F);
        highFrequency(3, 0xBF);
        length(4, 0xFF);
        envelope(4, 0x00);
        polynomialCounter(4, 0x00);
        soundMode(4, 0xBF);
        channelControl(0x77);
        outputTerminal(0xF3);
        soundEnabled(0xF1);
    }

    public int soundEnabled() {
        return  (sound4.enabled ? 0b0000_1000 : 0) |
                (sound3.enabled ? 0b0000_0100 : 0) |
                (sound2.enabled ? 0b0000_0010 : 0) |
                (sound1.enabled ? 0b0000_0001 : 0);
    }

    public void soundEnabled(int data) {
        sound1.enabled = (data & 0b1000_0000) != 0;
        sound2.enabled = (data & 0b1000_0000) != 0;
        sound3.enabled = (data & 0b1000_0000) != 0;
        sound4.enabled = (data & 0b1000_0000) != 0;
    }

    public int channelControl() {
        return  (leftChannel.voiceInEnabled ? 0b1000_0000 : 0) |
                (leftChannel.volume << 4) |
                (rightChannel.voiceInEnabled ? 0b0000_1000 : 0) |
                rightChannel.volume;
    }

    public void channelControl(int data) {
        leftChannel.voiceInEnabled = (data & 0b1000_0000) != 0;
        leftChannel.volume = (data & 0b0111_0000) >> 4;
        rightChannel.voiceInEnabled = (data & 0b0000_1000) != 0;
        rightChannel.volume = data & 0b0000_0111;
    }

    public int outputTerminal() {
        return  sound4.terminal.binaryValue << 3 |
                sound3.terminal.binaryValue << 2 |
                sound2.terminal.binaryValue << 1 |
                sound1.terminal.binaryValue;
    }

    public void outputTerminal(int data) {
        sound4.terminal = Terminal.fromBinary((data & 0b1000_1000) >> 3);
        sound3.terminal = Terminal.fromBinary((data & 0b0100_0100) >> 2);
        sound2.terminal = Terminal.fromBinary((data & 0b0010_0010) >> 1);
        sound1.terminal = Terminal.fromBinary(data & 0b0001_0001);
    }

    public Memory wavePatternRAM() {
        return wavePatternRAM;
    }

    public void lowFrequency(int soundId, int data) {
        Frequency frequency = frequencyFromId(soundId);
        int mergedData = (frequency.value & ~0xFF) | data;
        switch (soundId) {
            case 1:
                sound1.frequency = new Frequency(mergedData, 32, frequency.mode);
                break;
            case 2:
                sound2.frequency = new Frequency(mergedData, 32, frequency.mode);
                break;
            case 3:
                sound3.frequency = new Frequency(mergedData, 64, frequency.mode);
                break;
        }
    }

    public void highFrequency(int soundId, int data) {
        Frequency frequency = frequencyFromId(soundId);
        int mergedData = (frequency.value & ~0xFF00) | ((data & 0b0000_0111) << 8);
        SoundMode mode = (data & 0b0100_0000) != 0 ? SoundMode.USE_DURATION : SoundMode.REPEAT;
        switch (soundId) {
            case 1:
                sound1.frequency = new Frequency(mergedData, 32, mode);
                break;
            case 2:
                sound2.frequency = new Frequency(mergedData, 32, mode);
                break;
            case 3:
                sound3.frequency = new Frequency(mergedData, 64, mode);
                break;
        }
        if ((data & 0b1000_0000) != 0) {
            restartSound(soundId);
        }
    }

    public int highFrequency(int soundId) {
        return frequencyFromId(soundId).mode == SoundMode.USE_DURATION ? 0b01000_0000 : 0;
    }

    public void envelope(int soundId, int data) {
        int initialVolume = (data & 0b1111_0000) >> 4;
        EnvelopeDirection direction = (data & 0b0000_1000) != 0 ? EnvelopeDirection.INCREASE : EnvelopeDirection.DECREASE;
        int steps = (data & 0b000_0111);

        Envelope envelope = new Envelope(initialVolume, direction, steps);

        switch (soundId) {
            case 1:
                sound1.envelope = envelope;
                break;
            case 2:
                sound2.envelope = envelope;
                break;
            case 4:
                sound4.envelope = envelope;
                break;
        }
    }

    public int envelope(int soundId) {
        Envelope envelope = envelopeFromId(soundId);
        return  (envelope.initialVolume << 4) |
                (envelope.direction == EnvelopeDirection.INCREASE ? 0b0000_1000 : 0) |
                envelope.steps;
    }

    public void length(int soundId, int data) {
        switch(soundId) {
            case 1:
                sound1.waveDutyMode = WaveDutyMode.fromValue(data >> 6);
                sound1.duration = new Duration(data & 0b0011_1111, DurationMode.SHORT, sound1);
                break;
            case 2:
                sound2.waveDutyMode = WaveDutyMode.fromValue(data >> 6);
                sound2.duration = new Duration(data & 0b0011_1111, DurationMode.SHORT, sound2);
                break;
            case 3:
                sound3.duration = new Duration(data, DurationMode.LONG, sound3);
                break;
            case 4:
                sound4.duration = new Duration(data, DurationMode.SHORT, sound4);
                break;
        }
    }

    public int length(int soundId) {
        switch (soundId) {
            case 1:
                return sound1.waveDutyMode.bits << 6 | sound1.duration.length;
            case 2:
                return sound2.waveDutyMode.bits << 6 | sound2.duration.length;
            case 3:
                return sound3.duration.length;
            case 4:
                return sound4.duration.length;
            default:
                throw new IllegalArgumentException("Sound " + soundId + " has no length");
        }
    }

    public int sweep(int soundId) {
        if (soundId != 1) {
            throw new IllegalArgumentException("Only sound1 can sweep.");
        }
        return  (sound1.sweep.time << 4) |
                (sound1.sweep.mode == SweepMode.SUBTRACTION ? 0b0000_1000 : 0) |
                sound1.sweep.shifts;
    }

    public int soundControl(int soundId) {
        if (soundId != 3) {
            throw new IllegalArgumentException("Only sound3 can turn on/off.");
        }
        return sound3.enabled ? 0b1000_0000 : 0;
    }

    public int outputLevel(int soundId) {
        if (soundId != 3) {
            throw new IllegalArgumentException("Only sound3 can read output levels.");
        }
        return sound3.wavePatternMode.bits << 5;
    }

    public int soundMode(int soundId) {
        if (soundId != 4) {
            throw new IllegalArgumentException("Only sound4 can read sound mode.");
        }
        return sound4.soundMode == SoundMode.USE_DURATION ? 0b01000_0000 : 0;
    }

    public int polynomialCounter(int soundId) {
        if (soundId != 4) {
            throw new IllegalArgumentException("Only sound4 can read polynomial counter.");
        }
        return  (sound4.polynomial.shiftClock << 4) |
                (sound4.polynomial.step == PolynomialStep._7_STEPS ? 0b0000_1000 : 0) |
                sound4.polynomial.dividingRatio;

    }

    public void sweep(int soundId, int data) {
        if (soundId != 1) {
            throw new IllegalArgumentException("Only sound1 can sweep.");
        }
        int time = (data & 0b0111_0000) >> 4;
        SweepMode mode = (data & 0b0000_1000) != 0 ? SweepMode.SUBTRACTION : SweepMode.ADDITION;
        int shifts = data & 0b0000_0111;
        sound1.sweep = new Sweep(time, mode, shifts);
    }

    public void soundControl(int soundId, int data) {
        if (soundId != 3) {
            throw new IllegalArgumentException("Only sound3 can turn on/off.");
        }
        sound3.enabled = (data & 0b1000_0000) != 0;
    }

    public void outputLevel(int soundId, int data) {
        if (soundId != 3) {
            throw new IllegalArgumentException("Only sound3 can set output levels.");
        }
        sound3.wavePatternMode = WavePatternMode.fromValue((data & 0b0110_0000) >> 5);
    }

    public void soundMode(int soundId, int data) {
        if (soundId != 4) {
            throw new IllegalArgumentException("Only sound4 can set sound mode.");
        }
        sound4.soundMode = (data & 0b0100_0000) != 0 ? SoundMode.USE_DURATION : SoundMode.REPEAT;
        if ((data & 0b1000_0000) != 0) {
            restartSound(soundId);
        }
    }

    public void polynomialCounter(int soundId, int data) {
        if (soundId != 4) {
            throw new IllegalArgumentException("Only sound4 can set polynomial counter.");
        }
        sound4.polynomial.shiftClock = (data & 0b1111_0000) >> 4;
        sound4.polynomial.step = (data & 0b0000_1000) != 0 ? PolynomialStep._7_STEPS : PolynomialStep._15_STEPS;
        sound4.polynomial.dividingRatio = (data & 0b0000_0111);
    }

    private Envelope envelopeFromId(int soundId) {
        switch (soundId) {
            case 1:
                return sound1.envelope;
            case 2:
                return sound2.envelope;
            case 4:
                return sound4.envelope;
            default:
                throw new IllegalArgumentException("Sound " + soundId + " has no envelope");
        }
    }

    private Frequency frequencyFromId(int soundId) {
        switch (soundId) {
            case 1:
                return sound1.frequency;
            case 2:
                return sound2.frequency;
            case 3:
                return sound3.frequency;
            default:
                throw new IllegalArgumentException("Sound " + soundId + " has no frequency");
        }
    }

    private class SquareWaveSound extends Sound {
        Frequency frequency = new Frequency(0, 32, SoundMode.REPEAT);
        Envelope envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
        WaveDutyMode waveDutyMode = WaveDutyMode.HALF;
        Sweep sweep = new Sweep(0, SweepMode.ADDITION, 0); //Should only be used in sound1

        public int sample(Terminal terminal) {
            if (!enabledFor(terminal)) {
                return 0;
            }

            return waveData() * envelope.volume();
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
        Frequency frequency = new Frequency(0, 64, SoundMode.REPEAT);
        WavePatternMode wavePatternMode = WavePatternMode.MUTE;

        public int sample(Terminal terminal) {
            if (!enabledFor(terminal)) {
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
    }

    private class NoiseSound extends Sound {
        Envelope envelope = new Envelope(0, EnvelopeDirection.DECREASE, 0);
        Polynomial polynomial = new Polynomial();
        SoundMode soundMode = SoundMode.USE_DURATION;

        public int sample(Terminal terminal) {
            return 0;
        }
    }

    private abstract class Sound {
        boolean enabled = false;
        Terminal terminal = Terminal.NONE;
        Duration duration = new Duration(0, DurationMode.SHORT, this);

        public abstract int sample(Terminal terminal);

        protected boolean enabledFor(Terminal terminal) {
            return enabled && (this.terminal == Terminal.STEREO || this.terminal == terminal);
        }

        protected void disable() {
            this.enabled = false;
        }
    }

    private class Duration {
        private final int length;
        private final DurationMode mode;
        private final Counter counter;

        private boolean enabled;

        public Duration(int length, DurationMode mode, Sound sound) {
            this.length = length;
            this.mode = mode;
            this.counter = counter(() -> {
                this.enabled = false;
                sound.disable();
            }, initialCounterValue());
            this.enabled = length != 0;
        }

        public void step() {
            if (!enabled) {
                return;
            }

            counter.step();
        }

        public void reset() {
            this.counter.reset();
            this.enabled = length != 0;
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
                sound1.frequency = new Frequency(shadowFrequency, sound1.frequency.multiplier, sound1.frequency.mode);
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
        private final SoundMode mode;

        private final Counter counter;
        private final int multiplier;

        private int phase = 0;

        public Frequency(int value, int multiplier, SoundMode mode) {
            this.value = value;
            this.mode = mode;
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
        private int shiftClock = 0;
        private PolynomialStep step = PolynomialStep._15_STEPS;
        private int dividingRatio = 0;
    }

    private enum Terminal {
        NONE(0b0000_0000),
        LEFT(0b0000_0001),
        RIGHT(0b0001_0000),
        STEREO(0b0001_0001);

        private final int binaryValue;

        Terminal(int binaryValue) {
            this.binaryValue = binaryValue;
        }

        static Terminal fromBinary(int binaryValue) {
            for (Terminal t : values()) {
                if (binaryValue == t.binaryValue) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Invalid terminal value: " + binaryValue);
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

    private enum WavePatternMode {
        MUTE            (0b00),
        NORMAL          (0b01),
        SHIFTED_ONCE    (0b10),
        SHIFTED_TWICE   (0b11);

        private final int bits;

        WavePatternMode(int bits) {
            this.bits = bits;
        }

        public static WavePatternMode fromValue(int input) {
            for (WavePatternMode w : values()) {
                if (w.bits == input) {
                    return w;
                }
            }
            throw new IllegalArgumentException("Invalid WavePatternMode value: " + input);
        }
    }

    private enum WaveDutyMode {
        EIGHT(0b00),
        QUARTER(0b01),
        HALF(0b10),
        THREE_QUARTERS(0b11);

        private final int bits;

        WaveDutyMode(int bits) {
            this.bits = bits;
        }

        public static WaveDutyMode fromValue(int input) {
            for (WaveDutyMode w : values()) {
                if (w.bits == input) {
                    return w;
                }
            }
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
            return (this.data[index] << 4) & this.data[index + 1];
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
