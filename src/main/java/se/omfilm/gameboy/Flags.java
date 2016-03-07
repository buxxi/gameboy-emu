package se.omfilm.gameboy;

import java.util.*;
import java.util.stream.Collectors;

public interface Flags {
    boolean isSet(Flag flag);

    void set(Flag... flags);

    void setInterruptsDisabled(boolean disabled);

    void enable(Interrupt... interrupts);

    void request(Interrupt... interrupts);

    static Flag[] flags(boolean zero, boolean halfCarry, boolean carry) {
        List<Flag> flags = new ArrayList<>();
        if (halfCarry) {
            flags.add(Flags.Flag.HALF_CARRY);
        }
        if (zero) {
            flags.add(Flags.Flag.ZERO);
        }
        if (carry) {
            flags.add(Flags.Flag.CARRY);
        }
        return flags.toArray(new Flags.Flag[flags.size()]);
    }

    static Flag[] withNegative(boolean zero, boolean halfCarry, boolean carry) {
        List<Flag> flags = new ArrayList<>();
        flags.add(Flags.Flag.SUBTRACT);
        if (halfCarry) {
            flags.add(Flags.Flag.HALF_CARRY);
        }
        if (zero) {
            flags.add(Flags.Flag.ZERO);
        }
        if (carry) {
            flags.add(Flags.Flag.CARRY);
        }
        return flags.toArray(new Flags.Flag[flags.size()]);
    }

    static Flag[] flags(boolean halfCarry, boolean zero) {
        List<Flags.Flag> flags = new ArrayList<>();
        flags.add(Flags.Flag.SUBTRACT);
        if (halfCarry) {
            flags.add(Flags.Flag.HALF_CARRY);
        }
        if (zero) {
            flags.add(Flags.Flag.ZERO);
        }
        return flags.toArray(new Flags.Flag[flags.size()]);
    }

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

    enum Interrupt {
        VBLANK( 0b0000_0001),
        LCD(    0b0000_0010),
        TIMER(  0b0000_0100),
        JOYPAD( 0b0000_1000);

        public final int mask;

        Interrupt(int mask) {
            this.mask = mask;
        }

        public static Interrupt[] fromValue(int data) {
            Set<Interrupt> result = Arrays.asList(values()).stream().filter(
                    v -> (v.mask & data) != 0
            ).collect(Collectors.toSet());
            return result.toArray(new Interrupt[result.size()]);
        }
    }
}
