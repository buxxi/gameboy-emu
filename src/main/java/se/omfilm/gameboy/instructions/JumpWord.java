package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.Predicate;

public class JumpWord implements Instruction {
    private final Predicate<Flags> predicate;

    public JumpWord(Predicate<Flags> predicate) {
        this.predicate = predicate;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int nn = programCounter.wordOperand(memory);
        if (predicate.test(flags)) {
            programCounter.write(nn);
        }
        return 12;
    }

    public static Instruction unconditional() {
        return new JumpWord((flags) -> true);
    }

    public static Instruction ifLastNotZero() {
        return new JumpWord((flags) -> !flags.isSet(Flags.Flag.ZERO));
    }
}
