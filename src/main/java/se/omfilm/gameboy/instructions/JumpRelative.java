package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.Predicate;

public class JumpRelative implements Instruction {
    private final Predicate<Flags> predicate;

    private JumpRelative(Predicate<Flags> predicate) {
        this.predicate = predicate;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int data = memory.readByte(programCounter.increase());

        if (predicate.test(flags)) {
            programCounter.increase((byte) data);
        }

        return 8;
    }

    public static Instruction unconditional() {
        return new JumpRelative((flags) -> true);
    }

    public static Instruction ifLastNotZero() {
        return new JumpRelative((flags) -> !flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifLastZero() {
        return new JumpRelative((flags) -> flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifLastNotCarry() {
        return new JumpRelative((flags) -> !flags.isSet(Flags.Flag.CARRY));
    }

    public static Instruction ifLastCarry() {
        return new JumpRelative((flags) -> flags.isSet(Flags.Flag.CARRY));
    }
}
