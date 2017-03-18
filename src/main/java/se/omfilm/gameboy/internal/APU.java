package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.ByteArrayMemory;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.io.sound.SoundPlayback;

import static se.omfilm.gameboy.io.sound.SoundPlayback.SAMPLING_RATE;

public class APU {
    private static final int WAVE_PATTERNS = 32;
    private final SoundPlayback device;
    private int sampleCounter = CPU.FREQUENCY / SAMPLING_RATE;
    private int durationCounter = CPU.FREQUENCY / SAMPLING_RATE / 2;

    private Sound1 sound1 = new Sound1();
    private Sound2 sound2 = new Sound2();
    private Sound3 sound3 = new Sound3();
    private Sound4 sound4 = new Sound4();

    private Channel leftChannel = new Channel(Terminal.LEFT);
    private Channel rightChannel = new Channel(Terminal.RIGHT);

    private NibbleArrayMemory wavePatternRAM = new NibbleArrayMemory(0xFF30, new byte[WAVE_PATTERNS]);

    public APU(SoundPlayback device) {
        this.device = device;
        device.start(); //TODO
    }

    public void step(int cycles, Interrupts interrupts) {
        for (int i = 0; i < cycles; i++) {
            durationCounter--;
            if (durationCounter < 0) {
                durationCounter += CPU.FREQUENCY / SAMPLING_RATE / 2;
                stepDuration();
            }

            stepFrequency();

            sampleCounter--;
            if (sampleCounter < 0) {
                sampleCounter += CPU.FREQUENCY / SAMPLING_RATE;
                playSample();
            }
        }
    }

    private void stepDuration() {
        if (sound3.enabled && sound3.frequency.mode == SoundMode.COUNTER) {
            sound3.lengthCounter--;
            if (sound3.lengthCounter <= 0) {
                sound3.enabled = false;
            }
        }
    }

    private void stepFrequency() {
        sound3.frequency.step();
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

        return amp;
    }

    private boolean anySoundEnabled() {
        return sound1.enabled || sound2.enabled || sound3.enabled || sound4.enabled;
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
        frequency.update((frequency.value & ~0xFF) | data, frequency.mode);
    }

    public void highFrequency(int soundId, int data) {
        Frequency frequency = frequencyFromId(soundId);
        frequency.update((frequency.value & ~0xFF00) | ((data & 0b0000_0111) << 8), (data & 0b0100_0000) != 0 ? SoundMode.COUNTER : SoundMode.CONSECUTIVE);
        if ((data & 0b1000_0000) != 0) {
            soundFromId(soundId).restart();
        }
    }

    public int highFrequency(int soundId) {
        return frequencyFromId(soundId).mode == SoundMode.COUNTER ? 0b01000_0000 : 0;
    }

    public void envelope(int soundId, int data) {
        Envelope envelope = envelopeFromId(soundId);
        envelope.initialVolume = (data & 0b1111_0000) >> 4;
        envelope.direction = (data & 0b0000_1000) != 0 ? EnvelopeDirection.AMPLIFY : EnvelopeDirection.ATTENUATE;
        envelope.sweepCount = (data & 0b000_0111);
        if (envelope.sweepCount == 0) {
            envelope.stop();
        }
    }

    public int envelope(int soundId) {
        Envelope envelope = envelopeFromId(soundId);
        return  (envelope.initialVolume << 4) |
                (envelope.direction == EnvelopeDirection.AMPLIFY ? 0b0000_1000 : 0) |
                envelope.sweepCount;
    }

    public void length(int soundId, int data) {
        switch(soundId) {
            case 1:
                sound1.waveDuty = data >> 6;
                sound1.length = data & 0b0011_1111;
                sound1.lengthCounter = sound1.length;
                break;
            case 2:
                sound2.waveDuty = data >> 6;
                sound2.length = data & 0b0011_1111;
                break;
            case 3:
                sound3.length = data;
                break;
            case 4:
                sound4.length = data;
                break;
        }
    }

    public int length(int soundId) {
        switch (soundId) {
            case 1:
                return sound1.waveDuty << 6 | sound1.length;
            case 2:
                return sound2.waveDuty << 6 | sound2.length;
            case 3:
                return sound3.length;
            case 4:
                return sound4.length;
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
        return sound4.soundMode == SoundMode.COUNTER ? 0b01000_0000 : 0;
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
        sound1.sweep.time = (data & 0b0111_0000) >> 4;
        sound1.sweep.mode = (data & 0b0000_1000) != 0 ? SweepMode.SUBTRACTION : SweepMode.ADDITION;
        sound1.sweep.shifts = data & 0b0000_0111;
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
        sound4.soundMode = (data & 0b0100_0000) != 0 ? SoundMode.COUNTER : SoundMode.CONSECUTIVE;
        if ((data & 0b1000_0000) != 0) {
            sound4.restart();
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

    private Sound soundFromId(int soundId) {
        switch (soundId) {
            case 1:
                return sound1;
            case 2:
                return sound2;
            case 3:
                return sound3;
            case 4:
                return sound4;
            default:
                throw new IllegalArgumentException("Sound " + soundId + " not found");
        }
    }

    private class Sound1 extends Sound {
        Frequency frequency = new Frequency();
        Envelope envelope = new Envelope();
        int waveDuty = 0b10;
        Sweep sweep = new Sweep();

        public int sample(Terminal terminal) {
            return 0;
        }
    }

    private class Sound2 extends Sound {
        Frequency frequency = new Frequency();
        Envelope envelope = new Envelope();
        int waveDuty = 0b10;

        public int sample(Terminal terminal) {
            return 0;
        }
    }

    private class Sound3 extends Sound {
        Frequency frequency = new Frequency();
        WavePatternMode wavePatternMode = WavePatternMode.MUTE;

        public int sample(Terminal terminal) {
            if (enabledFor(terminal)) {
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

    private class Sound4 extends Sound {
        Envelope envelope = new Envelope();
        Polynomial polynomial = new Polynomial();
        SoundMode soundMode = SoundMode.COUNTER;

        public int sample(Terminal terminal) {
            return 0;
        }
    }

    private abstract class Sound {
        boolean enabled = false;
        Terminal terminal = Terminal.NONE;
        int length = 0;
        int lengthCounter = 0;

        public void restart() {
            lengthCounter = length;
            enabled = true;
        }

        public abstract int sample(Terminal terminal);

        protected boolean enabledFor(Terminal terminal) {
            return enabled && this.terminal != Terminal.STEREO && this.terminal != terminal;
        }
    }

    private class Sweep {
        int time = 0;
        SweepMode mode = SweepMode.ADDITION;
        int shifts = 0;
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
        private int value = 0;
        private SoundMode mode = SoundMode.COUNTER;

        private int counter = 0;
        private int phase = 0;

        public void update(int value, SoundMode mode) {
            this.value = value;
            this.counter = initialCounterValue(value);
            this.mode = mode;
        }

        public void step() {
            counter--;
            if (counter < 0) {
                phase = (phase + 1) % WAVE_PATTERNS;
                counter = initialCounterValue(value);
            }
        }

        private int initialCounterValue(int value) {
            return CPU.FREQUENCY / (64 * (2048 - value));
        }
    }

    private class Envelope {
        private int initialVolume = 0;
        private EnvelopeDirection direction = EnvelopeDirection.ATTENUATE;
        private int sweepCount = 0;

        public void stop() {

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
        ATTENUATE,
        AMPLIFY
    }

    private enum SoundMode {
        COUNTER,
        CONSECUTIVE
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
