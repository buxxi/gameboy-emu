package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class IncrementStackPointer implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int val = (stackPointer.read() + 1) % 0xFFFF;
        stackPointer.write(val);

        return 8;
    }
}
