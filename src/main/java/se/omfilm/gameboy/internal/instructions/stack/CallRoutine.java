package se.omfilm.gameboy.internal.instructions.stack;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

import java.util.function.Predicate;

public class CallRoutine implements Instruction {
    private final Predicate<Flags> predicate;

    private CallRoutine(Predicate<Flags> predicate) {
        this.predicate = predicate;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int nextProgramCounter = programCounter.wordOperand(memory);

        if (predicate.test(flags)) {
            stackPointer.push(memory, programCounter.read());
            programCounter.write(nextProgramCounter);
        }

        return 12;
    }

    public static Instruction unconditional() {
        return new CallRoutine((flags) -> true);
    }

    public static Instruction ifLastNotZero() {
        return new CallRoutine((flags) -> !flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifLastZero() {
        return new CallRoutine((flags) -> flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifLastNotCarry() {
        return new CallRoutine((flags) -> !flags.isSet(Flags.Flag.CARRY));
    }

    public static Instruction ifLastCarry() {
        return new CallRoutine((flags) -> flags.isSet(Flags.Flag.CARRY));
    }
}
