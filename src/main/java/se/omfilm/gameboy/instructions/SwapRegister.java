package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class SwapRegister implements Instruction {
    private final Instruction.RegisterReader source;
    private final Instruction.RegisterWriter target;

    public SwapRegister(Instruction.RegisterReader source, Instruction.RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int val = source.read(registers);
        int result = ((val & 0b0000_1111) << 4) | ((val & 0b1111_0000) >> 4);
        target.write(registers, result);

        boolean zero = result == 0;

        flags.set(Flags.flags(zero, false, false));

        return 8;
    }

    public static Instruction A() {
        return new SwapRegister(Registers::readA, Registers::writeA);
    }
}
