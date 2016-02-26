package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class PopStackIntoRegister implements Instruction {
    private RegisterWriter target;

    public PopStackIntoRegister(RegisterWriter target) {
        this.target = target;
    }

    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int pointer = stackPointer.read();
        target.write(registers, memory.readWord(pointer));
        stackPointer.write(pointer + 2);
    }

    public static Instruction toBC() {
        return new PopStackIntoRegister(Registers::writeBC);
    }
}
