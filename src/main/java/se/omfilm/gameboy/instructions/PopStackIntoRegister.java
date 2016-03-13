package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class PopStackIntoRegister implements Instruction {
    private RegisterWriter target;

    public PopStackIntoRegister(RegisterWriter target) {
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, stackPointer.pop(memory));

        return 12;
    }

    public static Instruction toAF() {
        return new PopStackIntoRegister(Registers::writeAF);
    }

    public static Instruction toBC() {
        return new PopStackIntoRegister(Registers::writeBC);
    }

    public static Instruction toDE() {
        return new PopStackIntoRegister(Registers::writeDE);
    }

    public static Instruction toHL() {
        return new PopStackIntoRegister(Registers::writeHL);
    }
}
