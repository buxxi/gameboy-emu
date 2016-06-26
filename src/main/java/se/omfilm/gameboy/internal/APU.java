package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.ByteArrayMemory;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.io.sound.SoundPlayback;

public class APU {
    private static final int BUFFER_SIZE = 2048;

    private final SoundPlayback device;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int cycleCounter = CPU.FREQUENCY / SoundPlayback.SAMPLING_RATE;
    private int bufferOffset = 0;

    private Sound1 sound1 = new Sound1();
    private Sound2 sound2 = new Sound2();
    private Sound3 sound3 = new Sound3();
    private Sound4 sound4 = new Sound4();

    private Channel channel1 = new Channel();
    private Channel channel2 = new Channel();

    private Memory wavePatternRAM = new ByteArrayMemory(0xFF30, new byte[16]);

    public APU(SoundPlayback device) {
        this.device = device;
        device.start(); //TODO
    }

    public void step(int cycles, Interrupts interrupts) {
        cycleCounter -= cycles;
        if (cycleCounter < 0) {
            cycleCounter = CPU.FREQUENCY / SoundPlayback.SAMPLING_RATE;

            buffer[bufferOffset++] = (byte) sound1.frequency.value; //TODO: just testdata
            buffer[bufferOffset++] = (byte) sound1.frequency.value;

            if (bufferOffset == buffer.length) {
                device.write(buffer, buffer.length);
                bufferOffset = 0;
            }
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
        return  (channel1.enabled ? 0b1000_0000 : 0) |
                (channel1.volume << 4) |
                (channel2.enabled ? 0b0000_1000 : 0) |
                channel2.volume;
    }

    public void channelControl(int data) {
        channel1.enabled = (data & 0b1000_0000) != 0;
        channel1.volume = (data & 0b0111_0000) >> 4;
        channel2.enabled = (data & 0b0000_1000) != 0;
        channel2.volume = data & 0b0000_0111;
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
        frequency.value = (frequency.value & ~0xFF) | data;
    }

    public void highFrequency(int soundId, int data) {
        Frequency frequency = frequencyFromId(soundId);
        frequency.value = (frequency.value & ~0xFF00) | ((data & 0b0000_0111) << 8);
        frequency.mode = (data & 0b0100_0000) != 0 ? SoundMode.COUNTER : SoundMode.CONSECUTIVE;
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
    }

    private class Sound2 extends Sound {
        Frequency frequency = new Frequency();
        Envelope envelope = new Envelope();
        int waveDuty = 0b10;
    }

    private class Sound3 extends Sound {
        Frequency frequency = new Frequency();
        WavePatternMode wavePatternMode = WavePatternMode.MUTE;
    }

    private class Sound4 extends Sound {
        Envelope envelope = new Envelope();
        Polynomial polynomial = new Polynomial();
        SoundMode soundMode = SoundMode.COUNTER;
    }

    private abstract class Sound {
        boolean enabled = false;
        Terminal terminal = Terminal.NONE;
        int length = 0;

        public void restart() {

        }
    }

    private class Sweep {
        int time = 0;
        SweepMode mode = SweepMode.ADDITION;
        int shifts = 0;
    }

    private class Channel {
        private boolean enabled = false;
        private int volume = 0;
    }

    private class Frequency {
        private int value = 0;
        private SoundMode mode = SoundMode.COUNTER;
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
        S01 (0b0000_0001),
        S02 (0b0001_0000),
        BOTH(0b0001_0001);

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
}
