package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class CallRoutineImmediate implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.push(memory, programCounter.read() + 2);
        programCounter.write(memory.readWord(programCounter.read()));

        return 12;
    }
}
