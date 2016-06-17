package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.Memory;

public interface Interrupts {
    int step(Memory memory);

    void enable(Interrupt interrupt, boolean enabled);

    void request(Interrupt interrupt, boolean requested);

    boolean enabled(Interrupt interrupt);

    boolean requested(Interrupt interrupt);

    enum Interrupt {
        VBLANK( 0b0000_0001),
        LCD(    0b0000_0010),
        TIMER(  0b0000_0100),
        SERIAL( 0b0000_1000),
        JOYPAD( 0b0001_0000);

        private static final Interrupt[] values = values();
        private final int mask;

        Interrupt(int mask) {
            this.mask = mask;
        }

        public int mask() {
            return mask;
        }

        public static Interrupt[] cachedValues() {
            return values;
        }
    }
}
