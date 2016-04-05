package se.omfilm.gameboy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

public class Timer {
    private static final Logger log = LoggerFactory.getLogger(Timer.class);

    private final Interrupts interrupts;

    private boolean enabled = false;
    private FREQUENCY frequency = FREQUENCY._4096;

    private int cycleCounter;
    private int timerCounter;
    private int timerModulo;

    public Timer(Interrupts interrupts) {
        this.interrupts = interrupts;
    }

    public void step(int cycles) {
        if (!enabled) {
            return;
        }

        cycleCounter -= cycles;

        if (cycleCounter <= 0) {
            cycleCounter = frequency.counterInitialValue();

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

    public void control(int data) {
        enabled = (data & 0b0000_0100) != 0;
        FREQUENCY newFrequency = FREQUENCY.fromCode(data & 0b0000_0011);
        if (newFrequency != this.frequency) {
            this.frequency = newFrequency;
            cycleCounter = this.frequency.counterInitialValue();
        }
    }

    public void counter(int data) {
        timerCounter = data;
    }

    public int counter() {
        return timerCounter;
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

        public static FREQUENCY fromCode(int code) {
            for (FREQUENCY frequency : values()) {
                if (frequency.code == code) {
                    return frequency;
                }
            }
            throw new IllegalArgumentException("No " + FREQUENCY.class.getSimpleName() + " for code " + DebugPrinter.hex(code, 4));
        }
    }
}
