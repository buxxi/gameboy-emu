package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.util.DebugPrinter;

public class Timer {
    private boolean enabled = false;
    private FREQUENCY frequency = FREQUENCY._4096;

    private int dividerCycles;
    private int dividerCounter;

    private int timerCycles;
    private int timerCounter;
    private int timerModulo;

    public void step(int cycles, Interrupts interrupts) {
        stepDivider(cycles);
        if (enabled) {
            stepTimer(cycles, interrupts);
        }
    }

    private void stepDivider(int cycles) {
        dividerCycles -= cycles;

        while (dividerCycles <= 0) {
            dividerCycles += 0xFF;
            dividerCounter = (dividerCounter + 1) & 0xFF;
        }
    }

    private void stepTimer(int cycles, Interrupts interrupts) {
        timerCycles -= cycles;

        while (timerCycles <= 0) {
            timerCycles += frequency.counterInitialValue();

            if (timerCounter == 0xFF) {
                timerCounter = timerModulo;
                interrupts.request(Interrupts.Interrupt.TIMER);
            } else {
                timerCounter++;
            }
        }
    }

    public void modulo(int data) {
        timerModulo = data;
    }

    public int modulo() {
        return timerModulo;
    }

    public void control(int data) {
        enabled = (data & 0b0000_0100) != 0;
        FREQUENCY newFrequency = FREQUENCY.fromBits(data & 0b0000_0011);
        if (newFrequency != this.frequency) {
            this.frequency = newFrequency;
            timerCycles = this.frequency.counterInitialValue();
        }
    }

    public int control() {
        return (enabled ? 0b0000_0100 : 0) | frequency.code;
    }

    public void counter(int data) {
        timerCounter = data;
    }

    public int counter() {
        return timerCounter;
    }

    public int divider() {
        return dividerCounter;
    }

    public void resetDivider() {
        dividerCounter = 0;
    }

    private enum FREQUENCY {
        _4096(  0b0000_0000, 4096),
        _262144(0b0000_0001, 262144),
        _65536( 0b0000_0010, 65536),
        _16384( 0b0000_0011, 16284);

        private final int code;
        private final int freq;

        FREQUENCY(int code, int freq) {
            this.code = code;
            this.freq = freq;
        }

        public int counterInitialValue() {
            return CPU.FREQUENCY / freq;
        }

        public static FREQUENCY fromBits(int bits) {
            for (FREQUENCY frequency : values()) {
                if (frequency.code == bits) {
                    return frequency;
                }
            }
            throw new IllegalArgumentException("No " + FREQUENCY.class.getSimpleName() + " for bits " + DebugPrinter.hex(bits, 4));
        }

        @Override
        public String toString() {
            return name().substring(1) + "Hz";
        }
    }
}
