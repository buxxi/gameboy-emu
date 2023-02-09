package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadWordIntoRegister implements Instruction {
    private final RegisterWriter target;

    private LoadWordIntoRegister(RegisterWriter target) {
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, programCounter.wordOperand(memory));

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
