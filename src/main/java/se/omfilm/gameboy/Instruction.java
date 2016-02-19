package se.omfilm.gameboy;

public interface Instruction {
    void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer);
}
