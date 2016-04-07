package se.omfilm.gameboy.internal;

public interface Flags {
    boolean isSet(Flag flag);

    void set(Flag flag, boolean set);

    default void set(Flag... flags) {
        for (Flag flag : flags) {
            set(flag, true);
        }
    }

    default void reset(Flag... flags) {
        for (Flag flag : flags) {
            set(flag, false);
        }
    }

    void setInterruptsDisabled(boolean disabled);

    enum Flag {
        ZERO(       0b1000_0000), //Z
        SUBTRACT(   0b0100_0000), //N
        HALF_CARRY( 0b0010_0000), //H
        CARRY(      0b0001_0000); //C

        public final int mask;

        Flag(int mask) {
            this.mask = mask;
        }
    }
}
