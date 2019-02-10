package se.omfilm.gameboy.internal;

/**
 * Represents the flags for the CPU.
 *
 * The flags are set and read in different instructions.
 * They can also be read from Register#readF where the byte returned contains the bits for flags set.
 *
 * It can also enable/disable all interrupts, se Interrupts.
 */
public interface Flags {
    boolean isSet(Flag flag);

    void set(Flag flag, boolean set);

    void setInterruptsDisabled(boolean disabled);

    boolean isInterruptsDisabled();

    /**
     * The allowed flags.
     * Each interrupt has a bit that can be masked against an int to see if that flag is enabled.
     */
    enum Flag {
        ZERO(       0b1000_0000), //Z
        SUBTRACT(   0b0100_0000), //N
        HALF_CARRY( 0b0010_0000), //H
        CARRY(      0b0001_0000); //C

        private final int mask;

        Flag(int mask) {
            this.mask = mask;
        }

        public int mask() {
            return mask;
        }
    }
}
