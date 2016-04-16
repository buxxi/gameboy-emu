package se.omfilm.gameboy.internal.instructions.stack;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

import java.util.function.Predicate;

public class Return implements Instruction {
    private static final Predicate<Flags> ALWAYS_TRUE = (flags) -> true;
    private final Predicate<Flags> predicate;

    private Return(Predicate<Flags> predicate) {
        this.predicate = predicate;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        if (predicate.test(flags)) {
            programCounter.write(stackPointer.pop(memory));
            return 16 + (predicate == ALWAYS_TRUE ? 0 : 4);
        }

        return 8;
    }

    public static Instruction unconditional() {
        return new Return(ALWAYS_TRUE);
    }

    public static Instruction ifNotZero() {
        return new Return((flags) -> !flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifZero() {
        return new Return((flags) -> flags.isSet(Flags.Flag.ZERO));
    }

    public static Instruction ifNotCarry() {
        return new Return((flags) -> !flags.isSet(Flags.Flag.CARRY));
    }

    public static Instruction ifCarry() {
        return new Return((flags) -> flags.isSet(Flags.Flag.CARRY));
    }

    public static Instruction andEnableInterrupts() {
        return new ReturnEnableInterrupts();
    }

    private static class ReturnEnableInterrupts extends Return {
        private ReturnEnableInterrupts() {
            super(ALWAYS_TRUE);
        }

        public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
            super.execute(memory, registers, flags, programCounter, stackPointer);
            flags.setInterruptsDisabled(false);
            return 16;
        }
    }
}
