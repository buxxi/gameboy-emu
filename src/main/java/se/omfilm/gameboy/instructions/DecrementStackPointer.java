package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class DecrementStackPointer implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = stackPointer.read();
        n = (n - 1) & 0xFFFF;
        stackPointer.write(n);
        return 8;
    }
}
