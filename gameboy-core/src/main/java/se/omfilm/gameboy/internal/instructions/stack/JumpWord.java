package se.omfilm.gameboy.internal.instructions.stack;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

import java.util.function.Predicate;

public class JumpWord implements Instruction {
    private static final Predicate<Flags> ALWAYS_TRUE = (flags) -> true;
    private final Predicate<Flags> predicate;

    private JumpWord(Predicate<Flags> predicate) {
        this.predicate = predicate;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int nn = programCounter.wordOperand(memory);
        if (predicate.test(flags)) {
            programCounter.write(nn);
            return 16;
        }
        return 12;
    }

    public static Instruction unconditional() {
        return new JumpWord(ALWAYS_TRUE);
    }

    public static Instruction ifLastNotZero() {
        return new JumpWord((flags) -> !flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifLastZero() {
        return new JumpWord((flags) -> flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifLastNotCarry() {
        return new JumpWord((flags) -> !flags.isSet(Flags.Flag.CARRY));
    }

    public static Instruction ifLastCarry() {
        return new JumpWord((flags) -> flags.isSet(Flags.Flag.CARRY));
    }
}
