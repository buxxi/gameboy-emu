package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadWordIntoRegister implements Instruction {
    private RegisterWriter target;

    public LoadWordIntoRegister(RegisterWriter target) {
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, memory.readWord(programCounter.increase(2)));

        return 12;
    }

    public static Instruction toDE() {
        return new LoadWordIntoRegister(Registers::writeDE);
    }

    public static Instruction toHL() {
        return new LoadWordIntoRegister(Registers::writeHL);
    }

    public static Instruction toBC() {
        return new LoadWordIntoRegister(Registers::writeBC);
    }
}
