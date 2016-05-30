package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.ByteArrayMemory;
import se.omfilm.gameboy.internal.memory.Memory;

public class APU {
    private Sound sound1 = new Sound();
    private Sound sound2 = new Sound();
    private Sound sound3 = new Sound();
    private Sound sound4 = new Sound();

    private Channel channel1 = new Channel();
    private Channel channel2 = new Channel();

    private Memory wavePatternRAM = new ByteArrayMemory(0xFF30, new byte[16]);

    public void step(int cycles) {

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

    private class Sound {
        private boolean enabled = false;
        private Terminal terminal = Terminal.NONE;
    }

    private class Channel {
        private boolean enabled = false;
        private int volume = 0;
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
}
