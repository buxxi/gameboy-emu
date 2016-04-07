package se.omfilm.gameboy.internal.instructions.stack;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

import java.util.function.Predicate;

public class JumpRelative implements Instruction {
    private final Predicate<Flags> predicate;

    private JumpRelative(Predicate<Flags> predicate) {
        this.predicate = predicate;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int data = programCounter.byteOperand(memory);

        if (predicate.test(flags)) {
            programCounter.write(programCounter.read() + ((byte) data));
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
