package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CallRoutineIfLastNotZero extends CallRoutine {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        if (!flags.isSet(Flags.Flag.ZERO)) {
            return super.execute(memory, registers, flags, programCounter, stackPointer);
        }
        return 12;
    }
}
