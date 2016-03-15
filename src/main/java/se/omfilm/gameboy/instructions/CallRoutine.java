package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.Predicate;

public class CallRoutine implements Instruction {
    private final Predicate<Flags> predicate;

    private CallRoutine(Predicate<Flags> predicate) {
        this.predicate = predicate;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int nextProgramCounter = memory.readWord(programCounter.increase(2));

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
}
