package se.omfilm.gameboy.internal;

/**
 * Represents the interrupt handling for the CPU.
 * Requesting a interrupt when it is enabled makes the CPUs program counter jump to specific memory address for that interrupt.
 * This needs to be stepped after the CPU has stepped for the interrupts to trigger.
 *
 * All interrupts can be disabled with Flags#setInterruptsDisabled, then no interrupts will be executed even if they are enabled and requested.
 *
 * The GPU requests some interrupts regarding the LCD-screen depending on timing, the same for TIMER, JOYPAD interrupt is requested when a button is pressed,
 * otherwise it can be requested by the code by writing to the correct memory address (see MMU.IORegister).
 */
public interface Interrupts {
    void enable(Interrupt interrupt, boolean enabled);

    void request(Interrupt interrupt, boolean requested);

    boolean enabled(Interrupt interrupt);

    boolean requested(Interrupt interrupt);

    /**
     * The allowed interrupts.
     * Each interrupt has a bit that can be masked against an int to see if that interrupt should be enabled/requested.
     */
    enum Interrupt {
        VBLANK( 0b0000_0001, 0x40),
        LCD(    0b0000_0010, 0x48),
        TIMER(  0b0000_0100, 0x50),
        SERIAL( 0b0000_1000, 0x58),
        JOYPAD( 0b0001_0000, 0x60);

        private static final Interrupt[] values = values();
        private final int mask;
        private final int pc;

        Interrupt(int mask, int pc) {
            this.mask = mask;
            this.pc = pc;
        }

        public int mask() {
            return mask;
        }

        public static Interrupt[] cachedValues() {
            return values;
        }

        public int jump(ProgramCounter programCounter) {
            programCounter.write(pc);
            return 20;
        }
    }
}
