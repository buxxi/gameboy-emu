package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class DecrementWordRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public DecrementWordRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        n = (n - 1) & 0xFFFF;
        target.write(registers, n);
        return 8;
    }

    public static Instruction BC() {
        return new DecrementWordRegister(Registers::readBC, Registers::writeBC);
    }

    public static Instruction DE() {
        return new DecrementWordRegister(Registers::readDE, Registers::writeDE);
    }

    public static Instruction HL() {
        return new DecrementWordRegister(Registers::readHL, Registers::writeHL);
    }
}
